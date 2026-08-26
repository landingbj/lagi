package ai.llm.pojo;

import java.util.Locale;

/**
 * Reporting window: local-time calendar periods or full history when {@link #ALL}.
 */
public enum TokenStatisticsRange {
    /** From start of today through now (same calendar day). */
    TODAY(1),
    /** Current natural week, from Monday 00:00 through now. */
    THIS_WEEK(0),
    /** Current natural month, from day 1 00:00 through now. */
    THIS_MONTH(0),
    /** Legacy rolling 7-day range, retained for compatible callers. */
    LAST_7_DAYS(7),
    /** Legacy rolling 30-day range, retained for compatible callers. */
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
     * Parses HTTP query values such as {@code today}, {@code week}, {@code month},
     * {@code 7d}, {@code 30d}, and {@code all}. Week and month are natural calendar
     * periods; 7d and 30d retain their rolling-window semantics for compatibility.
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
            case "week":
                return THIS_WEEK;
            case "month":
                return THIS_MONTH;
            case "7d":
            case "7":
            case "last_7_days":
                return LAST_7_DAYS;
            case "30d":
            case "30":
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
