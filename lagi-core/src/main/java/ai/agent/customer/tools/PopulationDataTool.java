package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class PopulationDataTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/human/alldata.php";

    public PopulationDataTool() {
        init();
    }

    private void init() {
        name = "population_data";
        toolInfo = ToolInfo.builder().name("population_data")
                .description("这是一个世界人口数据工具")
                .args(Lists.newArrayList())
                .build();
        register(this);
    }

    public String getPopulationData() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = null;
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "获取失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "获取失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "获取失败";
        }

        Map<String, Object> data = (Map<String, Object>) map.get("data");

        String nowPopulation = (String) data.get("now_population");
        String malePopulation = (String) data.get("male");
        String femalePopulation = (String) data.get("female");
        String thisYearHuman = (String) data.get("this_year_human");
        String thisDayHuman = (String) data.get("this_day_human");
        String thisYearDeadHuman = (String) data.get("this_yeardead_human");
        String thisDayDeadHuman = (String) data.get("this_daydead_humun");
        String thisYearNetMigration = (String) data.get("this_yearnet_migration");
        String thisDayNetMigration = (String) data.get("this_daynet_migration");
        String thisYearPopulationGrowth = (String) data.get("this_year_population_growth");
        String thisDayPopulationGrowth = (String) data.get("this_day_population_growth");

        return StrUtil.format(
                "当前世界人口: {}\n男性人口: {}\n女性人口: {}\n今年新增人口: {}\n今天新增人口: {}\n今年死亡人口: {}\n今天死亡人口: {}\n今年净迁移人数: {}\n今天净迁移人数: {}\n今年人口增长: {}\n今天人口增长: {}",
                nowPopulation, malePopulation, femalePopulation, thisYearHuman, thisDayHuman, 
                thisYearDeadHuman, thisDayDeadHuman, thisYearNetMigration, thisDayNetMigration, 
                thisYearPopulationGrowth, thisDayPopulationGrowth);
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getPopulationData();
    }

    public static void main(String[] args) {
        PopulationDataTool populationDataTool = new PopulationDataTool();
        String result = populationDataTool.getPopulationData();
        System.out.println(result);
    }
}
