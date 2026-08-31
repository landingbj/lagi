package ai.llm.pojo;

/** Calendar bucket used by the cost and token usage trend. */
public enum TokenStatisticsTrendBucket {
    HOUR("hour"),
    DAY("day"),
    MONTH("month");

    private final String value;

    TokenStatisticsTrendBucket(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
