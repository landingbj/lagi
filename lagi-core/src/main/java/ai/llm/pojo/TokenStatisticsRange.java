package ai.llm.pojo;

import java.util.Locale;

/**
 * Reporting window: local-time calendar days, a contiguous span ending on today,
 * or full history when {@link #ALL}.
 */
public enum TokenStatisticsRange {
    /** From start of today through now (same calendar day). */
    TODAY(1),
    /** Last 7 calendar days including today. */
    LAST_7_DAYS(7),
    /** Last 30 calendar days including today. */
    LAST_30_DAYS(30),
    /** Entire table (no time filter). {@link #getCalendarSpanDays()} is 0. */
    ALL(0);

    private final int calendarSpanDays;

    TokenStatisticsRange(int calendarSpanDays) {
        this.calendarSpanDays = calendarSpanDays;
    }

    /** Number of calendar days in the window (daily average = total usage / this); 0 for {@link #ALL}. */
    public int getCalendarSpanDays() {
        return calendarSpanDays;
    }

    /**
     * Parses HTTP query values such as {@code today}, {@code 7d}, {@code 30d}, {@code all}.
     *
     * @throws IllegalArgumentException if the value is not recognized
     */
    public static TokenStatisticsRange fromQueryParam(String param) {
        if (param == null || param.isEmpty()) {
            return TODAY;
        }
        String p = param.trim().toLowerCase(Locale.ROOT);
        switch (p) {
            case "today":
            case "1d":
                return TODAY;
            case "7d":
            case "7":
            case "week":
            case "last_7_days":
                return LAST_7_DAYS;
            case "30d":
            case "30":
            case "month":
            case "last_30_days":
                return LAST_30_DAYS;
            case "all":
            case "any":
                return ALL;
            default:
                throw new IllegalArgumentException("Unknown range: " + param);
        }
    }
}
