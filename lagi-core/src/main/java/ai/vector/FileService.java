package ai.vector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ai.common.pojo.FileChunkResponse;
import ai.ocr.OcrService;
import ai.utils.*;

import ai.utils.pdf.PdfUtil;
import ai.utils.word.WordUtils;
import com.google.gson.Gson;

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
        List<FileChunkResponse.Document> result = new ArrayList<>();
        String extString = file.getName().substring(file.getName().lastIndexOf("."));
        String fileType = extString.toLowerCase().toLowerCase();
        if (fileType.equals(".xls")||fileType.equals(".xlsx")){
            return EasyExcelUtil.getChunkDocumentExcel(file,chunkSize);
        }else if (fileType.equals(".csv")){
            return EasyExcelUtil.getChunkDocumentCsv(file);
        }else if (fileType.equals(".jpeg")||fileType.equals(".png")||
                  fileType.equals(".gif")||fileType.equals(".bmp")||
                  fileType.equals(".webp")||fileType.equals(".jpg")){
            return getChunkDocumentImage(result, file, chunkSize);
        }
        String content = getFileContent(file);
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

    public static List<FileChunkResponse.Document> getChunkDocumentImage(List<FileChunkResponse.Document> result,File file,Integer chunkSize) {
        List<String> langList = new ArrayList<>();
        langList.add("chn,eng,tai");
        OcrService ocrService = new OcrService();
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        File AbsoluteFile = new File("/upload/"+file.getName());
        System.out.println("AbsolutePath-----"+AbsoluteFile.getPath());
        String content = "";
        try {
            content = ocrService.image2Ocr(fileList, langList).get(0).toString();
        }catch (Exception e){
            System.out.println("ocr 未启用");
            content = "";
        }
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String text = content.substring(start, end).replaceAll("\\s+", " ");
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(text);
            List<String> images = new ArrayList<>();
            images.add(file.getAbsolutePath());
            FileChunkResponse.Image image = new FileChunkResponse.Image();
            image.setPath(AbsoluteFile.getPath());
            List<FileChunkResponse.Image> list = new ArrayList<>();
            list.add(image);
            doc.setImage(list);
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
                System.out.println("正在解析word文档,是否包含图片"+WordDocxUtils.checkImagesInWord(file));
                 content = WordUtils.getContentsByWord(in, extString).replaceAll("\\n+", "\n");;
                 content = content!=null?removeDirectory(content):content;
                break;
            case ".txt":
                content = getString(file.getPath());
                break;
            case ".pdf":
                //.replaceAll("[\r\n?|\n]", "")
                content = PdfUtil.webPdfParse(in).replaceAll("\\n+", "\n");
                content = content!=null?removeDirectory(content):content;
                break;
            case ".xls":
            case ".xlsx":
                content = EasyExcelUtil.getExcelContent(file);
                break;
            case ".csv":
                content = EasyExcelUtil.getCsvContent(file);
                break;
            case ".jpg":
            case ".jpeg":
            case ".png":
            case ".gif":
            case ".bmp":
            case ".webp":
                OcrService ocrService = new OcrService();
                List<String> langList = new ArrayList<>();
                langList.add("chn,eng,tai");
                content = "图片名为："+file.getName();
                List<File> fileList = new ArrayList<>();
                fileList.add(file);
                content += "内容为："+ocrService.image2Ocr(fileList, langList).toString();
                break;
            default:
                System.out.println("无法识别该文件");
                break;
        }
        in.close();
        return content;
    }


    public static String removeDirectory(String content) {

        Pattern directoryTitlePattern = Pattern.compile("目\\s*录|目\\s*次", Pattern.CASE_INSENSITIVE);
        Matcher directoryTitleMatcher = directoryTitlePattern.matcher(content);
        if (!directoryTitleMatcher.find()) {
            return content;
        }
        Integer directoryEndIndex = directoryTitleMatcher.end();
        Integer jei = directoryEndIndex;
        Integer directoryStartIndex = directoryEndIndex;

        while (content.length()>= jei) {
            char ch = content.charAt(directoryStartIndex);
            while ((content.length()> directoryStartIndex+1)&&ch == '\n'){
                jei++;
                directoryStartIndex++;
                ch = content.charAt(directoryStartIndex);
            }
            int nextLineIndex = content.indexOf('\n', directoryStartIndex);
            if (nextLineIndex == -1) {
                break;
            }
            directoryStartIndex = nextLineIndex;
            String nextLine = content.substring(jei, nextLineIndex).trim();
            if (!nextLine.isEmpty()&&!nextLine.matches(".*([IVXLCDM]{1,4}|\\d+)$")){
                break;
            }
            jei = nextLineIndex;
        }
        String cleanedContent = content.substring(0, directoryEndIndex) + content.substring(jei);
        return cleanedContent;
    }

    public static String getString(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            content.append(Files.lines(Paths.get(filePath))
                    .collect(Collectors.joining("\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
