package com.petarmc.fishtracker.client;

import com.petarmc.lib.log.PLog;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class EncryptionManager {

    private static final PLog log = new PLog("EncryptionManager");

    private byte[] signingKey;
    private byte[] encryptionKey;
    private final SecureRandom rng = new SecureRandom();


    public void setKey(String fernetKeyBase64) {
        byte[] key = Base64.getDecoder().decode(fernetKeyBase64);

        if (key.length != 32) {
            throw new IllegalArgumentException("Invalid Fernet key length (expected 32 bytes)");
        }

        signingKey = new byte[16];
        encryptionKey = new byte[16];
        System.arraycopy(key, 0, signingKey, 0, 16);
        System.arraycopy(key, 16, encryptionKey, 0, 16);

        log.info("Fernet key loaded successfully");
    }


    public String encrypt(String json) throws Exception {
        if (signingKey == null || encryptionKey == null)
            throw new IllegalStateException("Fernet key not set");

        long timestamp = Instant.now().getEpochSecond();


        byte[] iv = new byte[16];
        rng.nextBytes(iv);


        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec aesKey = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));


        ByteBuffer token = ByteBuffer.allocate(1 + 8 + 16 + ciphertext.length);
        token.put((byte) 0x80);
        token.putLong(timestamp);
        token.put(iv);
        token.put(ciphertext);

        byte[] tokenBytes = token.array();


        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
        byte[] hmacBytes = hmac.doFinal(tokenBytes);

        // final token = token + hmac
        ByteBuffer finalToken = ByteBuffer.allocate(tokenBytes.length + 32);
        finalToken.put(tokenBytes);
        finalToken.put(hmacBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(finalToken.array());
    }
}
