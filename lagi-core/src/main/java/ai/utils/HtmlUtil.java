package ai.utils;

import ai.llm.service.CompletionsService;
import ai.manager.Html2ContentManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.vector.FileService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class HtmlUtil {
    private static CompletionsService completionService ;

    static {
        if (Html2ContentManager.enable){
            completionService = new CompletionsService(Html2ContentManager.getInstance());
        }
    }
    private static final String PROMPT_TEMPLATE = "请仔细分析提供的网页HTML内容，识别并提取最可能包含文章主体内容的<div>元素的id和class属性。\n" +
            "要求如下：\n" +
//            "1. 如果存在多个候选<div>，优先选择包含丰富文本、段落（<p>）、标题（<h1>-<h6>）等内容的结构化区块。\n" +
            "1. 如果存在多个候选<div>，不要侧边栏与广告，只用与标题主体信息联系最紧密的内容的结构化区块。\n" +
            "2. 若未找到明确的文章容器，请将id和class均设为null。\n" +
            "3. 返回标准JSON格式，仅包含id和class字段，不添加额外说明或解释。\n" +
            "4. 若字段不存在，请返回null而非空字符串。\n" +
            "\n" +
            "输出格式示例：\n" +
            "{\n" +
            "  \"id\": \"article-content\",\n" +
            "  \"class\": \"post-body\"\n" +
            "}\n" +
            "\n" +
            "标题内容如下：\n" +
            "%s \n"+
            "待分析的内容如下：\n" +
            "%s";
    public static String cleanHtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("script, style, comment").remove();
        removeComments(doc);
        removeEmptyTags(doc);
        compressWhitespace(doc);
        return doc.body().html();
    }

    private static void removeEmptyTags(Element element) {
        for (int i = element.childrenSize() - 1; i >= 0; i--) {
            Element child = element.child(i);
            removeEmptyTags(child);
            if (child.text().trim().isEmpty() &&
                    !child.tagName().equalsIgnoreCase("img") &&
                    !child.tagName().equalsIgnoreCase("br")) {
                child.remove();
            }
        }
    }

    private static void compressWhitespace(Element element) {
        for (org.jsoup.nodes.Node child : element.childNodes()) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                textNode.text(textNode.text().replaceAll("\\s+", " ").trim());
            } else if (child instanceof Element) {
                compressWhitespace((Element) child);
            }
        }
    }

    // 块树节点类
    static class BlockNode {
        String tagName;
        String text;
        String path;
        double similarityScore;
        List<BlockNode> children = new ArrayList<>();

        public BlockNode(String tagName, String text, String path) {
            this.tagName = tagName;
            this.text = text;
            this.path = path;
        }

        @Override
        public String toString() {
            return "{" + tagName + ": " + text.substring(0, Math.min(20, text.length())) + "...}";
        }
    }

    // 递归删除注释节点
    private static void removeComments(Element element) {
        List<org.jsoup.nodes.Node> nodesToRemove = new ArrayList<>();
        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (node instanceof Comment) {
                nodesToRemove.add(node);
            } else if (node instanceof Element) {
                removeComments((Element) node); // 递归处理子元素
            }
        }

        // 使用 Node.remove() 来安全移除注释节点
        for (org.jsoup.nodes.Node node : nodesToRemove) {
            node.remove();
        }
    }
    public static String chat(String msg, List<ChatMessage> messages) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(10000000);
        chatCompletionRequest.setCategory("default_text_parse");
        if (messages == null){
            messages = new ArrayList<>();
        }
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(msg);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        chatCompletionRequest.setStream(false);
        ChatCompletionResult result = completionService.completions(chatCompletionRequest);
        return result.getChoices().get(0).getMessage().getContent();
    }

    public static class ArticleDivInfo {
        private String id;
        private String clazz;

        // 必须提供无参构造函数（如果自定义了其他构造函数）
        public ArticleDivInfo() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        @JsonProperty("class")
        public String getClassName() { return clazz; }

        @JsonProperty("class")
        public void setClassName(String clazz) { this.clazz = clazz; }
    }

    public static String html2md(String html) {
        String title = "";
        String markdown = "";
        try {
            Document doc = Jsoup.parse(html);
            System.out.println("标题: " + doc.title());
            html= doc.html();
            title = doc.title();
            String cleanedHtml = cleanHtml(html);
            try {
                if (completionService != null) {
                    String prompt = String.format(PROMPT_TEMPLATE, title, cleanedHtml);
                    String jsonInput = chat(prompt, null).replaceAll("```json|```", "").trim();
                    System.out.println("模型辅助输入：" + jsonInput);
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArticleDivInfo articleDivInfo = objectMapper.readValue(jsonInput, ArticleDivInfo.class);

                    if (articleDivInfo.getId() != null){
                        Element div = doc.getElementById(articleDivInfo.getId());
                        if (div != null) {
                            cleanedHtml = div.html();
                        }
                    }else if (articleDivInfo.getClassName() != null){
                        Elements divs = doc.getElementsByClass(articleDivInfo.getClassName());
                        if (!divs.isEmpty()) {
                            cleanedHtml = doc.selectFirst("." + articleDivInfo.getClassName()).html();
                        }
                    }
                    cleanedHtml = cleanHtml(cleanedHtml);
                }
//                System.out.println("模型辅助清理后的HTML: " + cleanedHtml);
            }catch (Exception e){
                System.out.println("模型辅助清理错误：" + e.getMessage());
            }
            com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter converter = com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter.builder().build();
            markdown = converter.convert("<h1>"+title+"</h1>"+cleanedHtml.replaceAll("!\\[.*?\\]\\(.*?\\)", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return markdown;
    }

    public static String htmlUrl2md(String url) {
        String html = "";
        String title = "";
        String markdown = "";
        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println("标题: " + doc.title());
            html= doc.html();
            title = doc.title();
            String cleanedHtml = cleanHtml(html);
            try {
                if (completionService != null) {
                    String prompt = String.format(PROMPT_TEMPLATE, title, cleanedHtml);
                    String jsonInput = chat(prompt, null).replaceAll("```json|```", "").trim();
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArticleDivInfo articleDivInfo = objectMapper.readValue(jsonInput, ArticleDivInfo.class);

                    if (articleDivInfo.getId() != null){
                        Element div = doc.getElementById(articleDivInfo.getId());
                        if (div != null) {
                            cleanedHtml = div.html();
                        }
                    }else if (articleDivInfo.getClassName() != null){
                        Elements divs = doc.getElementsByClass(articleDivInfo.getClassName());
                        if (!divs.isEmpty()) {
                            cleanedHtml = doc.selectFirst("." + articleDivInfo.getClassName()).html();
                        }
                    }
                    cleanedHtml = cleanHtml(cleanedHtml);
                }
                System.out.println("模型辅助清理后的HTML: " + cleanedHtml);
            }catch (Exception e){
                System.out.println("模型辅助清理错误：" + e.getMessage());
            }
            FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
            markdown = converter.convert("<h1>"+title+"</h1>"+cleanedHtml.replaceAll("!\\[.*?\\]\\(.*?\\)", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return markdown;
    }
    public static void main(String[] args) throws IOException {
//        Document doc = Jsoup.connect("https://www.cssn.cn/skgz/202410/t20241011_5790125.shtml").get();
//        System.out.println("详细内容："+doc.html());

        String md = html2md(FileService.getString("C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\测试页面.html"));
        System.out.println(md);
    }
}
