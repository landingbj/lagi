package ai.deploy;


import ai.deploy.pojo.*;

import java.util.List;

public interface ModelDeployment {

    boolean newDeploy(DeployInfo deployRequest);

    boolean updateDeploy(DeployInfo deployRequest);

    boolean deleteDeploy(DeployInfo deployRequest);

    List<DeployInfo> getDeploys(String userId);

    DeployResult deploy(DeployInfo deployRequest);

    UnDeployResult undeploy(DeployInfo unDeployRequest);


}

