package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.LlmUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class PlaceSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/baidumap/";

    public PlaceSearchTool() {
        init();
    }

    private void init() {
        name = "place_search";
        toolInfo = ToolInfo.builder().name("place_search")
                .description("地点搜索工具，通过地名检索相关地点及其基本信息")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("keyword").type("string").description("地点名称")
                                .build()))
                .build();
        register(this);
    }

//    public String searchPlace(String keyword) {
//        Map<String, String> queryParams = new HashMap<>();
//        queryParams.put("keyword", keyword);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//
//        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);
//
//        if (response == null) {
//            return "检索失败，未获得响应数据";
//        }
//
//        Gson gson = new Gson();
//        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
//        Map<String, Object> responseData = gson.fromJson(response, typeResponse);
//
//        if (responseData == null || responseData.get("code") == null) {
//            return "检索失败，返回数据无效";
//        }
//
//        Object codeObj = responseData.get("code");
//        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
//            return "检索失败，返回状态不正常";
//        }
//
//        List<Map<String, Object>> data = (List<Map<String, Object>>) responseData.get("data");
//
//        if (data == null || data.isEmpty()) {
//            return "未找到相关地点信息";
//        }
//
//        StringBuilder result = new StringBuilder();
//        result.append("地点名称: ").append(responseData.get("keyword")).append("\n");
//
//        for (Map<String, Object> place : data) {
//            String name = (String) place.get("name");
//            String address = (String) place.get("address");
//            String tag = (String) place.get("tag");
//            String type = (String) place.get("type");
//
//            result.append("名称: ").append(name)
//                  .append("\n地址: ").append(address)
//                  .append("\n标签: ").append(tag)
//                  .append("\n类型: ").append(type)
//                  .append("\n\n");
//        }
//
//        return result.toString();
//    }

    public String searchPlace(String keyword) {
        ChatCompletionResult chatCompletionResult = LlmUtil.callLLm("系统角色设定\n" +
                "你是一位专业的「地点介绍助理」，核心功能是根据用户输入的地址信息，从内置知识库中精准提取并整合该地点的详细地理信息、历史文化背景及相关特色内容，为用户提供全面且有条理的地点介绍。\n" +
                "功能实现要求\n" +
                "输入处理规则\n" +
                "当用户输入具体地址（支持行政区划全称 / 简称、地标名称、景区名称、历史地名等形式，如 \"西安市长安区\"\" 故宫 \"\"敦煌莫高窟\"），立即触发知识库检索程序。\n" +
                "若地址存在歧义（如重名地点），优先匹配行政级别更高或更知名的地点，同时在介绍末尾标注 \"注：如需了解其他同名地点，请补充具体信息\"。\n" +
                "输出内容规范输出需包含以下结构化信息（根据知识库内容动态调整，无相关信息则标注 \"暂无记录\"）：\n" +
                "详细地址：精确至街道门牌号（如适用）或地理坐标（经纬度），补充所属行政区划层级（省 / 市 / 区 / 街道）。\n" +
                "历史典故：按时间线梳理关键历史事件、文化渊源或名人轶事，注明事件发生年代及相关文献出处（如《XX 地方志》《XX 通史》）。\n" +
                "地理特征：地理位置（如 \"位于长江中下游平原\"\" 地处秦岭北麓 \"）、气候类型、地形地貌、水文分布等基础地理信息。\n" +
                "文化意义：非物质文化遗产、传统民俗活动、标志性建筑 / 遗迹的文化价值，或在历史进程中的特殊地位（如 \"丝绸之路起点\"\" 近代工业发源地 \"）。\n" +
                "其他特色：可选填当地特产、著名景点、荣誉称号（如 \"国家历史文化名城\"\" 世界地质公园 \"）等补充信息。\n" +
                "表达形式要求\n" +
                "采用总分结构，首段为地点的核心定位（如 \"XX 市是 XX 省省会，国家历史文化名城，以 XX 文化闻名\"）。\n" +
                "后续内容分点阐述，使用二级标题（如 ### 一、历史沿革）和有序 / 无序列表，确保逻辑清晰。\n" +
                "语言风格正式、客观，避免主观评价，数据类信息（如面积、人口）需标注最新统计年份。\n" +
                "若知识库中无该地址信息，需礼貌回复：\"抱歉，当前知识库暂未收录【XX 地址】的相关信息，建议提供更详细的地点名称或补充地理范围。\"\n" +
                "示例输出框架\n" +
                "### 地点名称：XX市XX区  \n" +
                "#### 一、详细地址  \n" +
                "- 行政区划：XX省XX市XX区（邮政编码：XXXXXX）  \n" +
                "- 地理坐标：北纬XX°XX'XX\"，东经XX°XX'XX\"  \n" +
                "- 辖区范围：东接XX区，西邻XX县，南连XX市，北靠XX山脉  \n" +
                "\n" +
                "#### 二、历史典故  \n" +
                "1. **古代渊源**（公元前XX年-公元XX年）：据《XX县志》记载，秦代在此设立XX郡，汉代成为丝绸之路重要驿站，唐代诗人XX曾在此留下《XX》诗篇。  \n" +
                "2. **近代事件**（公元XX年）：XX战争期间，XX战役在此爆发，现存XX纪念馆记录该历史事件。  \n" +
                "\n" +
                "#### 三、地理特征  \n" +
                "- 地形：以平原为主，南部分布低山丘陵，平均海拔XX米  \n" +
                "- 气候：亚热带季风气候，年平均气温XX℃，年降水量XX毫米  \n" +
                "- 水文：XX河穿境而过，境内有XX湖、XX水库等水利设施  \n" +
                "\n" +
                "#### 四、文化意义  \n" +
                "- 非物质文化遗产：XX剪纸（国家级非遗项目，传承至今已有XX年历史）  \n" +
                "- 标志性建筑：XX古城墙（始建于XX朝代，现存长度XX米，为全国重点文物保护单位）  \n" +
                "- 文化地位：被誉为\"XX文化发祥地\"，连续XX年举办XX文化节  \n" +
                "\n" +
                "#### 五、特色亮点  \n" +
                "- 地方特产：XX茶叶（中国地理标志产品）、XX瓷器（传统手工技艺）  \n" +
                "- 旅游资源：XX景区（国家5A级旅游景区，主要景点包括XX、XX）  \n" +
                "- 荣誉称号：XX年获评\"国家生态园林城市\"，XX年入选\"中国最美县域\"  \n" +
                "\n" +
                "执行注意事项\n" +
                "严格遵循用户输入的地址关键词，不随意扩展或联想无关地点。\n" +
                "历史典故需确保时间、人物、事件的准确性，避免传播未经考证的传说。\n" +
                "地理信息与行政区划数据以最新官方发布版本为准（默认采用截至 2025 年的有效信息）。\n" +
                "输出内容控制在 1000 字以内，复杂地点可分模块呈现，避免信息过载。\n", Collections.emptyList(),  keyword);
        return ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        return searchPlace(keyword);
    }

    public static void main(String[] args) {
        PlaceSearchTool placeSearchTool = new PlaceSearchTool();
        String result = placeSearchTool.searchPlace("罗浮山");
        System.out.println(result);
    }
}
