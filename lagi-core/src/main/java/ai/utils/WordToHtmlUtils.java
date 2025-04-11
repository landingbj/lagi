package ai.utils;

import ai.vector.DocToHtml;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.xwpf.usermodel.*;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class WordToHtmlUtils {

    public static void toHtml(String path){
        try {
            FileInputStream fis = new FileInputStream(path);
            if (path.toLowerCase().endsWith(".doc")) {
                System.out.println(DocToHtml.inputDocPath(path));
            }else if (path.toLowerCase().endsWith(".docx")){
                parseFile3( path, fis);
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * DOCX
     * @param path
     * @param fis
     */
    public static void parseFile3(String path,FileInputStream fis){
        try {
            XWPFDocument document = new XWPFDocument(fis);
            // 遍历文档中的所有元素（段落和表格）
            List<IBodyElement> bodyElements = document.getBodyElements();
            for (IBodyElement element : bodyElements) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String text = paragraph.getText();
                    if (text != null && !text.isEmpty()) {
                        //处理段落或正文
                        System.out.println(paragraph.getText());
                    } else {
                        // 顺序遍历图片
                        paragraph.getIRuns().forEach(run -> {
                            if (run instanceof XWPFRun) {
                                XWPFRun xWPFRun = (XWPFRun) run;
                                //如果片段没有文字，可能该片段为图片
                                if (org.apache.commons.lang3.StringUtils.isEmpty(xWPFRun.text())){
                                    //该片段为图片时
                                    if (xWPFRun.getEmbeddedPictures().size()>0){
                                        for (XWPFPicture picture : xWPFRun.getEmbeddedPictures()) {
                                            XWPFPictureData pictureData = picture.getPictureData();
                                            String base64Image = "<img src='data:image/png;base64," + Base64.getEncoder().encodeToString((pictureData.getData())) + "'/>";
                                            System.out.println(base64Image);
                                        }
                                    }else {
                                        if(xWPFRun.getCTR().xmlText().indexOf("instrText") > 0) {
                                            System.out.println("there is an equation field");
                                        }
                                    }
                                }
                            }else if(run instanceof XWPFFieldRun){ //公式
                                XWPFFieldRun xWPFRun = (XWPFFieldRun) run;


                            }
                        });
                    }
                } else if (element instanceof XWPFTable) {
                    //处理表格
                    XWPFTable table = (XWPFTable) element;
                    StringBuilder tableBuilder = new StringBuilder();
                    tableBuilder.append("<table border ='1' >");
                    for(XWPFTableRow row: table.getRows()){
                        tableBuilder.append("<tr>");
                        for(XWPFTableCell cell:  row.getTableCells()){
                            tableBuilder.append("<td");
                            String text = cell.getText();
                            String color = cell.getColor();
                            color= org.apache.commons.lang3.StringUtils.isEmpty(color) ? "#FFFFFF" : "#"+color;
                            tableBuilder.append(" style='border: 1px solid #000000 ; background-color:"+color+"' >");
                            if (StringUtils.isEmpty(text)){
                                List<XWPFParagraph> cellParagraphs = cell.getParagraphs();
                                for(XWPFParagraph cellsTemp:cellParagraphs){
                                    cellsTemp.getIRuns().forEach(run -> {
                                        if (run instanceof XWPFRun) {
                                            XWPFRun xWPFRun = (XWPFRun) run;
                                            for (XWPFPicture picture : xWPFRun.getEmbeddedPictures()) {
                                                XWPFPictureData pictureData = picture.getPictureData();
                                                String base64Image = "<img src='data:image/png;base64," + Base64.getEncoder().encodeToString((pictureData.getData())) + "'/>";
                                                tableBuilder.append(base64Image);
                                            }
                                        }
                                    });
                                }
                            }else {
                                tableBuilder.append(text);
                            }
                            tableBuilder.append("</td>");
                        }
                        tableBuilder.append("</tr>");
                    }
                    tableBuilder.append("</table>");
                    System.out.println(tableBuilder.toString());
                }
                System.out.println("</br>");
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * DOC传递文件路径
     * @param filePath 文件路径
     * @return 返回html标签
     */
    public static String inputDocPath(String filePath) {
        File file = new File(filePath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            HWPFDocument hwpfDocument = new HWPFDocument(inputStream);

            // 提取图像数据并转换为 BASE64 编码字符串
            List<Picture> pictures = hwpfDocument.getPicturesTable().getAllPictures();
            List<String> base64ImageStrings = new ArrayList<>();
            for (Picture picture : pictures) {
                byte[] imageData = picture.getContent();
                String base64ImageString = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
                base64ImageStrings.add(base64ImageString);
            }

            // 转换为 HTML 文本
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            wordToHtmlConverter.processDocument(hwpfDocument);
            Document document = wordToHtmlConverter.getDocument();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            String html = outputStream.toString("UTF-8");

            //查找所有图片
            List<String> matches = findMatchesToPic(html);

            //查找无用标签并替换成空，用自己写入的标签
            html = findMatchesToLable(html);
            // 在 HTML 中插入图像
            // 替换图片链接为 base64 编码
            for (int i = 0; i < base64ImageStrings.size(); i++) {
                html = html.replace(matches.get(i), "<img src=\"" + base64ImageStrings.get(i) + "\">");
            }

            return html;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //查找所有图片集合
    private static List<String> findMatchesToPic(String input) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("<!--.*?-->");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            matches.add(match);
        }
        return matches;
    }

    //查找没用的标签，并替换
    private static String findMatchesToLable(String html) {
        Pattern pattern = Pattern.compile("<META.*?>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            html = html.replace(matcher.group(), "");
        }

        Pattern patternMeta = Pattern.compile("<meta.*?>", Pattern.DOTALL);
        Matcher matcherMeta = patternMeta.matcher(html);
        if (matcherMeta.find()) {
            html = html.replace(matcherMeta.group(), "");
        }

        Pattern patternBody = Pattern.compile("<body.*?>", Pattern.DOTALL);
        Matcher matcherBody = patternBody.matcher(html);
        if (matcherBody.find()) {
            html = html.replace(matcherBody.group(), "");
        }

        Pattern patternStyle = Pattern.compile("<style.*?</style>", Pattern.DOTALL);
        Matcher matcherStyle = patternStyle.matcher(html);
        if (matcherStyle.find()) {
            html = html.replace(matcherStyle.group(), "");
        }

        html = html.replace("<html>", "");
        html = html.replace("<head>", "");
        html = html.replace("</head>", "");
        html = html.replace("</body>", "");
        html = html.replace("</html>", "");

        return html;
    }
}
