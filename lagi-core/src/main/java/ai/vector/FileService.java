package ai.vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.common.pojo.Document;
import ai.common.pojo.ExtractContentResponse;
import ai.utils.HttpUtil;

import ai.utils.pdf.PdfUtil;
import ai.utils.word.WordUtils;
import com.google.gson.Gson;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.IOUtils;

public class FileService {
    private static final String FILE_PROCESS_URL = "http://ec2-52-82-51-248.cn-northwest-1.compute.amazonaws.com.cn:8200";
    private static final String EXTRACT_CONTENT_URL = FILE_PROCESS_URL + "/file/extract_content_with_image";

    private final Gson gson = new Gson();

    public ExtractContentResponse extractContent(File file) throws IOException {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        String returnStr = HttpUtil.multipartUpload(EXTRACT_CONTENT_URL, fileParmName, fileList, formParmMap);
        ExtractContentResponse response = gson.fromJson(returnStr, ExtractContentResponse.class);
        return response;
    }

    public List<Document> splitChunks(File file, int chunkSize) throws IOException {
        String content = getFileContent(file);
        List<Document> result = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String text = content.substring(start, end).replaceAll("\\s+", " ");
            Document doc = new Document();
            doc.setText(text);
            result.add(doc);
            start = end;
        }
        return result;
    }

    public String getFileContent(File file) throws IOException {
        String extString = file.getName().substring(file.getName().lastIndexOf("."));
        InputStream in = Files.newInputStream(file.toPath());
        String content = null;
        switch (extString) {
            case ".doc":
            case ".docx":
                content = WordUtils.getContentsByWord(in, extString);
                break;
            case ".txt":
                content = getString(in);
                break;
            case ".pdf":
                content = PdfUtil.webPdfParse(in).replaceAll("[\r\n?|\n]", "");
                break;
            default:
                break;
        }
        in.close();
        return content;
    }

    private String getString(InputStream in) {
        String str = "";
        try {
            BufferedInputStream bis = new BufferedInputStream(in);
            CharsetDetector cd = new CharsetDetector();
            cd.setText(bis);
            CharsetMatch cm = cd.detect();
            if (cm != null) {
                Reader reader = cm.getReader();
                str = IOUtils.toString(reader);
            } else {
                str = IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }
}
