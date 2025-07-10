package ai.model.service;


public interface ModelDeployment {

    String deploy(String modelId);

    String undeploy(String modelId);

}

