package com.securemsg.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

public class EncryptionUtil {
    private static final String SECRET_KEY = "1234567890123456"; // 16 bytes
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String input) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"), new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8"));
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decrypt(String encrypted) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        byte[] iv = new byte[16];
        System.arraycopy(decoded, 0, iv, 0, iv.length);
        byte[] ciphertext = new byte[decoded.length - iv.length];
        System.arraycopy(decoded, iv.length, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"), new IvParameterSpec(iv));
        byte[] original = cipher.doFinal(ciphertext);
        return new String(original, "UTF-8");
    }
}
