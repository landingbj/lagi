package ai.vector.pojo;

import java.util.Map;

public class UpsertRecord {
    private String document;
    private String id;
    private Map<String, String> metadata;

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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String document;
        private String id;
        private Map<String, String> metadata;


        public Builder withDocument(String document) {
            this.document = document;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public UpsertRecord build() {
            UpsertRecord upsertRecord = new UpsertRecord();
            upsertRecord.setDocument(document);
            upsertRecord.setId(id);
            upsertRecord.setMetadata(metadata);
            return upsertRecord;
        }
    }
}
