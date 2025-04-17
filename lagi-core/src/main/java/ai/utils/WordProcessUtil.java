package ai.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class WordProcessUtil {

    private static int processParagraphs(List<XWPFParagraph> paragraphs) {
        int pictureCount = 0;
        for (XWPFParagraph p : paragraphs) {
            for (XWPFRun run : p.getRuns()) {
                if (!run.getEmbeddedPictures().isEmpty()) {
                    pictureCount += run.getEmbeddedPictures().size();
                }
            }
        }
        return pictureCount;
    }

    public void loaderDocx(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream("your_document.docx");
             XWPFDocument document = new XWPFDocument(fis)) {

            int totalFoundPictures = 0;

            // 处理正文部分的图片
            totalFoundPictures += processParagraphs(document.getParagraphs());

            // 处理表格中的图片
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        totalFoundPictures += processParagraphs(cell.getParagraphs());
                    }
                }
            }

            // 处理页眉部分的图片
            for (XWPFHeader header : document.getHeaderList()) {
                totalFoundPictures += processParagraphs(header.getParagraphs());
            }

            // 处理页脚部分的图片
            for (XWPFFooter footer : document.getFooterList()) {
                totalFoundPictures += processParagraphs(footer.getParagraphs());
            }

            // 处理其他可能包含图片的文档部件
            for (POIXMLDocumentPart part : document.getRelations()) {
//                if (part instanceof XWPFRelation && ((XWPFRelation) part).isPicture()) {
//                    totalFoundPictures++;
//                }
            }

            // 直接使用 getAllPictures 方法获取所有图片
            List<XWPFPictureData> allPictures = document.getAllPictures();
            POIXMLDocumentPart parent = allPictures.get(0).getParent();
            System.out.println("Total pictures found by manual traversal: " + totalFoundPictures);
            System.out.println("Total pictures in the document using getAllPictures: " + allPictures.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            WordProcessUtil wordProcessUtil = new WordProcessUtil();
            wordProcessUtil.loaderDocx("C:\\Users\\Administrator\\Desktop\\bushu\\RAG\\测试文档\\1\\安全带未系提示电路.docx");
        } catch (IOException e) {

        }
    }


}
