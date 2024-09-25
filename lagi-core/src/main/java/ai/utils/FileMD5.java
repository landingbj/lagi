package ai.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileMD5 {

    public static void main(String[] args) {
        String pdfPath = "C:\\Users\\Administrator\\Desktop\\京能\\06-员工培训管理办法.pdf"; // 替换为你的文件路径
        try {
            String md5 = calculateMD5(pdfPath);
            System.out.println("MD5: " + md5);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算文件的MD5值
     *
     * @param filePath 文件路径
     * @return MD5字符串
     * @throws IOException 如果读取文件失败
     * @throws NoSuchAlgorithmException 如果没有找到算法
     */
    public static String calculateMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] dataBytes = new byte[4096];
            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        }
        byte[] mdbytes = md.digest();

        // convert the byte to hex format method 1
        StringBuilder hexString = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            hexString.append(Integer.toHexString(0xFF & mdbyte));
        }
        return hexString.toString();
    }
}
