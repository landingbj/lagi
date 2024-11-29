package ai.servlet.api;


import ai.common.pojo.IndexSearchData;
import ai.common.pojo.TextBlock;
import ai.dto.*;
import ai.learn.questionAnswer.KShingleFilter;
import ai.response.ChunkDataResponse;
import ai.response.CropRectResponse;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.sevice.PdfService;
import ai.utils.PdfUtil;
import ai.vector.VectorStoreService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class PdfPreviewServlet extends RestfulServlet {

    private final PdfService pdfService = new PdfService();

    private final VectorStoreService vectorStoreService = new VectorStoreService();

    private final String cropImageBaseDir = "crop/";

    private static final String UPLOAD_DIR = "/upload";

//    private KShingleFilter kShingleFilter = new KShingleFilter(4, 0.3, 0.5);

    @Post("genPdfCrop")
    public List<List<String>> searchTextPCoordinate(@Body PdfSearchRequest request) {
        String fileDir = makeCropImageDir(cropImageBaseDir + request.getFileName().split("\\.")[0]);
        int extend = request.getExtend() == null ? 50 : request.getExtend();
        List<List<String>>  res  = new ArrayList<>();
        List<String> searchWords = request.getSearchWords();
        for (String word : searchWords) {
            List<String>  paths =  new ArrayList<>();
            List<TextBlock> lists = pdfService.searchTextPCoordinate(request.getPdfPath(), word);
            if(lists.isEmpty()) {
                continue;
            }
            List<List<TextBlock>> pagesList = new ArrayList<>();
            List<TextBlock> pageList = new ArrayList<>();
            int lastPageIndex = lists.get(0).getPageNo();
            for (TextBlock list : lists) {
                if(Objects.equals(lastPageIndex, list.getPageNo())) {
                    pageList.add(list);
                } else {
                    pagesList.add(pageList);
                    pageList = new ArrayList<>();
                    pageList.add(list);
                    lastPageIndex = list.getPageNo();
                }
            }
            pagesList.add(pageList);
            for (List<TextBlock> plist : pagesList) {
                List<Integer> rect = calcCropRect(plist, extend);
                int pageIndex = (int) Math.floor(plist.get(0).getPageNo() - 1);
                String cropImage = pdfService.cropPageImage(request.getPdfPath(), fileDir, pageIndex, rect.get(0), rect.get(1), rect.get(2), rect.get(3));
                paths.add(cropImage);
            }
            res.add(paths);
        }
        return res;
    }

    @Post("filterChunk")
    public List<ChunkDataResponse> filterChunk(@Body ChunkFilterRequest chunkFilterRequest) {
        List<IndexSearchData> indexSearchData = vectorStoreService.searchByIds(chunkFilterRequest.getChunkIds(), chunkFilterRequest.getCategory());
        Set<String> context = new HashSet<>();
        KShingleFilter kShingleFilter = new KShingleFilter(chunkFilterRequest.getResult().length(), 0.3, 0.5);
        return indexSearchData.stream()
                .filter(i-> {
                    if(!context.add(i.getText())) {
                        return false;
                    }
                    if(i.getFilepath() != null && !i.getFilepath().isEmpty()) {
                        boolean similar = kShingleFilter.isSimilar(chunkFilterRequest.getResult(), i.getText());
                        System.out.println(similar);
                        return similar;
                    }
                    return false;
                })
                .map(i -> ChunkDataResponse.builder()
                        .chunk(i.getText())
                        .filePath(i.getFilepath().get(0))
                        .filename(i.getFilename().get(0))
                        .build())
                .collect(Collectors.toList());
    }



    private boolean isSimilar(KShingleFilter kShingleFilter, String text, String result) {
        List<String> objects = splitChunk(text, 512);
        for (String s : objects) {
            boolean similar = kShingleFilter.isSimilar(result, s);
            if(similar) {
                return true;
            }
        }
        return false;
    }

    private String bigChunkSearch(KShingleFilter kShingleFilter, String text, String result) {
        List<String> objects = splitChunk(text, 512);
        for (String s : objects) {
            boolean similar = kShingleFilter.isSimilar(result, s);
            System.out.println(text);
            System.out.println(similar);
            if(similar) {
                return s;
            }
        }
        return text;
    }

    private static List<String> splitChunk(String text, int size) {
        List<String> objects = new ArrayList<>();
        for(int i = 0; i < text.length(); i+=size) {
            int limit = Math.min(i + size, text.length());
            String substring = text.substring(i, limit);
            objects.add(substring);
        }
        return objects;
    }

    @Post("crop")
    public List<String> crop(HttpServletRequest request,  @Body CropRequest cropRequest) {
        String baseDir = makeCropImageDir(request.getSession().getServletContext().getRealPath("static") + "/" + cropImageBaseDir);
        Integer extend = cropRequest.getExtend() == null ? 200 : cropRequest.getExtend();
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);

        List<IndexSearchData> indexSearchData = vectorStoreService.searchByIds(Lists.newArrayList(cropRequest.getChunkId()), cropRequest.getCategory());
        if(indexSearchData == null || indexSearchData.isEmpty()) {
            return Collections.emptyList();
        }
        if(indexSearchData.get(0).getFilepath() == null  || indexSearchData.get(0).getFilepath().isEmpty()) {
            return Collections.emptyList();
        }
        String chunk = indexSearchData.get(0).getText().replaceAll("\\s+", "");
        String filePath = uploadDir + File.separator + indexSearchData.get(0).getFilepath().get(0);
        File uploadFile = new File(filePath);
        if(!uploadFile.exists()) {
            return Collections.emptyList();
        }
        filePath = convertDoc2Pdf(uploadFile, filePath);
        List<TextBlock> pCoordinates = null;
        Matcher matcher = qaMather(chunk);
        if(matcher != null) {
            pCoordinates = searchPdfByQA(matcher, filePath);
        }
        if(pCoordinates == null) {
            pCoordinates = pdfService.searchTextPCoordinate(filePath, chunk);
        }
        Map<Integer, List<TextBlock>> pageCoordinateMap = pCoordinates.stream().collect(Collectors.groupingBy(TextBlock::getPageNo));
        Map<Integer, String> cropMap =  new HashMap<>();
        KShingleFilter kShingleFilter = new KShingleFilter(cropRequest.getResult().length(), 0.3, 0.5);
        for (Map.Entry<Integer, List<TextBlock>> entry : pageCoordinateMap.entrySet()) {
            List<TextBlock> pCoordinate = entry.getValue();
            String ck = getChunkByMathOrPdfCoordinate(matcher, chunk, pCoordinate);
            boolean similar = kShingleFilter.isSimilar(cropRequest.getResult(), ck);
            if(!similar) {
                continue;
            }
            List<Integer> rect = calcCropRect(pCoordinate, extend);
            int pageIndex = entry.getKey() - 1;
            String cropImage = pdfService.cropPageImage(filePath, baseDir, pageIndex, rect.get(0), rect.get(1), rect.get(2), rect.get(3));
            if(cropImage != null) {
                File file = new File(cropImage);
                String path = "static/" +  cropImageBaseDir + file.getName();
                cropMap.put(entry.getKey(), path);
            }
        }
        return pCoordinates.stream().map(TextBlock::getPageNo).distinct().map(cropMap::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String makeCropImageDir(String request) {
        //获取static文件夹的路径
        File dir = new File(request);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return request;
    }


    @Post("cropRect")
    public List<CropRectResponse> cropRect(HttpServletRequest request, @Body CropRectRequest cropRectRequest) {
        makeCropImageDir(request.getSession().getServletContext().getRealPath("static"));
        List<CropRequest> chunkData = cropRectRequest.getChunkData();
        List<CropRectResponse> res = new ArrayList<>();
        Set<String> chunkSet = new HashSet<>();
        for (CropRequest cropRequest : chunkData) {
            String chunkId = cropRequest.getChunkId();
            List<IndexSearchData> indexSearchData = vectorStoreService.searchByIds(Lists.newArrayList(chunkId), cropRectRequest.getCategory());
            if(indexSearchData == null || indexSearchData.isEmpty()) {
                continue;
            }
            IndexSearchData data = indexSearchData.get(0);
            if(data.getFilepath() == null || data.getFilepath().isEmpty()) {
                continue;
            }
            String chunk = data.getText().replaceAll("\\s+", "");
            if(!chunkSet.add(chunk)) {
                continue;
            }

            int extend = cropRequest.getExtend() == null ? 400 : cropRequest.getExtend();
            String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
            String filePath = uploadDir + File.separator + data.getFilepath().get(0);
            File uploadFile = new File(filePath);
            filePath = convertDoc2Pdf(uploadFile, filePath);
            if(!uploadFile.exists()) {
                continue;
            }
            KShingleFilter kShingleFilter = new KShingleFilter(4, 0.2, 0.5);
            String minStr = chunk.length() < cropRequest.getResult().length() ? chunk : cropRequest.getResult();
            String maxStr = chunk.length() < cropRequest.getResult().length() ?  cropRequest.getResult() : chunk;
            boolean similar1 = kShingleFilter.isSimilar(minStr, maxStr);
            if(!similar1) {
                continue;
            }
            List<TextBlock> pCoordinates = null;
            Matcher matcher = qaMather(chunk);
            if(matcher != null) {
                pCoordinates = searchPdfByQA(matcher, filePath);
            }
            if(pCoordinates == null) {
                pCoordinates = pdfService.searchTextPCoordinate(filePath, chunk);
            }
            // split to page
            Map<Integer, List<TextBlock>> pageCoordinateMap = pCoordinates.stream().collect(Collectors.groupingBy(TextBlock::getPageNo));
            Map<Integer, PageRect> pageRectMap =  new HashMap<>();
            double minThreshold = 0.3;
            for (Map.Entry<Integer, List<TextBlock>> entry : pageCoordinateMap.entrySet()) {
                List<TextBlock> pCoordinate = entry.getValue();
                String ck = getChunkByMathOrPdfCoordinate(matcher, chunk, pCoordinate);
                double similar = detectSimilarity(0.3, cropRequest.getResult(), ck);
                if(similar < minThreshold) {
                    continue;
                }
                List<Integer> rect = calcCropRect(pCoordinate, extend);
                if(similar> minThreshold) {
                    minThreshold = Math.max(similar, minThreshold);
                    pageRectMap = new HashMap<>();
                }
                pageRectMap.put(entry.getKey(), PageRect.builder().page(entry.getKey()).rect(rect).build());
            }
            List<PageRect> rects = pCoordinates.stream()
                    .map(TextBlock::getPageNo)
                    .distinct()
                    .map(pageRectMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if(!rects.isEmpty()) {
                res.add(CropRectResponse.builder()
                        .filename(data.getFilename().get(0))
                        .filePath(data.getFilepath().get(0))
                        .rects(rects)
                        .build());
            }
        }
        return merge(res);
    }

     public List<CropRectResponse> merge(List<CropRectResponse> chunkData) {
        Map<String, List<CropRectResponse>> groupedByFilePath = chunkData.stream()
                .collect(Collectors.groupingBy(CropRectResponse::getFilePath));
        List<CropRectResponse> result = new ArrayList<>();

        for (Map.Entry<String, List<CropRectResponse>> entry : groupedByFilePath.entrySet()) {
            String filePath = entry.getKey();
            List<CropRectResponse> group = entry.getValue();
            List<PageRect> mergedRects = group.stream()
                    .flatMap(response -> response.getRects().stream())
                    .distinct()
                    .collect(Collectors.toList());
            CropRectResponse mergedResponse = new CropRectResponse();
            mergedResponse.setFilePath(filePath);
            mergedResponse.setFilename(group.get(0).getFilename());
            mergedResponse.setRects(mergedRects);
            result.add(mergedResponse);
        }

        return result;
    }
    private double detectSimilarity(double threshold,  String result, String text) {
        double max = 0.0;
        String maxStr = result.length() > text.length() ? result : text;
        String minStr = result.length() > text.length() ? text : result;
        for (int i = 0; i < 100; i++) {
            threshold = threshold +  i * 0.1;
            KShingleFilter kShingleFilter = new KShingleFilter(minStr.length() , threshold , 0.5);
            if(kShingleFilter.isSimilar(minStr, maxStr)) {
                max = Math.max(max, threshold);
            }else {
                break;
            }
        }
        return max;
    }

    private static String getChunkByMathOrPdfCoordinate(Matcher matcher, String chunk, List<TextBlock> pCoordinate) {
        String ck = null;
        if(matcher != null) {
            ck = chunk;
        }else {
            StringBuilder pageText = new StringBuilder();
            for (TextBlock i : pCoordinate) {
                pageText.append(i.getText());
            }
            ck = pageText.toString();
        }
        return ck;
    }

    @Post("cropByRect")
    public List<String> cropByRect(HttpServletRequest request, @Body CropRectRequest cropRectRequest) {
        String cropImageBaseDirs = StringUtils.left(cropImageBaseDir, cropImageBaseDir.length() - 1);

        String baseDir = makeCropImageDir(request.getSession().getServletContext().getRealPath("static") + "\\" + cropImageBaseDirs);
        List<CropRequest> chunkData = cropRectRequest.getChunkData();
        List<String> res = new ArrayList<>();
        for (CropRequest cropRequest : chunkData) {
            String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
            String filePath = uploadDir + File.separator + cropRequest.getFilePath();
            filePath = convertDoc2Pdf(new File(filePath), filePath);
            File uploadFile = new File(filePath);
            if(!uploadFile.exists()) {
                continue;
            }
            if(cropRequest.getRects() != null) {
                List<PageRect> rects = cropRequest.getRects();
                if(rects != null && !rects.isEmpty()) {
                    for (PageRect rect : rects) {
                        String dir = baseDir + File.separator + uploadFile.getName().split("\\.")[0];
                        File file = new File(dir);
                        if(!file.exists()) {
                            file.mkdirs();
                        }
                        String cropImage = pdfService.cropPageImage(filePath, dir  , rect.getPage() - 1, rect.getRect().get(0), rect.getRect().get(1), rect.getRect().get(2), rect.getRect().get(3));
                        if(cropImage != null) {
                            String path = "static/" +  cropImageBaseDir + uploadFile.getName().split("\\.")[0]+ "/"+ new File(cropImage).getName();
                            res.add(path);
                        }
                    }
                }
            }
        }
        return res;
    }

    private List<TextBlock> searchPdfByQA(Matcher matcher, String filePath) {
        List<TextBlock> pCoordinates = null;
        int count = matcher.groupCount();
        int oneLineLen = 10;
        if (count >= 2) {
            String s1 = matcher.group(count);
            s1 = s1.substring(0, Math.min(oneLineLen, s1.length()));
            String s2 = matcher.group(count - 1);
            s2 = s2.substring(0, Math.min(oneLineLen, s2.length()));
            String s3 = null;
            if(count - 2 >= 1) {
                s3 = matcher.group(count - 2);
                s3 = s3.substring(0, Math.min(oneLineLen, s3.length()));
            }
            List<List<TextBlock>> p1 = pdfService.searchAllTextPCoordinate(filePath, s1);
            List<List<TextBlock>> p2 = pdfService.searchAllTextPCoordinate(filePath, s2);
            if(s3 != null) {
                List<List<TextBlock>> p3 = pdfService.searchAllTextPCoordinate(filePath, s3);
                pCoordinates = nearBlock(p1, p2, p3, 400);
            } else {
                pCoordinates = nearBlock(p1, p2, 400);
            }
        }
        return pCoordinates;
    }

    private String getLastLine(String str) {
        String[] split = str.split("\r\n");
        return split[split.length - 1].replaceAll("\\s+", "");
    }


    private static String convertDoc2Pdf(File uploadFile, String filePath) {
        if(!"pdf".equals(uploadFile.getName().split("\\.")[1])) {
            String parent = uploadFile.getParent();
            filePath = parent + File.separator + uploadFile.getName().split("\\.")[0]+ ".pdf";
            if(!new File(filePath).exists()) {
                PdfUtil.convertToPdf(uploadFile.getAbsolutePath(), parent);
            }
        }
        return filePath;
    }

    private Matcher qaMather(String chunk) {

//        故障分类:硬件比较仪。编号85位号:位10/11解决方法及建议:未用。
//        故障分类	位号	故障名称	故障原因	解决方法及建议
//        故障分类:硬件比较仪。编号85位号:位15故障名称:定子电流U标号（1=+）
        List<Pattern> patterns = Lists.newArrayList(
            Pattern.compile("状态代码:(.+)名称:(.+)原因/描述:(.+)解决办法:(.+)"),
            Pattern.compile("状态代码:(.+)名称:(.+)描述:(.+)故障解决办法:(.+)"),
            Pattern.compile("状态代码:(.+)名称:(.+)描述:(.+)解决办法:(.+)"),
            Pattern.compile("序号:(.+)故障:(.+)原因/描述:(.+)解决办法:(.+)"),
            Pattern.compile("序号:(.+)报警字:(.+)报警信息:(.+)故障解决办法:(.+)"),
            Pattern.compile("故障分类:(.+)位号:(.+)故障名称:(.+)故障原因:(.+)解决方法及建议:(.+)"),
            Pattern.compile("故障名称:(.+)故障描述:(.+)故障原因:(.+)故障处理:(.+)"),
            Pattern.compile("故障名称:(.+)故障描述:(.+)故障处理:(.+)"),
            Pattern.compile("故障分类:(.+)位号:(.+)解决方法及建议:(.+)"),
            Pattern.compile("故障分类:(.+)位号:(.+)故障名称:(.+)"),
            Pattern.compile("序号:(.+)故障:(.+)原因/描述:(.+)"),
            Pattern.compile("序号:(.+)故障:(.+)故障解决方法:(.+)")
        );
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(chunk);
            if(matcher.find()) {
                return matcher;
            }
        }
        return null;
    }

    private List<TextBlock> nearBlock(List<List<TextBlock>> p1, List<List<TextBlock>> p2, int distanceLimit) {
        double powLimit = Math.pow(distanceLimit, 2);
        for (List<TextBlock> list : p1) {
            TextBlock point1 = list.get(0);
            int page1 = point1.getPageNo();
            for (List<TextBlock> list1 : p2) {
                TextBlock point2 = list1.get(0);
                int page2 = point2.getPageNo();
                if(page1 != page2) {
                    continue;
                }
                double pow = Math.pow(point2.getX() - point1.getX(), 2) + Math.pow(point2.getY() - point1.getY(), 2);
                if(pow <= powLimit) {
                    List<TextBlock> res = new ArrayList<>(list.size() + list1.size());
                    List<TextBlock> min = point1.getX() < point2.getX() ? list : list1;
                    List<TextBlock> max = point1.getX() >= point2.getX() ? list : list1;
                    res.addAll(min);
                    res.addAll(max);
                    return res;
                }
            }
        }
        return null;
    }


    private List<TextBlock> nearBlock(List<List<TextBlock>> p1, List<List<TextBlock>> p2, List<List<TextBlock>> p3, int distanceLimit) {
        double powLimit = Math.pow(distanceLimit, 2);
        for (List<TextBlock> list : p1) {
            TextBlock point1 = list.get(0);
            int page1 = point1.getPageNo();
            for (List<TextBlock> list1 : p2) {
                TextBlock point2 = list1.get(0);
                int page2 = point2.getPageNo();
                if(page1 != page2) {
                    continue;
                }
                for (List<TextBlock> list2 : p3) {
                    TextBlock point3 = list2.get(0);
                    int page3 = point3.getPageNo();
                    if(page2 != page3) {
                        continue;
                    }
                    double pow1 = Math.pow(point2.getX() - point1.getX(), 2) + Math.pow(point2.getY() - point1.getY(), 2);
                    if(pow1 <= powLimit) {
                        double pow2 = Math.pow(point3.getX() - point2.getX(), 2) + Math.pow(point3.getY() - point2.getY(), 2);
                        if(pow2 <= powLimit) {
                            List<TextBlock> res = new ArrayList<>(list.size() + list1.size());
                            List<TextBlock> min = point1.getX() < point2.getX() ? list : list1;
                            List<TextBlock> max = point1.getX() >= point2.getX() ? list : list1;
                            res.addAll(min);
                            res.addAll(max);
                            return res;
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<Integer> calcCropRect(List<TextBlock> textsPCoordinate, int extend) {
        if(textsPCoordinate.isEmpty()) {
            return Collections.emptyList();
        }
        int y0 = Integer.MAX_VALUE;
        int y1 = Integer.MIN_VALUE;
        for (TextBlock list : textsPCoordinate) {
            y0 = Math.min(y0, (int)list.getY() - extend);
            y1 = Math.max(y1, (int)list.getY() + extend);
        }
        return Lists.newArrayList(0, y0, 1200, y1);
    }
}
