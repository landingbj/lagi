package ai.sevice;

import ai.utils.PDFTextExtractor;
import ai.utils.PdfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PdfService {


    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    public List<PDFTextExtractor.TextBlock> searchTextPCoordinate(String pdfPath, String searchWord) {
        searchWord = searchWord.replaceAll("\\s+", "");
        return PdfUtil.getWordsCoordinateByPath(pdfPath, searchWord);
    }

    public List<List<PDFTextExtractor.TextBlock>> searchAllTextPCoordinate(String pdfPath, String searchWord) {
        searchWord = searchWord.replaceAll("\\s+", "");
        return PdfUtil.getAllWordsCoordinateByPath(pdfPath, searchWord);
    }

    public String cropPageImage(String pdfPath, String pageDir, int pageIndex, int x0, int y0, int x1, int y1) {
        try {
            return PdfUtil.getCroppedPageImage(pdfPath, pageDir, pageIndex, x0, y0, x1, y1);
        } catch (Exception e) {
            log.error("cropPageImage error", e);
        }
        return null;
    }
}
