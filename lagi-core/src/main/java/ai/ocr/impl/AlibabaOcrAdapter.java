package ai.ocr.impl;

import ai.annotation.OCR;
import ai.common.ModelService;
import ai.ocr.IOcr;
import ai.ocr.pojo.AlibabaOcrDocument;
import com.aliyun.ocr_api20210707.models.RecognizeAdvancedRequest;
import com.aliyun.ocr_api20210707.models.RecognizeAdvancedResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@OCR(company = "alibaba", modelNames = "ocr")
public class AlibabaOcrAdapter extends ModelService implements IOcr {

    @Override
    public boolean verify() {
        if(getAccessKeyId() == null || getAccessKeyId().startsWith("you")) {
            return false;
        }
        if(getAccessKeySecret() == null || getAccessKeySecret().startsWith("you")) {
            return false;
        }
        return true;
    }

    public com.aliyun.ocr_api20210707.Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(getAccessKeyId())
                .setAccessKeySecret(getAccessKeySecret())
                .setEndpoint(getEndpoint());
        return new com.aliyun.ocr_api20210707.Client(config);
    }

    public String recognize(BufferedImage image) {
        String result = null;
        RecognizeAdvancedResponse response;
        try {
            com.aliyun.ocr_api20210707.Client client = createClient();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            InputStream bodyStream = new ByteArrayInputStream(os.toByteArray());
            RecognizeAdvancedRequest recognizeAdvancedRequest = new RecognizeAdvancedRequest()
                    .setBody(bodyStream)
                    .setNeedRotate(true)
                    .setOutputTable(true)
                    .setNoStamp(true)
                    .setParagraph(true);
            RuntimeOptions runtime = new RuntimeOptions();
            response = client.recognizeAdvancedWithOptions(recognizeAdvancedRequest, runtime);
            if (response != null && response.getStatusCode() == 200) {
                result = response.getBody().getData();
                result = toFormatedText(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String toFormatedText(String text) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AlibabaOcrDocument doc = mapper.readValue(text, AlibabaOcrDocument.class);
        return toFormatedText(doc);
    }

    public String toFormatedText(AlibabaOcrDocument doc) {
        Map<Integer, Integer> paraTableMap = new HashMap<>();
        if (doc.getPrism_wordsInfo() != null) {
            for (AlibabaOcrDocument.PrismWordInfo wordInfo : doc.getPrism_wordsInfo()) {
                paraTableMap.put(wordInfo.getParagraphId(), wordInfo.getTableId());
            }
        }

        Map<Integer, String> tableHtmlMap = new HashMap<>();
        if (doc.getPrism_tablesInfo() != null) {
            for (AlibabaOcrDocument.PrismTablesInfo tablesInfo : doc.getPrism_tablesInfo()) {
                String html = toHtmlTable(tablesInfo);
                tableHtmlMap.put(tablesInfo.getTableId(), html);
            }
        }

        StringBuilder sb = new StringBuilder();
        Set<Integer> processedTableSet = new HashSet<>();

        for (int i = 0; i < doc.getPrism_paragraphsInfo().size(); i++) {
            AlibabaOcrDocument.PrismParagraphInfo para = doc.getPrism_paragraphsInfo().get(i);
            Integer tableId = paraTableMap.get(para.getParagraphId());
            if (tableId != null && tableHtmlMap.containsKey(tableId)) {
                if (!processedTableSet.contains(tableId)) {
                    sb.append(tableHtmlMap.get(tableId));
                    processedTableSet.add(tableId);
                    if (i < doc.getPrism_paragraphsInfo().size() - 1) {
                        sb.append("\n");
                    }
                }
            } else {
                sb.append(para.getWord());
                if (i < doc.getPrism_paragraphsInfo().size() - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    public String toHtmlTable(AlibabaOcrDocument.PrismTablesInfo tablesInfo) {
        return generateTableHtml(tablesInfo.getCellInfos());
    }

    public String generateTableHtml(List<AlibabaOcrDocument.CellInfo> cellInfos) {
        StringBuilder html = new StringBuilder();
        html.append("<table>");

        int maxRow = 0;
        int maxCol = 0;

        for (AlibabaOcrDocument.CellInfo cell : cellInfos) {
            maxRow = Math.max(maxRow, cell.getYec());
            maxCol = Math.max(maxCol, cell.getXec());
        }

        int[][] cellOccupancy = new int[maxRow + 1][maxCol + 1];

        for (AlibabaOcrDocument.CellInfo cell : cellInfos) {
            for (int i = cell.getYsc(); i <= cell.getYec(); i++) {
                for (int j = cell.getXsc(); j <= cell.getXec(); j++) {
                    cellOccupancy[i][j] = 1;
                }
            }
        }

        for (int i = 0; i <= maxRow; i++) {
            html.append("<tr>");
            for (int j = 0; j <= maxCol; j++) {
                if (cellOccupancy[i][j] == 0) {
                    html.append("<td></td>");
                } else {
                    AlibabaOcrDocument.CellInfo cell = getCellForPosition(cellInfos, i, j);
                    if (cell != null) {
                        if (i == cell.getYsc() && j == cell.getXsc()) {
                            int rowSpan = cell.getYec() - cell.getYsc() + 1;
                            int colSpan = cell.getXec() - cell.getXsc() + 1;
                            html.append("<td");
                            if (rowSpan > 1) {
                                html.append(" rowspan='").append(rowSpan).append("'");
                            }
                            if (colSpan > 1) {
                                html.append(" colspan='").append(colSpan).append("'");
                            }
                            html.append(">").append(cell.getWord()).append("</td>");
                        }
                    }
                }
            }
            html.append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }

    private AlibabaOcrDocument.CellInfo getCellForPosition(List<AlibabaOcrDocument.CellInfo> cellInfos, int row, int col) {
        for (AlibabaOcrDocument.CellInfo cell : cellInfos) {
            if (row >= cell.getYsc() && row <= cell.getYec() &&
                    col >= cell.getXsc() && col <= cell.getXec()) {
                return cell;
            }
        }
        return null;
    }
}
