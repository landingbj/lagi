package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.utils.*;
import ai.utils.word.WordUtils;
import ai.vector.FileService;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.Document;
import ai.vector.loader.pojo.DocumentParagraph;
import ai.vector.loader.pojo.SplitConfig;
import ai.vector.loader.util.DocxParser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DocLoader implements DocumentLoader {

    @Override
    public List<FileChunkResponse.Document> load(String path, SplitConfig splitConfig) {
        FileService fileService = new FileService();
        File file = new File(path);
        String content = null;

        try {
            if (WordDocxUtils.checkImagesInWord(file)){
                FileChunkResponse response = fileService.extractContent(file);
                if (response != null && response.getStatus().equals("success")) {
                    return response.getData();
                }
            }
            String extString = file.getName().substring(file.getName().lastIndexOf("."));
            InputStream in = Files.newInputStream(file.toPath());
            content = WordUtils.getContentsByWord(in, extString).replaceAll("\\n+", "\n");;
            content = content!=null? FileService.removeDirectory(content):content;
        } catch (Exception e) {
            log.error("load doc file error", e);
        }

        if(file.getName().endsWith(".docx")) {
            try {
                Document document = DocxParser.loadDocx(path);
                List<DocumentParagraph> paragraphs = document.getParagraphs();
                boolean b = hasTitle(document);
                if(b) {
                    return splitByTitle(path, paragraphs, splitConfig.getChunkSizeForText());
                }
            }catch (Exception ignored){
            }
        }

        if(content == null) {
            return Collections.emptyList();
        }
        if (QaExtractorUtil.extractQA(content, splitConfig.getCategory(), splitConfig.getExtra(), splitConfig.getChunkSizeForText())) {
            return Collections.emptyList();
        }
        if (ChapterExtractorUtil.isChapterDocument(content)) {
            return ChapterExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        } else if (SectionExtractorUtil.isChapterDocument(content, splitConfig.getChunkSizeForText())) {
            return SectionExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        } else if (OrdinanceExtractorUtil.isOrdinanceDocument(content)) {
            return OrdinanceExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        } else {
            return FileService.splitContentChunks(splitConfig.getChunkSizeForText(), content);
        }
    }


    private boolean hasTitle(Document document) {
        return document.getTitleCount() > 5;
    }

    List<FileChunkResponse.Document> splitByTitle(String path, List<DocumentParagraph> paragraphs, int chunkSize) {

        paragraphs = mergeParagraphs(new File(path).getParentFile(), paragraphs);

        // 连续的title 合并
        // chunk size > chunkSize 分割。  title 下有正文, 遇到下一个 title分割
        int minTxtCount = chunkSize / 5;
        List<FileChunkResponse.Document> res = new ArrayList<>();
        FileChunkResponse.Document document = new FileChunkResponse.Document();
        for (DocumentParagraph paragraph : paragraphs) {
            String curTxt = paragraph.getTxt();
            String text = document.getText() == null? "" : document.getText();
            int lastLen = text.length();
            int curLen = curTxt.length();
            int totalLen = lastLen + curLen;
            // title 遇到下一个title 且大于最小的长度 分割
            // split
            if(curLen > chunkSize) {
                res.add(document);
                document = new FileChunkResponse.Document();
                int loop = curLen / chunkSize;
                int i = 0;
                for (; i < loop; i++) {
                    curTxt = paragraph.getTxt().substring(i * chunkSize, (i + 1) * chunkSize);
                    document.setText(curTxt);
                    res.add(document);
                    document = new FileChunkResponse.Document();
                }
                curTxt = paragraph.getTxt().substring(i * chunkSize);
                text = "";
            } else {
                if("title".equals(paragraph.getSubType())){
                    if(totalLen > minTxtCount) {
                        res.add(document);
                        document = new FileChunkResponse.Document();
                        text = "";
                    }
                } else {
                    if(totalLen > chunkSize) {
                        res.add(document);
                        document = new FileChunkResponse.Document();
                        text = "";
                    }
                }
            }

            document.setText(text + curTxt);

            List<FileChunkResponse.Image> images = document.getImages();
            if(images == null) {
                images = new ArrayList<>();
            }
            if(paragraph.getImages() != null){
                List<FileChunkResponse.Image> collect = paragraph.getImages().stream().map(image -> {
                    FileChunkResponse.Image image1 = new FileChunkResponse.Image();
                    image1.setPath(image);
                    return image1;
                }).collect(Collectors.toList());
                images.addAll(collect);
                document.setImages(images);
            }
        }
        res.add(document);
        res.forEach(document1 -> {
            if(document1.getText() != null) {
                document1.setText(document1.getText().replaceAll("\\n{2,1000}", "\n\n"));
            }
        });
        return res;
    }

    private List<DocumentParagraph> mergeParagraphs(File dir,  List<DocumentParagraph> paragraphs) {
        List<DocumentParagraph> res  = new ArrayList<>();
        for (DocumentParagraph paragraph : paragraphs) {
            if("txt".equals(paragraph.getType())) {
                if(paragraph.getTxt() == null) {
                    paragraph.setTxt("");
                }
                if("\n".equals(paragraph.getTxt())) {
                    if(res.isEmpty()) {
                        res.add(paragraph);
                    } else {
                        DocumentParagraph documentParagraph = res.get(res.size() - 1);
                        documentParagraph.setTxt(documentParagraph.getTxt() + paragraph.getTxt());
                    }
                } else {
                    res.add(paragraph);
                }
            }
            if("image".equals(paragraph.getType())) {
                List<String> strings = convert2LocalImagePath(dir, paragraph);
                paragraph.setImages(strings);
                if(res.isEmpty()) {
                    DocumentParagraph documentParagraph = new DocumentParagraph("txt", null, "0", "", paragraph.getImages(), null);
                    res.add(documentParagraph);
                } else {
                    DocumentParagraph documentParagraph = res.get(res.size() - 1);
                    List<String> images = documentParagraph.getImages();
                    if(images == null) {
                        images = new ArrayList<>();
                    }
                    images.addAll(paragraph.getImages());
                    documentParagraph.setImages(images);
                }
            }
            if("table".equals(paragraph.getType())) {
                String markdown = toMarkdown(paragraph.getTable());
                paragraph.setTxt(markdown);
                if(res.isEmpty()) {
                    res.add(paragraph);
                } else {
                    DocumentParagraph documentParagraph = res.get(res.size() - 1);
                    documentParagraph.setTxt(documentParagraph.getTxt() + "\n" + paragraph.getTxt());
                }
            }
        }
        return res;
    }


    private List<String> convert2LocalImagePath(File dir,  DocumentParagraph paragraph) {
        List<String> imagesTemp = paragraph.getImages();
        return imagesTemp.stream().map(image -> {
            // 下载到本地
            String filename = UUID.randomUUID() + ".jpg";
            String absolutePath = Paths.get(dir.getAbsolutePath(), filename).toFile().getAbsolutePath();
            Base64Util.toFile(absolutePath, image);
            return dir.getName() + "/" + filename;
        }).collect(Collectors.toList());
    }


//    private List<FileChunkResponse.Image> covert2FileChunkResponseImage(File dir,  DocumentParagraph paragraph) {
//        List<String> imagesTemp = paragraph.getImages();
//        return imagesTemp.stream().map(image -> {
//            FileChunkResponse.Image image1 = new FileChunkResponse.Image();
//            // 下载到本地
//            String filename = UUID.randomUUID() + ".jpg";
//            String absolutePath = Paths.get(dir.getAbsolutePath(), filename).toFile().getAbsolutePath();
//            Base64Util.toFile(absolutePath, image);
//            image1.setPath(dir.getName() + "/" + filename);
//            return image1;
//        }).collect(Collectors.toList());
//    }

    public static String toMarkdown(List<List<String>> table) {
        if(table != null && !table.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("|");
            for (int i = 0; i < table.get(0).size(); i++) {
                String s = table.get(0).get(i);
                sb.append(s).append("|");
            }
            sb.append("\n");
            if(table.size() > 1) {
                sb.append("|");
                for (int i = 0; i < table.get(0).size(); i++) {
                    sb.append("----").append("|");
                }
                sb.append("\n");
                sb.append("|");
                for (int i = 1; i < table.size(); i++) {
                    List<String> row = table.get(i);
                    for (int j = 0; j < row.size(); j++) {
                        sb.append(row.get(j)).append("|");
                    }
                    if(i < table.size() - 1) {
                        sb.append("\n");
                        sb.append("|");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        return "";
    }

    public static void main(String[] args) {



        SplitConfig splitConfig = new SplitConfig(512, 512, 512, "a", Collections.emptyMap());
        DocLoader docLoader = new DocLoader();
        List<FileChunkResponse.Document> load = docLoader.load("C:\\Users\\Administrator\\Desktop\\bushu\\RAG\\测试文档\\2\\大模型中间件产品介绍V1.4.docx", splitConfig);
        for (FileChunkResponse.Document document : load) {
            System.out.println(document.getText());
//            if(document.getText() != null) {
                System.out.println(document.getText().length()+ "__________________________________________");
//            }
//            System.out.println(document.getImages());
        }
    }
}
