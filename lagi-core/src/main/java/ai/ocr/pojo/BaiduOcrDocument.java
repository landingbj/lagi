package ai.ocr.pojo;

import lombok.Data;

import java.util.List;

@Data
public class BaiduOcrDocument {
    private List<ParagraphResult> paragraphs_result;
    private int paragraphs_result_num;
    private int direction;
    private int language;
    private List<WordResult> words_result;
    private long log_id;
    private int words_result_num;

    @Data
    public static class ParagraphResult {
        private List<Integer> words_result_idx;
    }

    @Data
    public static class WordResult {
        private String words;
    }
}



