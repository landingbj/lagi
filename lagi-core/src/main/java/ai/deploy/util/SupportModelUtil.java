package ai.deploy.util;

import ai.deploy.pojo.SupportModel;
import ai.deploy.pojo.SupportModels;
import ai.utils.JsonFileLoadUtil;

import java.util.Map;
import java.util.stream.Collectors;

public class SupportModelUtil {

    public static SupportModels getModels() {
        return JsonFileLoadUtil.readWordLRulesList("/model_path.json", SupportModels.class);
    }

    public static Map<String, SupportModel> getModelSupportMap() {
        SupportModels models = getModels();
        return models.getModels().stream().collect(Collectors.toMap(SupportModel::getName, a->a));
    }

}
