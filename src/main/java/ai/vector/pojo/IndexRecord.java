package ai.vector.pojo;

import java.util.Map;

public class IndexRecord {
    private String document;
    private String id;
    private Map<String, Object> metadata;
    private Float distance;

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String document;
        private String id;
        private Map<String, Object> metadata;
        private Float distance;

        public Builder withDocument(String document) {
            this.document = document;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withDistance(Float distance) {
            this.distance = distance;
            return this;
        }

        public IndexRecord build() {
            IndexRecord indexRecord = new IndexRecord();
            indexRecord.setDocument(document);
            indexRecord.setId(id);
            indexRecord.setMetadata(metadata);
            indexRecord.setDistance(distance);
            return indexRecord;
        }
    }

    public String toString() {
        return "IndexRecord{" + "\n" +
                "document='" + document + '\'' + ",\n" +
                "id='" + id + '\'' + ",\n" +
                "metadata=" + metadata + ",\n" +
                "distance=" + distance + ",\n" +
                '}';
    }
}
