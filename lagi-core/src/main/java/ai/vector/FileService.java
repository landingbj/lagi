package ai.vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.common.pojo.FileChunkResponse;
import ai.utils.AiGlobal;
import ai.utils.HttpUtil;

import ai.utils.LagiGlobal;
import ai.utils.pdf.PdfUtil;
import ai.utils.word.WordUtils;
import com.google.gson.Gson;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.IOUtils;

public class FileService {
    private static final String EXTRACT_CONTENT_URL = AiGlobal.SAAS_URL + "/saas/extractContentWithImage";

    private final Gson gson = new Gson();

    public FileChunkResponse extractContent(File file) {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        Map<String, String> headers = new HashMap<>();
        if (LagiGlobal.getLandingApikey() == null) {
            return null;
        }
        headers.put("Authorization", "Bearer " + LagiGlobal.getLandingApikey());
        String returnStr = HttpUtil.multipartUpload(EXTRACT_CONTENT_URL, fileParmName, fileList, formParmMap, headers);
        return gson.fromJson(returnStr, FileChunkResponse.class);
    }

    public List<FileChunkResponse.Document> splitChunks(File file, int chunkSize) throws IOException {
        String content = getFileContent(file);
        List<FileChunkResponse.Document> result = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String text = content.substring(start, end).replaceAll("\\s+", " ");
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
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
        switch (extString.toLowerCase()) {
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
