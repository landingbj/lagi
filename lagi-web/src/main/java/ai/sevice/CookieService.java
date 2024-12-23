package ai.sevice;

import ai.servlet.dto.LoginRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CookieService {
    private static final String ALGORITHM = "AES";
    private static final byte[] KEY = "landingbj-lagi25".getBytes();

    public String encodeUser(String username, String password) {
        String time = String.valueOf(System.currentTimeMillis());
        return encode(time + ":" + username + ":" + password);
    }

    public LoginRequest decodeUser(String value) {
        String[] strs = decode(value).replace("\"", "").split(":", 3);
        LoginRequest loginRequest = new LoginRequest();
        if (strs.length != 3) {
            loginRequest.setUsername("");
            loginRequest.setPassword("");
            return loginRequest;
        }
        loginRequest.setUsername(strs[1]);
        loginRequest.setPassword(strs[2]);
        return loginRequest;
    }

    public String encode(String value) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decode(String value) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedValue = Base64.getDecoder().decode(value);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        CookieService cookieService = new CookieService();
        String encoded = cookieService.encodeUser("admin", "a:dmin");
        System.out.println(encoded);
        LoginRequest decoded = cookieService.decodeUser(encoded);
        System.out.println(decoded);
    }
}
