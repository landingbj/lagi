package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.*;

@Setter
public class BloodTypeCalculationTool extends AbstractTool {

//    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/bloodtype/";
//    private static final String API_ADDRESS = "https://www.apii.cn/api/huaiyun/xx/";
    private static final String API_ADDRESS = "https://api.istero.com/resource/v1/blood/query";

    public BloodTypeCalculationTool() {
        init();
    }

    private void init() {
        name = "blood_type_calculation";
        toolInfo = ToolInfo.builder().name("blood_type_calculation")
                .description("根据父母血型计算子代血型的可能性")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("father").type("string").description("父亲的血型")
                                .build(),
                        ToolArg.builder()
                                .name("mother").type("string").description("母亲的血型")
                                .build()))
                .build();
        register(this);
    }

//    public String calculateBloodType(String father, String mother) {
//        Map<String, String> queryParams = new HashMap<>();
//        queryParams.put("father", father);
//        queryParams.put("mother", mother);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//
//        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);
//
//        if (response == null) {
//            return "查询失败，未获得响应数据";
//        }
//
//        Gson gson = new Gson();
//        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
//        Map<String, Object> responseData = gson.fromJson(response, typeResponse);
//
//        if (responseData == null || responseData.get("code") == null) {
//            return "查询失败，返回数据无效";
//        }
//
//        Object codeObj = responseData.get("code");
//        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
//            return "查询失败，返回状态不正常";
//        }
//
//        String fatherBloodType = (String) responseData.get("father");
//        String motherBloodType = (String) responseData.get("mother");
//        String possibleBloodTypes = (String) responseData.get("possible");
//        String impossibleBloodTypes = (String) responseData.get("impossible");
//
//        return String.format("父亲血型: %s\n母亲血型: %s\n子代可能血型: %s\n子代不可能血型: %s",
//                fatherBloodType != null ? fatherBloodType : "无数据",
//                motherBloodType != null ? motherBloodType : "无数据",
//                possibleBloodTypes != null ? possibleBloodTypes : "无数据",
//                impossibleBloodTypes != null ? impossibleBloodTypes : "无数据");
//    }


    /**
     * 根据父母的血型计算子代可能的血型及其概率
     * @param father 父亲的血型，允许的值："A", "B", "AB", "O"
     * @param mother 母亲的血型，允许的值："A", "B", "AB", "O"
     * @return 格式化的字符串，包含可能的血型及其概率
     */
    public static String calculateBloodType(String father, String mother) {
        // 验证输入的血型是否有效
        validateBloodType(father);
        validateBloodType(mother);

        // 获取父母可能的基因型
        List<String> fatherGenotypes = getGenotypes(father);
        List<String> motherGenotypes = getGenotypes(mother);

        // 计算所有可能的子代基因型组合
        List<String> allChildGenotypes = new ArrayList<>();
        for (String fGenotype : fatherGenotypes) {
            for (String mGenotype : motherGenotypes) {
                // 对每种基因型组合，计算子代可能的基因型
                List<String> childGenotypes = getChildGenotypes(fGenotype, mGenotype);
                allChildGenotypes.addAll(childGenotypes);
            }
        }

        // 统计每种基因型的出现次数
        Map<String, Integer> genotypeCount = new HashMap<>();
        for (String genotype : allChildGenotypes) {
            genotypeCount.put(genotype, genotypeCount.getOrDefault(genotype, 0) + 1);
        }

        // 计算每种表型（血型）的概率
        Map<String, Double> phenotypeProbability = new HashMap<>();
        for (Map.Entry<String, Integer> entry : genotypeCount.entrySet()) {
            String genotype = entry.getKey();
            int count = entry.getValue();
            String phenotype = getPhenotype(genotype);
            double probability = (double) count / allChildGenotypes.size();
            phenotypeProbability.put(phenotype, phenotypeProbability.getOrDefault(phenotype, 0.0) + probability);
        }

        // 按指定格式构建返回字符串
        StringBuilder result = new StringBuilder();
        List<String> bloodTypes = Arrays.asList("AB", "A", "B", "O");
        for (String bloodType : bloodTypes) {
            if (phenotypeProbability.containsKey(bloodType)) {
                result.append(String.format("后代血型为%s的概率为 %.2f%%\n",
                        bloodType, phenotypeProbability.get(bloodType) * 100));
            }
        }
        return result.toString();
    }

    /**
     * 验证血型输入是否有效
     * @param bloodType 血型
     */
    private static void validateBloodType(String bloodType) {
        if (!Arrays.asList("A", "B", "AB", "O").contains(bloodType)) {
            throw new IllegalArgumentException("无效的血型: " + bloodType);
        }
    }

    /**
     * 根据血型获取可能的基因型
     * @param bloodType 血型
     * @return 可能的基因型列表
     */
    private static List<String> getGenotypes(String bloodType) {
        switch (bloodType) {
            case "A":
                return Arrays.asList("AA", "AO");
            case "B":
                return Arrays.asList("BB", "BO");
            case "AB":
                return Collections.singletonList("AB");
            case "O":
                return Collections.singletonList("OO");
            default:
                throw new IllegalArgumentException("无效的血型: " + bloodType);
        }
    }

    /**
     * 根据父母的基因型计算子代可能的基因型
     * @param fatherGenotype 父亲的基因型
     * @param motherGenotype 母亲的基因型
     * @return 子代可能的基因型列表
     */
    private static List<String> getChildGenotypes(String fatherGenotype, String motherGenotype) {
        List<String> result = new ArrayList<>();
        for (char f : new char[]{fatherGenotype.charAt(0), fatherGenotype.charAt(1)}) {
            for (char m : new char[]{motherGenotype.charAt(0), motherGenotype.charAt(1)}) {
                // 确保基因型按字母顺序排列
                String genotype = f <= m ? "" + f + m : "" + m + f;
                result.add(genotype);
            }
        }
        return result;
    }

    /**
     * 根据基因型确定血型
     * @param genotype 基因型
     * @return 血型
     */
    private static String getPhenotype(String genotype) {
        if (genotype.contains("A") && genotype.contains("B")) {
            return "AB";
        } else if (genotype.contains("A")) {
            return "A";
        } else if (genotype.contains("B")) {
            return "B";
        } else {
            return "O";
        }
    }

    @Override
    public String apply(Map<String, Object> args) {
        String father = (String) args.get("father");
        String mother = (String) args.get("mother");
        return calculateBloodType(father, mother);
    }

    public static void main(String[] args) {
        BloodTypeCalculationTool bloodTypeCalculationTool = new BloodTypeCalculationTool();
        String result = bloodTypeCalculationTool.calculateBloodType("O", "O");
        System.out.println(result);
    }
}
