package ai.utils;

import ai.utils.word.WordUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class WordDocxUtils extends WordUtils {
    public WordDocxUtils()
    {
        super();
    }
    public static boolean checkImagesInWord(File file) throws IOException {
        String extString = file.getName().substring(file.getName().lastIndexOf("."));
        if (extString.toLowerCase().trim().equals(".docx")) {
            FileInputStream fis = new FileInputStream(file.getPath());
            XWPFDocument document = new XWPFDocument(fis);
            if (document != null){
                List<XWPFPictureData> pictures = document.getAllPictures();
                return pictures != null && !pictures.isEmpty();
            }
        }if (extString.toLowerCase().trim().equals(".doc")){
            FileInputStream fis = new FileInputStream(file.getPath());
            HWPFDocument document = new HWPFDocument(fis);
            if (document != null){
                PicturesTable picturesTable = document.getPicturesTable();
                if(picturesTable.getAllPictures()!=null){
                    List<Picture> pictures = picturesTable.getAllPictures();
                    return picturesTable != null && !pictures.isEmpty();
                }
            }
        } else if (extString.toLowerCase().trim().equals(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                for (PDPage page : document.getPages()) {
                    if (hasImages(page)) {
                        return true;
                    }
                }
            }
    }

        return false;
    }
    private static boolean hasImages(PDPage page) throws IOException {
        PDResources resources = page.getResources();
        if (resources != null) {
            COSDictionary xObjects = (COSDictionary) resources.getCOSObject().getDictionaryObject(COSName.XOBJECT);
            if (xObjects != null) {
                for (COSName name : xObjects.keySet()) {
                    PDXObject xObject = resources.getXObject(name);
                    if (xObject instanceof PDImageXObject) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
