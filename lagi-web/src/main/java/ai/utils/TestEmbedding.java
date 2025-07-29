package ai.utils;

import java.util.Arrays;
import java.util.List;

import ai.embedding.EmbeddingsUtil;

public class TestEmbedding {
    public static void main(String[] args) {
//        HikariDS.test();
        testRerank();
//        testEmbeddings();
    }

    public static void testRerank() {
        String prompt = "人工智能机器学习算法";
        List<String> chunks = Arrays.asList(
                "深度学习神经网络算法研究",
                "传统数据库系统设计原理",
                "机器学习分类算法实现",
                "前端用户界面开发技术",
                "人工智能自然语言处理",
                "云计算分布式系统架构"
        );

        System.out.println("Input prompt: " + prompt);
        System.out.println("Input chunks:");
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println(i + ": " + chunks.get(i));
        }
        System.out.println();

        List<String> rankedChunks = EmbeddingsUtil.rerank(prompt, chunks);

        System.out.println("Ranked results:");
        for (int i = 0; i < rankedChunks.size(); i++) {
            String chunk = rankedChunks.get(i);
            System.out.println(i + ": " + chunk);
        }
    }

    private static void testEmbeddings() {
        String[] strs = {
                "人工智能，指由人制造出来的机器所表现出来的智能。",
                "知识图谱因其自身的图展示、图挖掘、图模型计算优势，可帮助金融从业人员进行业务场景的分析与决策，有利于建立客户画像、进行精准营销获客，发现信用卡套现、资金挪用等行为，更好的表达、分析金融业务场景的交易全貌，从而成为行业的宠儿。金融领域数据是典型的具有”4V”特征的大数据（数量海量Volume、多结构多维度Variety、价值巨大Value、及时性要求Velocity）。进一步，金融领域是最能把数据变现的行业。金融业类别业非常广，大类主要包括：银行类、投资类、保险类等。再小粒度可分为：货币、债券、基金、信托等资管计划、要素市场、征信贷款等。知识图谱在金融领域的应用主要包括：风控、征信、审计、反欺诈、数据分析、自动化报告等。",
                "社会保障卡补领的办理流程是怎样的？",
                "社会保障卡补领的办理流程包括五个步骤：1. 申报/收件（0个工作日），申请人通过网站、APP或微信公众号申请，或携带身份证件到服务网点；2. 受理（0个工作日），综窗人员审核提交的证件；3. 决定（0个工作日），首席代表或部门负责人决定是否补卡；4. 制证（≤4个工作日），制证人员制作社会保障卡；5. 发证（0个工作日），综窗人员发放新卡。送达方式为窗口领取或邮寄。",
                "社会保障卡补领的办理流程包括五个步骤： 申报/收件（个工作日），申请人通过网站、或微信公众号申请，或携带身份证件到服务网点； 受理（个工作日），综窗人员审核提交的证件； 决定（个工作日），首席代表或部门负责人决定是否补卡； 制证（≤个工作日），制证人员制作社会保障卡；发证（个工作日），综窗人员发放新卡。送达方式为窗口领取或邮寄。",
        };
        for (String text : strs) {
            List<Double> vector = EmbeddingsUtil.embeddings(text, 16);
            System.out.println(vector.size());
        }

        for (String text : strs) {
            List<Double> vector = EmbeddingsUtil.embeddings(text, 32);
            System.out.println(vector.size());
        }

        for (String text : strs) {
            List<Double> vector = EmbeddingsUtil.embeddings(text, 64);
            System.out.println(vector.size());
        }

    }
}
