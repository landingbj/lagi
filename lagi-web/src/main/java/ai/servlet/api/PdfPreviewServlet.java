package ai.servlet.api;


import ai.common.pojo.IndexSearchData;
import ai.common.pojo.TextBlock;
import ai.config.ContextLoader;
import ai.dto.*;
import ai.learn.questionAnswer.KShingleFilter;
import ai.response.CropRectResponse;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.sevice.PdfService;
import ai.utils.PdfUtil;
import ai.vector.VectorStoreService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

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

    private final boolean enabledRagTrack = Boolean.TRUE.equals(ContextLoader.configuration.getStores().getRag().getTrack());

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
        if(!enabledRagTrack) {
            return Collections.emptyList();
        }
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
        return res;
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
        String baseDir = makeCropImageDir(request.getSession().getServletContext().getRealPath("static") + "/" + cropImageBaseDir);
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
                        String cropImage = pdfService.cropPageImage(filePath, baseDir, rect.getPage() - 1, rect.getRect().get(0), rect.getRect().get(1), rect.getRect().get(2), rect.getRect().get(3));
                        if(cropImage != null) {
                            String path = "static/" +  cropImageBaseDir + new File(cropImage).getName();
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
        List<Pattern> patterns = Lists.newArrayList(

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
