package ai.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Slf4j
public class PdfUtil {

    private static final Lock convertLock = new ReentrantLock();
    private static final String SOFFICE_PATH = "D:\\Tools\\LibreOffice\\program\\soffice.exe";
    private static final int ZOOM_SIZE = 1;
    private static final float DPI = 300;

    public static String convertToPdf(String docPath, String pdfPath) {
        try {
            convertLock.lock();
            int exitCode = getExitCode(docPath, pdfPath);

            if (exitCode == 0) {
                System.out.println("Conversion successful.");
            } else {
                System.err.println("Conversion failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            convertLock.unlock();
        }

        return pdfPath;
    }

    private static int getExitCode(String docPath, String pdfPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                SOFFICE_PATH,
                "--headless",
                "--convert-to",
                "pdf",
                "--outdir",
                pdfPath,
                docPath
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        return exitCode;
    }




    public static List<PDFTextExtractor.TextBlock> getWordsCoordinateByPath(String pdfPath, String searchWord) {
        List<PDFTextExtractor.TextBlock>  data = new ArrayList<>();
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFTextExtractor extractor = new PDFTextExtractor(searchWord, false);
            extractor.setSortByPosition(true);
            // 提取每一页的文本块
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                extractor.setStartPage(i + 1);
                extractor.setEndPage(i + 1);
                extractor.getText(document);
                boolean done = extractor.getDone();
                if(done) {
                    return extractor.getTextBlocks();
                }
            }
            extractor.getTextBlocks().clear();
            document.close();
            return data;
        } catch (IOException e) {
            log.error("Error reading PDF file: {}", e.getMessage());
        }
        return data;
    }

    public static List<List<PDFTextExtractor.TextBlock>> getAllWordsCoordinateByPath(String pdfPath, String searchWord) {
        List<List<PDFTextExtractor.TextBlock>>  data = new ArrayList<>();
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFTextExtractor extractor = new PDFTextExtractor(searchWord, true);
            extractor.setSortByPosition(true);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                extractor.setStartPage(i + 1);
                extractor.setEndPage(i + 1);
                extractor.getText(document);
            }
            document.close();
            return extractor.getResults();
        } catch (IOException e) {
            log.error("Error reading PDF file: {}", e.getMessage());
        }
        return data;
    }

    private static List<List<Float>> convert(List<PDFTextExtractor.TextBlock> blocks) {
        return blocks.stream().map(block -> {
            float pageNo = (float) block.getPageNo();
            List<Float> list = new ArrayList<>();
            list.add(block.getX());
            list.add(block.getY());
            list.add(pageNo);
            return list;
        }).collect(Collectors.toList());
    }


//    public static List<FileChunkResponse.Document> extractText(String pdfPath, int chunkSize) {
//        try {
//            PDDocument document = PDDocument.load(new File(pdfPath));
//            PDFTextExtractor extractor = new PDFTextExtractor();
//            extractor.setSortByPosition(true);
//            List<FileChunkResponse.Document> data = new ArrayList<>();
//            // 提取每一页的文本块
//            for (int i = 0; i < document.getNumberOfPages(); i++) {
//                extractor.setStartPage(i + 1);
//                extractor.setEndPage(i + 1);
//                extractor.getText(document);
//                float x0 = Float.MAX_VALUE, y0 = Float.MAX_VALUE, x1 = Float.MIN_VALUE, y1 = Float.MIN_VALUE;
//                FileChunkResponse.Document tempDoc = FileChunkResponse.Document.builder()
//                        .pageNo(i+1)
//                        .build();
//                StringBuilder tempSb = new StringBuilder();
//                int currentChunkSize = 0;
//                boolean addBlank = false;
//                boolean isStart = true;
//                for (int j = 0; j < extractor.getTextBlocks().size() - 1; j++) {
//                    PDFTextExtractor.TextBlock block = extractor.getTextBlocks().get(j);
//                    System.out.println(block);
//                    x0 = Math.min(block.getX(), x0);
//                    y0 = Math.min(block.getY(), y0);
//                    x1 = Math.max(block.getX() + block.getWidth(), x1);
//                    y1 = Math.max(block.getY() + block.getHeight(), y1);
//                    if(currentChunkSize < chunkSize) {
//                        boolean isBlockBlank = block.getText().matches("\\s");
//                        if(addBlank && isBlockBlank) {
//                            continue;
//                        }
//                        addBlank = isBlockBlank;
//                        tempSb.append(block.getText());
//                        currentChunkSize++;
//                    } else  {
//                        tempDoc.setText(tempSb.toString());
//                        tempDoc.setRect(Lists.newArrayList(x0, y0, x1, y1));
//                        data.add(tempDoc);
//                        if(isStart) {
//                            tempDoc.setIsStart(true);
//                            isStart = false;
//                        }
//                        tempDoc = FileChunkResponse.Document.builder()
//                                .pageNo(block.getPageNo())
//                                .build();
//                        tempSb = new StringBuilder();
//                        tempSb.append(block.getText());
//                        currentChunkSize = block.getText().length();
//                        x0 = Math.min(block.getX(), Float.MAX_VALUE);
//                        y0 = Math.min(block.getY(), Float.MAX_VALUE);
//                        x1 = Math.max(block.getX() + block.getWidth(), Float.MIN_VALUE);
//                        y1 = Math.max(block.getY() + block.getHeight(), Float.MIN_VALUE);
//                    }
//                }
//                tempSb.append(extractor.getTextBlocks().get(extractor.getTextBlocks().size() -1).getText());
//                tempDoc.setText(tempSb.toString());
//                tempDoc.setRect(Lists.newArrayList(x0, y0, x1, y1));
//                data.add(tempDoc);
//                if(isStart) {
//                    tempDoc.setIsStart(true);
//                }
//                extractor.getTextBlocks().clear();
//            }
//            document.close();
//            return data;
//        } catch (IOException e) {
//            log.error("Error reading PDF file: {}", e.getMessage());
//        }
//        return Collections.emptyList();
//    }


    public static String getCroppedPageImage(String pdfPath, String pageDir, int pageIndex, int x0, int y0, int x1, int y1) throws IOException {
        // 加载PDF文档
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {

            // 创建PDF渲染器
            PDFRenderer renderer = new PDFRenderer(document);

            // 渲染页面为图像
//            BufferedImage fullPageImage = renderer.renderImageWithDPI(pageIndex, DPI );
            BufferedImage fullPageImage = renderer.renderImage(pageIndex, ZOOM_SIZE);
            int width = fullPageImage.getWidth();
            int height = fullPageImage.getHeight();
            x0 = Math.max(x0, 0);
            y0 = Math.max(y0, 0);
            x1 = Math.min(width, x1);
            y1 = Math.min(height, y1);
//            // 计算裁剪区域
            int croppedWidth = (x1 - x0) * ZOOM_SIZE ;
            int croppedHeight = (y1 - y0) * ZOOM_SIZE;
            int croppedX = x0 * ZOOM_SIZE ;
            int croppedY = y0 * ZOOM_SIZE;

//            // 计算裁剪区域
//            int croppedWidth = (x1 - x0) ;
//            int croppedHeight = (y1 - y0) ;
//            int croppedX = x0 ;
//            int croppedY = y0 ;
            String pageImagePath = pageDir + "/" + (pageIndex + 1) + "_" + x0 + "_" + y0 + "_" + x1 + "_" + y1 + ".png";
            System.out.println(pageImagePath);
            // 裁剪图像
            BufferedImage croppedImage = fullPageImage.getSubimage(croppedX, croppedY, croppedWidth, croppedHeight);
            System.out.println(pageImagePath + "    finished");
            // 保存图像
            File pageFile = new File(pageImagePath);
            ImageIO.write(croppedImage, "png", pageFile);
            return pageImagePath;
        }
    }

    public static void main(String[] args) {
//        String keyword = "本标准代替Q/BJCE-204.03-06-2019《员工培训管理办法》，自本标准实施之日起，原标\n" +
//                "准作废。\n" +
//                "本标准的附录A、附录B、附录C、附录D、附录E、附录F、附录G为规范性附录。\n" +
//                "本标准由清洁能源公司党委组织部（人力资源部）提出并归口管理。\n" +
//                "本标准起草部室：党委组织部（人力资源部）\n" +
//                "本标准起草人：马宴维\n" +
//                "本标准修改人：王晓东\n" +
//                "本标准审核人：曲 展\n" +
//                "本标准复核人：曹满胜\n" +
//                "本标准批准人：张凤阳\n" +
//                "本标准于2011年12月31日首次发布，2013年10月第一次修编，2016年7月第二次修编，2019\n" +
//                "年11月第三次修编,2022年3月第四次修编。\n" +
//                "人力资源管理 Q/BJCE-204.03-06-2022\n" +
//                "1\n" +
//                "员工培训管理办法\n" +
//                "1 范围\n" +
//                "本标准规定了清洁能源公司培训管理的职责、管理内容与方法、报告和记录、检查与考\n" +
//                "核。\n" +
//                "本标准适用于清洁能源公司培训管理工作。\n" +
//                "2 规范性引用文件\n" +
//                "下列文件对于本文件的应用是必不可少的。凡是注日期的引用文件，仅注日期的版本适用\n" +
//                "于本文件。凡是不注日期的引用文件，其最新版本（包括所有的修改单）适用于本文件。";
        String keyword =  "MY1.5MW风力发电机组故障处理手册工程服务总部-调试部编2011版MY1.5MW风力发电机组故障处理手册文件号：版本：V1.0IMY1.5MW风力发电机组故障处理手册编写：_______________校核：_______________批准：_______________编写日期：2011年5月MY1.5MW风力发电机组故障处理手册文件号：版本：V1.0II前言MY1.5MW风力发电机组是一套集机械、电气、液压、计算机控制及远程监控为一体的高科技产品，为确保调试及维护过程中发电机组稳定安全运行，作为工程技术人员除必须掌握必要的调试技能和调试步骤外，还必须具备一定的故障识别、处理能力。因此，学习本手册内容是非常必要的。本手册内容共分三章，详细介绍了MY1.5MW风力发电机组三大电气系统常见故障的产生原因和解决办法，并对故障处理过程中的相关注意事项做了必要说明。本手册由调试部技术研究组成员共同编写而成，其中第一部分SSB变桨部分由罗华兵编写，LTi变桨系统部分由白宗举编写，OAT变桨部分由郭卓锋编写，第二部分主控系统部分由邱长进编写，第三部分变频器部分由余秋爽编写。因时间仓促，难免有疏漏之处，欢迎各位读者批评指正。MY1.5MW风力发电机组故障处理手册有助于工程现场人员快速掌握风机调试技能和故障处理能力，同时对现场的运行维护工作也具有一定的指导意义！MY1.5MW风力发电机组故障处理手册文件号：版本：V1.0III目录第一部分：变桨系统..................................................................................................................................................................................................................................11.1、SSB变桨系统…………………………………………………………………………………………………………………………………………………..11.1.1、环境控制：......................................................................................................................................................................................................................11.1.2、变桨系统：......................................................................................................................................................................................................................21.1.3、SSB变桨系统常见故障：..............................................................................................................................................................................................41.2、LTi变桨系统……………………………………………………………………………………………………………………………………………………71.2.1、环境控制：......................................................................................................................................................................................................................71.2.2、变桨系统：......................................................................................................................................................................................................................91.2.3、LTi变桨常见故障：......................................................................................................................................................................................................151.3、OAT变桨系统…………………………………………………………………………………………………………………………………………………191.3.1、环境控制：....................................................................................................................................................................................................................191.3.2、变桨系统：....................................................................................................................................................................................................................21第二部分：主控系统................................................................................................................................................................................................................................302.1、机舱部分状态代码…………………………………………………………………………………………………………………………………………….332.1.1、机舱安全链：................................................................................................................................................................................................................332.1.2、机舱电控柜：..................................................................................................................................................................................................";
        keyword = keyword.replaceAll("\\s+", "");
        List<List<PDFTextExtractor.TextBlock>> allWordsCoordinateByPath = getAllWordsCoordinateByPath("C:\\Users\\Administrator\\Desktop\\京能\\风力发电机组故障处理手册.pdf", keyword);
        System.out.println(allWordsCoordinateByPath.size());



    }
}
