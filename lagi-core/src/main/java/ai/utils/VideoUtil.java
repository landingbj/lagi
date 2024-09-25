//package ai.utils;
//
//import cn.hutool.core.lang.UUID;
//import org.bytedeco.javacv.*;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//
//public class VideoUtil{
//
//    //调用原子线程判断
//    private static AtomicBoolean running = new AtomicBoolean(true);
//
//    public String streamURL;// 流地址
//
//    public String filePath;// 视频文件路径
//
//    public String imagePath;// 图片路径,存放截取视频某一帧的图片
//
//
//
//    public void setStreamURL(String streamURL) {
//        this.streamURL = streamURL;
//    }
//
//    public void setFilePath(String filePath) {
//        this.filePath = filePath;
//    }
//
//    public void setImagePath(String imagePath) {
//        this.imagePath = imagePath;
//    }
//
//    /**
//     * frame 转图片流
//     * @param frame
//     * @return
//     */
//    public static BufferedImage FrameToBufferedImage(Frame frame) {
//        //创建BufferedImage对象
//        Java2DFrameConverter converter = new Java2DFrameConverter();
//        return converter.getBufferedImage(frame);
//    }
//
//
//    /**
//     * 执行视频流抓取
//     */
//    public Map<String,Object> run(String uuid,boolean screenshot) {
//        Map<String,Object> resultMap = new HashMap<>(3);
//        Map<String,Object> map = new LinkedHashMap<>(5);
//        map.put("screenshot",screenshot);
//        System.out.println(streamURL);
//        // 获取视频源
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamURL);
////        FFmpegFrameRecorder recorder = null;
//        try {
//            grabber.start();
//            Frame frame = grabber.grabFrame();
//            if (frame != null) {
//                File outFile = new File(filePath);
//                if (!outFile.isFile()) {
//                    try {
//                        outFile.createNewFile();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
////                // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
////                recorder = new FFmpegFrameRecorder(filePath, 1920, 1440, 1);
////                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 直播流格式
////                recorder.setFormat("flv");// 录制的视频格式
////                recorder.setFrameRate(25);// 帧数
////                //百度翻译的比特率，默认400000，但是我400000贼模糊，调成800000比较合适
////                recorder.setVideoBitrate(800000);
////                recorder.start();
//                int flag = 0;
//                while (frame != null && VideoUtil.running.get()) {
//                    //视频快照
//                    if(screenshot) {
//                        if (flag % 30 == 0) {
//                            frame = grabber.grabImage();
//                            //文件储存对象
//                            String fileName = imagePath + uuid + "_" + flag + ".jpg";
//                            System.out.println(fileName);
//                            File outPut = new File(fileName);
//                            ImageIO.write(FrameToBufferedImage(frame), "jpg", outPut);
//                            map.put("file" + flag, fileName);
//                            Thread.sleep(1000);
//                            if (flag == 600){
//                                //结束视频快照
//                                screenshot = false;
//                            }
//                        }
//                    }
//
////                    recorder.record(frame);// 录制
//                    frame = grabber.grabFrame();// 获取下一帧
//                    flag++;
//                }
////                recorder.record(frame);
//                // 停止录制
////                recorder.stop();
//                grabber.stop();
//                //视频文件地址
//                map.put("file",outFile);
//                resultMap.put(uuid,map);
//                //转成JSON存储到redis
//                //redisUtil.set(uuid,resultMap,60*60);
//            }
//        } catch (FrameGrabber.Exception e) {
//            e.printStackTrace();
//        } catch (FrameRecorder.Exception e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (null != grabber) {
//                try {
//                    grabber.stop();
//                } catch (FrameGrabber.Exception e) {
//                    e.printStackTrace();
//                }
//            }
////            if (recorder != null) {
////                try {
////                    recorder.stop();
////                } catch (FrameRecorder.Exception e) {
////                    e.printStackTrace();
////                }
////            }
//        }
//        VideoUtil.running.set(true);
//        return  resultMap;
//    }
//
//
//
//
//
//    /**
//     * 测试：
//     * 1、在D盘中新建一个test文件夹，test中再分成video和img，在video下存入一个视频，并命名为test
//     * D:/test/video     D:/test/img
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        //getVideoStream();
//        String result = getStreamScreenshot("C:\\Users\\Administrator\\Desktop\\temp\\1.flv","C:\\Users\\Administrator\\Desktop\\temp\\","http://192.168.0.33:8380//live?app=demo&stream=home");
//        System.out.println(result);
//    }
//
//
//
//    /**
//     * 获取视频流
//     * @param filePath
//     * @param streamURL
//     */
//    public static String getVideoStream(String filePath,String imagePath,String streamURL){
//        return getStream(filePath,imagePath,streamURL,false);
//    }
//
//    /**
//     * 获取视频流快照
//     * @param filePath
//     * @param streamURL
//     */
//    public static String getStreamScreenshot(String filePath,String imagePath,String streamURL){
//        return getStream(filePath,imagePath,streamURL,true);
//    }
//
//    /**
//     * 开始执行下拉流截视频流
//     */
//    public static String getStream(String filePath,String imagePath,String streamURL,boolean screenshot){
//        VideoUtil videoUtil = new VideoUtil();
//        videoUtil.setFilePath(filePath);
//        videoUtil.setImagePath(imagePath);
//        videoUtil.setStreamURL(streamURL);
//        String uuid = UUID.randomUUID().toString();
//        try {
//            Thread t0 = new Thread(() -> {
//                System.out.println("start...");
//                videoUtil.run(uuid,screenshot);
//            });
//            t0.start();
//            Thread.sleep(60000);
//            System.out.println("stop...");
//            videoUtil.running.set(false);
//        } catch (Throwable t) {
//            System.out.println("Caught in main: " + t);
//            t.printStackTrace();
//        }
//        return  uuid;
//    }
//
//
//}