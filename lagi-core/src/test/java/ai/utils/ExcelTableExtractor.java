package ai.utils;

import ai.openai.pojo.ChatCompletionResult;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.io.*;
import java.util.*;

public class ExcelTableExtractor {


    public void splitDoc(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        HWPFDocument document = new HWPFDocument(fis);
        Range range = document.getRange();
        StyleSheet styleSheet = document.getStyleSheet();
        List<String> paragraphs = new ArrayList<>();
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph paragraph = range.getParagraph(i);
            System.out.println(isTitle(paragraph, styleSheet));
            if (isTitle(paragraph, styleSheet)) {
                System.out.println(paragraph.text());
            }
//            System.out.println("isTitle " + isTitle(paragraph, styleSheet) + " \t " + paragraph.text());
            paragraphs.add(range.getParagraph(i).text());
        }
        document.close();
        fis.close();
    }

    public static boolean isTitle(Paragraph paragraph, StyleSheet styleSheet) {
        // 方法一：检查段落样式
        short styleIndex = paragraph.getStyleIndex();
        String styleName = styleSheet.getStyleDescription(styleIndex).getName();
        if (styleName != null && (styleName.startsWith("Heading") || styleName.startsWith("标题"))) {
            return true;
        }
        CharacterRun characterRun = paragraph.getCharacterRun(0);
        if (characterRun != null
                && characterRun.isBold()
                && characterRun.getFontSize() > 14) {
            return true;
        }
        return false;
    }


    public static boolean isBold(Paragraph paragraph, StyleSheet styleSheet) {
        // 方法一：检查段落样式
        short styleIndex = paragraph.getStyleIndex();
        String styleName = styleSheet.getStyleDescription(styleIndex).getName();
        if (styleName != null && (styleName.startsWith("Heading") || styleName.startsWith("标题"))) {
            return true;
        }
        CharacterRun characterRun = paragraph.getCharacterRun(0);
        if (characterRun != null
                && characterRun.isBold()
                && characterRun.getFontSize() > 14) {
            return true;
        }
        return false;
    }

    public List<String> loadDocx(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        XWPFDocument document = new XWPFDocument(fis);
        List<IBodyElement> bodyElements = document.getBodyElements();
        int count = 0;
        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                String text = paragraph.getText();
                List<XWPFPicture> imagesInParagraph = getImagesInParagraph(paragraph);
                if (!imagesInParagraph.isEmpty()) {
                    count += imagesInParagraph.size();
                }

            } else if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        List<XWPFParagraph> paragraphs = cell.getParagraphs();
                        for (XWPFParagraph paragraph : paragraphs) {
                            String text = paragraph.getText();
                            List<XWPFPicture> imagesInParagraph = getImagesInParagraph(paragraph);
                            if (!imagesInParagraph.isEmpty()) {
                                count += imagesInParagraph.size();
                            }
                        }
                    }
                }
            } else if (bodyElement instanceof XWPFSDT) {
                XWPFSDT xwpfsdt = (XWPFSDT) bodyElement;
                XWPFDocument document1 = xwpfsdt.getDocument();
                List<XWPFParagraph> paragraphs = document1.getParagraphs();
                for (XWPFParagraph paragraph : paragraphs) {
                    String text = paragraph.getText();
                    List<XWPFPicture> imagesInParagraph = getImagesInParagraph(paragraph);
                    if (!imagesInParagraph.isEmpty()) {
                        count += imagesInParagraph.size();
                    }
                }
            }
        }
        System.out.println(count);
        System.out.println(bodyElements);
        document.close();
        fis.close();
        return Collections.emptyList();
    }

    public static List<XWPFPicture> getImagesInParagraph(XWPFParagraph paragraph) {
        List<XWPFPicture> pictures = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()) {
            pictures.addAll(run.getEmbeddedPictures());
        }
        return pictures;
    }

    public static boolean isTitle(XWPFParagraph paragraph) {
        // 方法一：检查段落样式
        String styleId = paragraph.getStyle();
        String styleName = null;
        if (styleId != null) {
            XWPFStyles styles = paragraph.getDocument().getStyles();
            XWPFStyle style = styles.getStyle(styleId);
            if (style != null) {
                styleName = style.getName();
            }
        }

        if (styleName != null && (styleName.startsWith("Heading") || styleName.startsWith("标题"))) {
            return true;
        }

        // 方法二：检查大纲级别
        CTPPr pPr = paragraph.getCTP().getPPr();
        if (pPr != null && pPr.getOutlineLvl() != null) {
            int outlineLevel = pPr.getOutlineLvl().getVal().intValue();
            return outlineLevel >= 0 && outlineLevel <= 8;
        }

        return false;
    }

    private void savePart(String outputDir, int partNumber, String content) throws IOException {
        String fileName = outputDir + "/part_" + partNumber + ".txt";
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content.getBytes());
        fos.close();
    }

    public static void main(String[] args) {
        String s = "{\"source\":\"lottery_results\",  \"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"彩票开奖结果如下：\n" +
                "|期号|开奖时间|销售金额|中奖号码|追加号码| \n" +
                "|-----|-----|-----|-----|-----| \n" +
                "|2025041|2025-04-15 星期二|3.77亿|06 10 17 19 25 31|06|\"}}]}";
//        ChatCompletionResult bean = JSONUtil.toBean(s, ChatCompletionResult.class);
        Gson gson = new Gson();
        ChatCompletionResult chatCompletionResult = gson.fromJson(s, ChatCompletionResult.class);
        System.out.println(chatCompletionResult);

    }
}