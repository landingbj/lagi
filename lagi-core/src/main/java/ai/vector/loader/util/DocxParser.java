package ai.vector.loader.util;

import ai.vector.loader.pojo.Document;
import ai.vector.loader.pojo.DocumentParagraph;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DocxParser {

    public static boolean isTitle(XWPFParagraph paragraph) {

        String styleId = paragraph.getStyle();
        String styleName = null;
        if (styleId != null) {
            XWPFStyles styles = paragraph.getDocument().getStyles();
            XWPFStyle style = styles.getStyle(styleId);
            if (style != null) {
                styleName = style.getName();
            }
        }

        if(styleName != null) {
            styleName = styleName.toLowerCase();
            if(styleName.startsWith("heading") || styleName.startsWith("标题")) {
                return true;
            }
        }

        CTPPr pPr = paragraph.getCTP().getPPr();
        if (pPr != null && pPr.getOutlineLvl() != null) {
            int outlineLevel = pPr.getOutlineLvl().getVal().intValue();
            return outlineLevel >= 0 && outlineLevel <= 8;
        }
        return false;
    }

    public static Document loadDocx(String filePath) throws IOException {
        Document res = null;
        FileInputStream fis = new FileInputStream(filePath);
        XWPFDocument document = new XWPFDocument(fis);
        List<IBodyElement> bodyElements = document.getBodyElements();
        res = Document.builder()
                .type(2)
                .paragraphs(new ArrayList<>())
                .fileName(new File(filePath).getName())
                .titleCount(0)
                .build();
        for (IBodyElement bodyElement : bodyElements) {
            DocumentParagraph documentParagraph = new DocumentParagraph();
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                String text = paragraph.getText();
                if(StrUtil.isBlank(text)) {
                    List<String> imagesInParagraph = getImagesInParagraph(paragraph);
                    if(!imagesInParagraph.isEmpty()) {
                        documentParagraph.setType("image");
                        documentParagraph.setImages(imagesInParagraph);
                    } else {
                        documentParagraph.setType("txt");
                        documentParagraph.setSubType("txt");
                        documentParagraph.setTxt("\n");
                    }
                } else {
                    boolean title = isTitle(paragraph);
                    res.setTitleCount(res.getTitleCount() + 1);
                    documentParagraph.setType("txt");
                    documentParagraph.setSubType(title ? "title" : "txt");
                    documentParagraph.setTxt(text + "\n");
                }

            } else if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                documentParagraph.setType("table");
                List<List<String>> tb = new ArrayList<>();
                for (XWPFTableRow row : table.getRows()) {
                    List<String> tr = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        List<XWPFParagraph> paragraphs = cell.getParagraphs();
                        for (XWPFParagraph paragraph : paragraphs) {
                            String text = paragraph.getText();
                            tr.add(text);
                            List<String> imagesInParagraph = getImagesInParagraph(paragraph);
                            if (!imagesInParagraph.isEmpty()) {
                                DocumentParagraph imageParagraph = new DocumentParagraph();
                                imageParagraph.setType("image");
                                imageParagraph.setImages(imagesInParagraph);
                                res.getParagraphs().add(imageParagraph);
                            }
                        }
                    }
                    tb.add(tr);
                }
                documentParagraph.setTable(tb);
            } else if (bodyElement instanceof XWPFSDT) {
//                XWPFSDT xwpfsdt = (XWPFSDT) bodyElement;
//                XWPFDocument document1 = xwpfsdt.getDocument();
//                List<XWPFParagraph> paragraphs = document1.getParagraphs();
//                System.out.println("b");
//                for (XWPFParagraph paragraph : paragraphs) {
//                    documentParagraph = new DocumentParagraph();
//                    String text = paragraph.getText();
//                    System.out.println(text);
//                    if(StrUtil.isBlank(text)) {
//                        List<String> imagesInParagraph = getImagesInParagraph(paragraph);
//                        if(!imagesInParagraph.isEmpty()) {
//                            count += imagesInParagraph.size();
////                            System.out.println("a:"  + count);
//                            documentParagraph.setType("image");
//                            documentParagraph.setImages(imagesInParagraph);
//                        } else {
//                            documentParagraph.setType("txt");
//                            documentParagraph.setSubType("txt");
//                            documentParagraph.setTxt("\n");
//                        }
//                    } else {
//                        boolean title = isTitle(paragraph);
//                        documentParagraph.setType("txt");
//                        documentParagraph.setSubType(title ? "title" : "txt");
//                        documentParagraph.setTxt(text + "\n");
//                    }
//                    res.getParagraphs().add(documentParagraph);
//                }
//                continue;
            }
            res.getParagraphs().add(documentParagraph);
        }
//        List<XWPFPictureData> allPictures = document.getAllPictures();
        document.close();
        fis.close();
        return res;
    }

    public static List<String> getImagesInParagraph(XWPFParagraph paragraph) {
        List<String> pictures = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()) {
            List<XWPFPicture> embeddedPictures = run.getEmbeddedPictures();
            for (XWPFPicture picture : embeddedPictures) {
                pictures.add(Base64.getEncoder().encodeToString((picture.getPictureData().getData())));
            }
        }
        return pictures;
    }


}
