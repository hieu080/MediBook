package com.identityservice.security;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
public class JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProvider.class);

    private final JwtProperties jwtProperties;
    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        KeyPair keyPair = resolveKeyPair(jwtProperties);
        this.privateKey = keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    public String getKeyId() {
        return jwtProperties.getKeyId();
    }

    public String getModulus() {
        return base64UrlUnsigned(publicKey.getModulus());
    }

    public String getPublicExponent() {
        return base64UrlUnsigned(publicKey.getPublicExponent());
    }

    private KeyPair resolveKeyPair(JwtProperties properties) {
        if (hasText(properties.getPrivateKey()) || hasText(properties.getPrivateKeyPath())) {
            return loadConfiguredKeyPair(
                    readKey(properties.getPrivateKey(), properties.getPrivateKeyPath(), "JWT private key"),
                    readKey(properties.getPublicKey(), properties.getPublicKeyPath(), "JWT public key")
            );
        }
        return generateDevelopmentKeyPair();
    }

    private KeyPair loadConfiguredKeyPair(String privateKeyPem, String publicKeyPem) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodeKey(privateKeyPem)));
            PublicKey publicKey = hasText(publicKeyPem)
                    ? keyFactory.generatePublic(new X509EncodedKeySpec(decodeKey(publicKeyPem)))
                    : derivePublicKey(keyFactory, privateKey);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid RSA JWT key configuration", ex);
        }
    }

    private PublicKey derivePublicKey(KeyFactory keyFactory, PrivateKey privateKey) throws Exception {
        if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
            throw new IllegalStateException("JWT public key is required when private key is not RSA CRT");
        }
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                rsaPrivateKey.getModulus(),
                rsaPrivateKey.getPublicExponent()
        );
        return keyFactory.generatePublic(publicKeySpec);
    }

    private KeyPair generateDevelopmentKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            log.warn("JWT RSA key is generated at startup. Configure JWT_PRIVATE_KEY or JWT_PRIVATE_KEY_PATH for stable tokens.");
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate RSA JWT key pair", ex);
        }
    }

    private String readKey(String inlineValue, String pathValue, String label) {
        if (hasText(inlineValue)) {
            return inlineValue;
        }
        if (!hasText(pathValue)) {
            return null;
        }
        try {
            return Files.readString(Path.of(pathValue));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read " + label + " from " + pathValue, ex);
        }
    }

    private byte[] decodeKey(String value) {
        String normalized = value
                .replace("\\n", "\n")
                .replaceAll("-----BEGIN ([A-Z ]+)-----", "")
                .replaceAll("-----END ([A-Z ]+)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private String base64UrlUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] unsigned = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, unsigned, 0, unsigned.length);
            bytes = unsigned;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
