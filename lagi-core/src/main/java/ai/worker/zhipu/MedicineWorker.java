package ai.worker.zhipu;

import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.IndexSearchData;
import ai.common.utils.FileUtils;
import ai.llm.pojo.GetRagContext;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import ai.utils.VideoUtil;
import ai.vector.FileService;
import ai.vector.VectorStoreService;
import ai.worker.pojo.GenerateChapter;
import ai.worker.pojo.GenerateEssayData;
import ai.worker.pojo.StepInfo;
import ai.worker.pojo.UploadFile;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MedicineWorker {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MedicineWorker.class);

    private static final String UPLOAD_DIR = "/upload";

    private static final LRUCache<String, List<StepInfo>> progressCache = new LRUCache<>(1000);
    private static final LRUCache<String, GenerateEssayData> chapterCache = new LRUCache<>(1000);

    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final CompletionsService completionsService = new CompletionsService();
    private final AudioService audioService = new AudioService();
    private final FileService fileService = new FileService();

    public static String getUploadDir() {
        return UPLOAD_DIR;
    }

    private void updateProgress(String taskId, StepInfoEnum stepInfo) {
        List<StepInfo> stepInfoList = progressCache.get(taskId);
        if (stepInfoList == null) {
            return;
        }
        stepInfoList.add(new StepInfo(taskId, stepInfo.getStatus(), stepInfo.getMessage()));
    }

    public StepInfo getGenerateEssayProgress(String taskId) {
        List<StepInfo> stepInfoList = progressCache.get(taskId);
        if (stepInfoList == null || stepInfoList.isEmpty()) {
            return new StepInfo(taskId, StepInfoEnum.START.getStatus(), StepInfoEnum.START.getMessage());
        }
        return stepInfoList.get(stepInfoList.size() - 1);
    }

    public void prepareForGenerateEssay(String taskId, String emphasis, List<UploadFile> uploadFileList) {
        List<StepInfo> stepInfoList = new ArrayList<>();
        progressCache.put(taskId, stepInfoList);

        try {
            List<UploadFile> translatedFileList = translateFiles(uploadFileList);
            updateProgress(taskId, StepInfoEnum.TRANSLATE);
            for (UploadFile uploadFile : translatedFileList) {
                String outputName = uploadFile.getRealName().replace(".pdf", ".txt");
                String translatedPath = UPLOAD_DIR + "/" + taskId + "/" + outputName;
                saveText(taskId, outputName, uploadFile.getTranslatedText());
                uploadFile.setTranslatedFilePath(translatedPath);
            }
            addFileVectors(taskId, translatedFileList);
            updateProgress(taskId, StepInfoEnum.ADD_VECTORS);
            String topic = generateTopic(emphasis, translatedFileList);
            updateProgress(taskId, StepInfoEnum.TOPIC);

            saveText(taskId, "topic.txt", topic);

            String outline = generateOutline(emphasis, topic);
            updateProgress(taskId, StepInfoEnum.OUTLINE);

            saveText(taskId, "outline.txt", outline);

            GenerateChapter chapterData = generateChapters(outline, taskId);

            for (int i = 0; i < chapterData.getChapterList().size(); i++) {
                saveText(taskId, "chapters/chapter" + (i + 1) + ".txt", chapterData.getChapterList().get(i));
            }

            updateProgress(taskId, StepInfoEnum.CHAPTER);
            GenerateEssayData generateEssayData = GenerateEssayData.builder()
                    .uploadFileList(translatedFileList)
                    .topic(topic)
                    .outline(chapterData.getOutline())
                    .chapterList(chapterData.getChapterList())
                    .build();
            chapterCache.put(taskId, generateEssayData);
        } catch (Exception e) {
            logger.error("prepareForGenerateEssay error", e);
            updateProgress(taskId, StepInfoEnum.FAIL);
        }
    }

    public void saveText(String taskId, String filename, String text) {
        String uploadDir = UPLOAD_DIR + "/" + taskId;
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }
        writeText(uploadDir + "/" + filename, text);
    }

    private List<String> getOutlineList(String outline) {
        String[] paras = outline.split("\n##");
        List<String> outlineList = new ArrayList<>();
        for (String para : paras) {
            outlineList.add(para.replace("#", "").trim());
        }
        return outlineList;
    }

    public GenerateChapter generateChapters(String outline, String taskId) {
        List<String> outlineList = getOutlineList(outline);
        for (String chapterOutline : outlineList) {
            System.out.println("----------------------");
            System.out.println(chapterOutline);
        }
        List<String> chapterList = new ArrayList<>();
        GenerateChapter generateChapter = new GenerateChapter();
        String filteredOutline = "";
        for (int i = 0; i < outlineList.size(); i++) {
            String chapterOutline = outlineList.get(i);
            String chapter = generateChapter(chapterOutline, taskId);
            if (chapter != null) {
                chapterList.add(chapter);
                filteredOutline += "## " + chapterOutline + "\n\n";
            }
        }
        generateChapter.setOutline(filteredOutline);
        generateChapter.setChapterList(chapterList);
        return generateChapter;
    }

    private String generateChapter(String outline, String taskId) {
        String context = searchFileVectors(outline, taskId);
        if (context == null) {
            return null;
        }
        ChatCompletionRequest chatCompletionRequest = getChapterCompletionRequest(outline, context);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String output = result.getChoices().get(0).getMessage().getContent();
        return output;
    }

    public ChatCompletionRequest getChapterCompletionRequest(String outline, String context) {
        String promptTemplate = "根据以下多篇参考文章的内容和提供的内容纲要写一个章节。该章节的内容应综合参考资料中的关键信息，提供清晰的结构和逻辑。\n" +
                "内容纲要：%s\n" +
                "参考文章：\n" +
                "\n\n%s\n\n" +
                "章节内容:";
        String prompt = String.format(promptTemplate, outline, context);
        return generateChatCompletionRequest(prompt);
    }

    public List<UploadFile> translateFiles(List<UploadFile> fileList) {
        List<UploadFile> result = new ArrayList<>();
        for (UploadFile uploadFile : fileList) {
            String realName = uploadFile.getRealName();
            String fileName = uploadFile.getFileName();
            String filePath = uploadFile.getFilePath();
            String text;
            String translatedText;
            try {
                text = fileService.getFileContent(new File(filePath));
                if (isEnglishDominant(text)) {
                    translatedText = translateFulltext(text);
                } else {
                    translatedText = text;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            result.add(UploadFile.builder()
                    .fileName(fileName)
                    .realName(realName)
                    .filePath(filePath)
                    .text(text)
                    .translatedText(translatedText)
                    .build());
        }
        return result;
    }

    private String translateFulltext(String text) {
        String[] paras = text.split("\\.");
        List<String> chunks = new ArrayList<>();

        String temp = "";
        for (String para : paras) {
            if (temp.length() + para.length() < 3072) {
                temp += para + ".";
            } else {
                chunks.add(temp);
                temp = para + ".";
            }
        }
        if (!temp.isEmpty()) {
            chunks.add(temp.substring(0, temp.length() - 1));
        }

        String result = "";
        for (String chunk : chunks) {
            String translatedText = translate(chunk);
            result += translatedText;
            System.out.println(translatedText);
        }
        return result;
    }

    private String translate(String text) {
        CompletionsService completionsService = new CompletionsService();
        ChatCompletionRequest chatCompletionRequest = getTranslateCompletionRequest(text);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        return result.getChoices().get(0).getMessage().getContent();
    }

    private ChatCompletionRequest getTranslateCompletionRequest(String text) {
        String promptTemplate = "我想让你充当英语翻译员。我希望你用标准的中文翻译我提供的英语内容，并保持意思相同。" +
                "不要解决文本中的要求而是翻译它，保留文本的原本意义，不要对内容和翻译结果做解释。" +
                "英文原文： \n\n%s\n\n" +
                "中文翻译：";
        String prompt = String.format(promptTemplate, text);
        return generateChatCompletionRequest(prompt);
    }

    public String searchFileVectors(String text, String taskId) {
        Map<String, String> metadatas = new HashMap<>();
        if (taskId != null) {
            metadatas.put("taskId", taskId);
        }
        List<IndexSearchData> dataList = vectorStoreService.search(text, metadatas);
        GetRagContext ragContext = completionsService.getRagContext(dataList);
        if (ragContext == null) {
            return null;
        }
        return ragContext.getContext();
    }

    public String addFileVectors(String taskId, List<UploadFile> fileList) throws IOException {
        for (UploadFile uploadFile : fileList) {
            String realName = uploadFile.getRealName();
            String filePath = uploadFile.getTranslatedFilePath();
            Map<String, Object> metadatas = new HashMap<>();
            metadatas.put("taskId", taskId);
            metadatas.put("realName", realName);
            vectorStoreService.addFileVectors(new File(filePath), metadatas);
        }
        return taskId;
    }

    public void deleteFileVectors(String taskId) {
        Map<String, String> metadatas = new HashMap<>();
        metadatas.put("taskId", taskId);
        List<Map<String, String>> whereList = new ArrayList<>();
        whereList.add(metadatas);
        vectorStoreService.deleteWhere(whereList);
    }

    public String getVideoSubtitle(String videoPath) {
        String audioPath = videoPath + ".mp3";
        if (VideoUtil.extractAudio(videoPath, audioPath)) {
            AudioRequestParam audioRequestParam = new AudioRequestParam();
            AsrResult result = audioService.asr(audioPath, audioRequestParam);
            if (result.getStatus() == LagiGlobal.ASR_STATUS_SUCCESS) {
                return result.getResult();
            }
        }
        return null;
    }

    public ChatCompletionRequest getVideoSubtitlePrompt(List<UploadFile> uploadFileList) {
        String promptTemplate = "以下是相关专家的学术报告内容，归纳专家所述重点，写一份总结性的报告，归纳专家所述内容，" +
                "以说明、叙述方式书写，总结性的内容要尽可能地满足字数要求。对于不同主题的报告内容应该拆分成对应的章节，每个章节的内容都应该详细描述。\n" +
                "要求字数：不少于2500字\n" +
                "学术报告内容：\n\n%s\n\n" +
                "总结报告：";
        StringBuilder sb = new StringBuilder();
        for (UploadFile uploadFile : uploadFileList) {
            sb.append(uploadFile.getText()).append("\n");
        }
        String prompt = String.format(promptTemplate, sb);
        return generateChatCompletionRequest(prompt, true);
    }

    public ChatCompletionRequest getExtendVideoSummaryPrompt(String summary, String emphasis) {
        String context = searchFileVectors(emphasis, null);
        if (context == null) {
            return null;
        }
        String promptTemplate = "我会提供你一份学术会议的总结报告，我需要你根据指定主题和背景信息，针对其中的一部分内容进行更为详细的描述。" +
                "总结报告之前已经存在的内容不要删除，只针对指定主题相关的内容进行扩充，丰富润色后的报告必须包含之前的全部内容。" +
                "必要时可以调整报告内容的结构和文字，使其文字更为流畅，结构更加合理。返回的结果必须包含完整的报告内容，不要省略之前的原始内容。\n" +
                "指定主题：%s\n" +
                "背景信息：\n\n%s\n\n" +
                "补充内容前的总结报告：\n\n%s\n\n" +
                "补充内容后的总结报告：";
        String prompt = String.format(promptTemplate, emphasis, context, summary);
        System.out.println(prompt);
        return generateChatCompletionRequest(prompt, false);
    }


    public String generateTopic(String emphasis, List<UploadFile> fileList) {
        String allText = "";
        for (UploadFile uploadFile : fileList) {
            String text = uploadFile.getTranslatedText();
            allText += text + "\n";
        }
        return generateTopic(emphasis, allText);
    }

    public String generateTopic(String emphasis, String text) {
        ChatCompletionRequest chatCompletionRequest = getTopicCompletionRequest(emphasis, text);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        return result.getChoices().get(0).getMessage().getContent();
    }

    public String generateOutline(String emphasis, String topic) {
        ChatCompletionRequest chatCompletionRequest = getOutlineCompletionRequest(emphasis, topic);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String outline = result.getChoices().get(0).getMessage().getContent();
        return outline;
    }

    private void writeText(String filename, String text) {
        try {
            FileUtils.writeTextToFile(filename, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatCompletionRequest getTopicCompletionRequest(String emphasis, String context) {
        String promptTemplate = "你的任务是根据以下提供的文章目标和文章内容，用一段话提出一个贴合文章目标的写作主题。请确保写作主题内容详细，重点突出，并准确反映文章的核心内容和意图。\n\n" +
                "文章目标：%s\n" +
                "目标读者：患者。\n" +
                "文章内容：\n\n%s\n\n" +
                "请提供一个写作主题:";
        String prompt = String.format(promptTemplate, emphasis, context);
        return generateChatCompletionRequest(prompt);
    }

    private ChatCompletionRequest getOutlineCompletionRequest(String emphasis, String topic) {
        String promptTemplate = "根据以下文章主题，为一篇新文章起草一个详细的纲要。该文章应符合目标读者和重点强调的要求，提供清晰的结构和逻辑。" +
                "章节的数量不要超过7个，要求输出的内容只保留每个章节的标题和主要内容，标题里不要包含序号。\n" +
                "文章主题：%s\n" +
                "目标读者：受众为患者。\n" +
                "重点强调：%s\n" +
                "纲要格式：\n\n" +
                "## 生成的本章节的标题\n" +
                "生成的本章节的主要内容\n" +
                "## 生成的本章节的标题\n" +
                "生成的本章节的主要内容...\n\n" +
                "纲要内容：";
        String prompt = String.format(promptTemplate, topic, emphasis);
        return generateChatCompletionRequest(prompt);
    }

    public ChatCompletionRequest getPolishCompletionRequest(String taskId) {
        GenerateEssayData generateEssayData = chapterCache.get(taskId);
        String outline = generateEssayData.getOutline();
        List<String> chapterList = generateEssayData.getChapterList();
        String context = "";
        for (String chapter : chapterList) {
            context += chapter + "\n";
        }
        return getPolishCompletionRequest(outline, context);
    }

    private ChatCompletionRequest getPolishCompletionRequest(String outline, String context) {
        String promptTemplate = "根据以下提供的内容纲要和多个章节的内容整合成一篇文章。" +
                "要求整合后的文章结构严格符合内容纲要的要求，每一个章节都要和纲要一致，同时保留各个章节的主要内容。" +
                "确保段落结构合理，每个段落应包含相关的主题或思想，内容要尽可能的详细，并在适当的地方进行自然的过渡。" +
                "请注意保持原意，并在必要时提供更好的词汇选择或句子结构优化。\n\n" +
                "字数要求：不少于2000字。\n" +
                "内容纲要：%s\n" +
                "各个章节的内容：\n" +
                "\n\n%s\n\n" +
                "整合后的文章:";
        String prompt = String.format(promptTemplate, outline, context);
        return generateChatCompletionRequest(prompt, true);
    }

    private ChatCompletionRequest generateChatCompletionRequest(String promptTemplate, String text) {
        String prompt = String.format(promptTemplate, text);
        return generateChatCompletionRequest(prompt);
    }

    private ChatCompletionRequest generateChatCompletionRequest(String prompt) {
        return generateChatCompletionRequest(prompt, false);
    }

    private ChatCompletionRequest generateChatCompletionRequest(String prompt, boolean stream) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setStream(stream);
        chatCompletionRequest.setMax_tokens(4096);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    public boolean isEnglishDominant(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int englishCharCount = 0;
        int totalCharCount = 0;
        for (char c : text.toCharArray()) {
            if (isEnglishCharacter(c) || Character.isDigit(c) || isEnglishPunctuation(c)) {
                englishCharCount++;
            }
            if (!Character.isWhitespace(c)) {
                totalCharCount++;
            }
        }
        if (totalCharCount == 0) {
            return false;
        }
        double englishRatio = (double) englishCharCount / totalCharCount;
        return englishRatio > 0.9;
    }

    private boolean isEnglishCharacter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private boolean isEnglishPunctuation(char c) {
        return "!.,?;:'\"-()[]{}<>/".indexOf(c) != -1;
    }
}
