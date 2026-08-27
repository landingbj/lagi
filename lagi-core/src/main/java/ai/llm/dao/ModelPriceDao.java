package ai.llm.dao;

import ai.common.db.HikariDS;
import ai.llm.pojo.TokenUsageCost;
import ai.utils.AiGlobal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Local model-price catalogue used for LinkMind usage estimates. */
public class ModelPriceDao {
    static final String CNY = "CNY";
    /** Fixed reference exchange rate used to make local usage estimates comparable. */
    static final BigDecimal CNY_PER_USD = new BigDecimal("7.20");
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000L);
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    public void initialize() throws SQLException {
        ensureSchema();
    }

    public TokenUsageCost calculate(String provider, String model, long inputTokens, long outputTokens) {
        try {
            ensureSchema();
            String normalizedProvider = trimToEmpty(provider);
            String normalizedModel = trimToEmpty(model);
            ModelPrice price = !normalizedProvider.isEmpty() && !normalizedModel.isEmpty()
                    ? findEffectivePrice(normalizedProvider, normalizedModel) : null;
            String priceSource = "local_config";
            if (price == null || price.inputPricePerMillion == null) {
                price = similarModelEstimate(normalizedModel);
                priceSource = "similar_model_estimate";
            }
            BigDecimal inputCost = BigDecimal.valueOf(Math.max(0L, inputTokens))
                    .multiply(price.inputPricePerMillion)
                    .divide(MILLION, 8, RoundingMode.HALF_UP);
            BigDecimal outputCost = BigDecimal.valueOf(Math.max(0L, outputTokens))
                    .multiply(price.outputPricePerMillion == null ? BigDecimal.ZERO : price.outputPricePerMillion)
                    .divide(MILLION, 8, RoundingMode.HALF_UP);
            return TokenUsageCost.builder()
                    .inputCost(inputCost)
                    .outputCost(outputCost)
                    .totalCost(inputCost.add(outputCost).setScale(8, RoundingMode.HALF_UP))
                    .currency(CNY)
                    .priceSource(priceSource)
                    .build();
        } catch (SQLException e) {
            return null;
        }
    }

    /** Used by deployment import tooling and tests to seed an approved local price row. */
    public void upsert(String provider, String model, BigDecimal inputPricePerMillion,
                       BigDecimal outputPricePerMillion, String currency, long effectiveAt) throws SQLException {
        if (isBlank(provider) || isBlank(model) || inputPricePerMillion == null || isBlank(currency)) {
            throw new IllegalArgumentException("provider, model, input price and currency are required");
        }
        ensureSchema();
        String sql = "INSERT INTO linkmind_model_prices(provider, model, input_price_per_million, "
                + "output_price_per_million, currency, effective_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(provider, model, effective_at) DO UPDATE SET "
                + "input_price_per_million=excluded.input_price_per_million, "
                + "output_price_per_million=excluded.output_price_per_million, currency=excluded.currency, "
                + "updated_at=excluded.updated_at";
        long now = System.currentTimeMillis();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provider.trim());
            ps.setString(2, model.trim());
            ps.setBigDecimal(3, toCny(inputPricePerMillion, currency));
            if (outputPricePerMillion == null) {
                ps.setNull(4, java.sql.Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, toCny(outputPricePerMillion, currency));
            }
            ps.setString(5, CNY);
            ps.setLong(6, effectiveAt);
            ps.setLong(7, now);
            ps.executeUpdate();
        }
    }

    public static void resetSchemaForTests() {
        synchronized (SCHEMA_LOCK) {
            schemaReady = false;
        }
    }

    private ModelPrice findEffectivePrice(String provider, String model) throws SQLException {
        ModelPrice exact = findEffectivePrice(provider, model, true);
        if (exact != null) {
            return exact;
        }
        // A SaaS-routed request is recorded through LandingAdapter, while a direct
        // request is recorded through the configured adapter type.  When the model
        // belongs to one vendor only, use that catalogued vendor price without
        // changing the recorded request provider.
        return findEffectivePrice(provider, model, false);
    }

    private ModelPrice findEffectivePrice(String provider, String model, boolean exactProvider) throws SQLException {
        String sql = "SELECT input_price_per_million, output_price_per_million, currency "
                + "FROM linkmind_model_prices WHERE provider = ? COLLATE NOCASE "
                + "AND model = ? COLLATE NOCASE AND effective_at <= ? "
                + "ORDER BY effective_at DESC LIMIT 1";
        if (!exactProvider) {
            sql = "SELECT input_price_per_million, output_price_per_million, currency "
                    + "FROM linkmind_model_prices p WHERE model = ? COLLATE NOCASE AND effective_at <= ? "
                    + "AND effective_at = (SELECT MAX(p2.effective_at) FROM linkmind_model_prices p2 "
                    + "WHERE p2.provider = p.provider COLLATE NOCASE AND p2.model = p.model COLLATE NOCASE "
                    + "AND p2.effective_at <= ?) ORDER BY provider COLLATE NOCASE";
        }
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            long now = System.currentTimeMillis();
            if (exactProvider) {
                ps.setString(1, provider);
                ps.setString(2, model);
                ps.setLong(3, now);
            } else {
                ps.setString(1, model);
                ps.setLong(2, now);
                ps.setLong(3, now);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ModelPrice price = new ModelPrice(rs.getBigDecimal("input_price_per_million"),
                        rs.getBigDecimal("output_price_per_million"), rs.getString("currency"));
                // A fallback is valid only when the model is catalogued by one
                // provider.  Otherwise an operator must configure the exact route.
                return !exactProvider && rs.next() ? null : price;
            }
        }
    }

    private void ensureSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS linkmind_model_prices ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, provider TEXT NOT NULL, model TEXT NOT NULL, "
                        + "input_price_per_million DECIMAL(20,8), output_price_per_million DECIMAL(20,8), "
                        + "currency TEXT NOT NULL DEFAULT 'CNY', effective_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, "
                        + "UNIQUE(provider, model, effective_at))");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_model_prices_lookup "
                        + "ON linkmind_model_prices(provider, model, effective_at)");
                ModelPriceCatalog.seedDefaults(conn);
                normalizeStoredPricesToCny(conn);
                schemaReady = true;
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static BigDecimal toCny(BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        String normalized = trimToEmpty(currency).toUpperCase();
        if (normalized.isEmpty() || CNY.equals(normalized) || "RMB".equals(normalized)) {
            return amount;
        }
        if ("USD".equals(normalized)) {
            return amount.multiply(CNY_PER_USD).setScale(8, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("only CNY and USD prices are supported by the local price catalogue");
    }

    private static void normalizeStoredPricesToCny(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE linkmind_model_prices SET input_price_per_million=input_price_per_million * ?, "
                        + "output_price_per_million=CASE WHEN output_price_per_million IS NULL THEN NULL "
                        + "ELSE output_price_per_million * ? END, currency=? "
                        + "WHERE currency IS NOT NULL AND UPPER(currency)='USD'")) {
            ps.setBigDecimal(1, CNY_PER_USD);
            ps.setBigDecimal(2, CNY_PER_USD);
            ps.setString(3, CNY);
            ps.executeUpdate();
        }
    }

    private static ModelPrice similarModelEstimate(String model) {
        String normalized = trimToEmpty(model).toLowerCase();
        if (normalized.contains("embedding") || normalized.contains("embed") || normalized.contains("bge")) {
            return new ModelPrice(new BigDecimal("0.5"), BigDecimal.ZERO, CNY);
        }
        if (normalized.contains("nano") || normalized.contains("mini") || normalized.contains("lite")
                || normalized.contains("flash") || normalized.contains("air")) {
            return new ModelPrice(new BigDecimal("0.5"), new BigDecimal("2"), CNY);
        }
        if (normalized.contains("coder") || normalized.contains("code")) {
            return new ModelPrice(new BigDecimal("1.5"), new BigDecimal("6"), CNY);
        }
        if (normalized.contains("opus") || normalized.contains("max") || normalized.contains("pro")
                || normalized.contains("ultra")) {
            return new ModelPrice(new BigDecimal("2.5"), new BigDecimal("10"), CNY);
        }
        return new ModelPrice(new BigDecimal("0.8"), new BigDecimal("3.2"), CNY);
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class ModelPrice {
        private final BigDecimal inputPricePerMillion;
        private final BigDecimal outputPricePerMillion;
        private final String currency;

        private ModelPrice(BigDecimal inputPricePerMillion, BigDecimal outputPricePerMillion, String currency) {
            this.inputPricePerMillion = inputPricePerMillion;
            this.outputPricePerMillion = outputPricePerMillion;
            this.currency = currency;
        }
    }
}
