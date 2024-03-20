package ai.migrate.pojo;

public class EmbeddingConfig {
    private String type;
    private String openai_api_key;
    private String model_name;
    private String api_endpoint;

    public String getOpenai_api_key() {
        return openai_api_key;
    }

    public void setOpenai_api_key(String openai_api_key) {
        this.openai_api_key = openai_api_key;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getApi_endpoint() {
        return api_endpoint;
    }

    public void setApi_endpoint(String api_endpoint) {
        this.api_endpoint = api_endpoint;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
