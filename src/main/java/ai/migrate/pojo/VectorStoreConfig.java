package ai.migrate.pojo;

import ai.vector.VectorStoreConstant;

public class VectorStoreConfig {
    private String type;
    private String default_category;
    private String url;
    private String metric = VectorStoreConstant.VECTOR_METRIC_COSINE;
    private String api_key;
    private String index_name;
    private String environment;
    private String project_name;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getIndex_name() {
        return index_name;
    }

    public void setIndex_name(String index_name) {
        this.index_name = index_name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefault_category() {
        return default_category;
    }

    public void setDefault_category(String default_category) {
        this.default_category = default_category;
    }

    @Override
    public String toString() {
        return "VectorStore{" +
                "type='" + type + '\'' +
                ", default_category='" + default_category + '\'' +
                '}';
    }
}
