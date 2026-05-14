package br.com.fiap.rei_dos_piratas.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class HmacUtil {

    @Value("${ME_SECRET}")
    private String secret;

    public String generateHmac(String payload){

        Mac sha256Hmac = null;

        try {
            sha256Hmac = Mac.getInstance("HmacSHA256");


        SecretKeySpec secretKey =
                new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        sha256Hmac.init(secretKey);

        byte[] signedBytes =
                sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(signedBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}
