package ai.migrate.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SecurityUtil {
	private static String secretKey = "SixteenByteKeya!";
	private static String iv = "SixteenByteIvab1";

	public static String aesDecrypt(String encryptedMessage) {
		String decryptedMessage = "";
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedMessage));
			decryptedMessage = new String(decryptedBytes, "UTF-8");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return decryptedMessage;
	}

	public static void main(String[] args) {
	}
}
