package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.config.RsaProperties;
import com.project.internship_desk_booking_system.service.RsaCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RsaCryptoServiceTest {
    private RsaCryptoService rsaCryptoService;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        String base64Private = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String pem = "-----BEGIN PRIVATE KEY-----\n" + base64Private + "\n-----END PRIVATE KEY-----";

        RsaProperties props = Mockito.mock(RsaProperties.class);
        Mockito.when(props.getPrivateKey()).thenReturn(pem);
        rsaCryptoService = new RsaCryptoService(props);
        rsaCryptoService.init();
    }

    @Test
    void testDecryptAndTryDecrypt() throws Exception {
        String original = "test1234";
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(original.getBytes(StandardCharsets.UTF_8));
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

        String decrypted = rsaCryptoService.decrypt(encryptedBase64);
        assertEquals(original, decrypted);

        assertEquals(original, rsaCryptoService.tryDecrypt(encryptedBase64));
        assertEquals("plain", rsaCryptoService.tryDecrypt("plain"));
    }

    @Test
    void testDecryptThrowsOnInvalid() {
        assertThrows(RuntimeException.class, () -> rsaCryptoService.decrypt("notbase64"));
    }
}

