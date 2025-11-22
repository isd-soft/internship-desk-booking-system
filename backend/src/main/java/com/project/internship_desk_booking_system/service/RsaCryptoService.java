package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.config.RsaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RsaCryptoService {

    private final RsaProperties rsaProperties;
    private PrivateKey privateKey;

    /**
     * Initializes the RSA private key from the configured properties after bean construction.
     * Throws RuntimeException if the key cannot be loaded.
     */
    @PostConstruct
    public void init() {
        try {
            String base64 = rsaProperties.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(base64);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            privateKey = keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA private key", e);
        }
    }

    /**
     * Decrypts a base64-encoded string using the RSA private key.
     * @param encryptedBase64 the encrypted base64 string
     * @return the decrypted string
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("RSA decrypt failed", e);
        }
    }

    /**
     * Tries to decrypt a value using RSA. If the value is not base64, returns it as is.
     * @param value the value to try to decrypt
     * @return decrypted value or original if not base64
     */
    public String tryDecrypt(String value) {
        try {
            Base64.getDecoder().decode(value);

            return decrypt(value);
        } catch (Exception e) {
            return value;
        }
    }

}
