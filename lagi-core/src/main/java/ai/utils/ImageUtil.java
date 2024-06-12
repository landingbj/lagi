package ai.utils;

import sun.misc.BASE64Decoder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class ImageUtil {

    public static String getFileContentAsBase64(String path) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(b);
    }

    public static File base64ToFile(String base64) throws Exception {
        if(base64.contains("data:image")){
            base64 = base64.substring(base64.indexOf(",")+1);
        }
        base64 = base64.toString().replace("\r\n", "");
        //创建文件目录
        String prefix=".jpeg";
        File file = File.createTempFile(UUID.randomUUID().toString(), prefix);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes =  decoder.decodeBuffer(base64);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        }finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }


}
