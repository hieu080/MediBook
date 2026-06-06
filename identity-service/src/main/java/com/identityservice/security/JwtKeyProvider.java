package com.identityservice.security;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        if (hasText(properties.getPrivateKey())) {
            return loadConfiguredKeyPair(
                    properties.getPrivateKey(),
                    readOptionalKey(properties.getPublicKey(), properties.getPublicKeyPath(), "JWT public key")
            );
        }
        if (hasText(properties.getPrivateKeyPath())) {
            return loadOrCreateConfiguredKeyPair(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
        }
        return generateDevelopmentKeyPair();
    }

    private KeyPair loadOrCreateConfiguredKeyPair(String privateKeyPathValue, String publicKeyPathValue) {
        Path privateKeyPath = Path.of(privateKeyPathValue);
        Path publicKeyPath = hasText(publicKeyPathValue) ? Path.of(publicKeyPathValue) : null;
        if (!Files.exists(privateKeyPath)) {
            return generateAndPersistDevelopmentKeyPair(privateKeyPath, publicKeyPath);
        }
        KeyPair keyPair = loadConfiguredKeyPair(
                readRequiredKey(privateKeyPath, "JWT private key"),
                readOptionalKey(null, publicKeyPathValue, "JWT public key")
        );
        if (publicKeyPath != null && !Files.exists(publicKeyPath)) {
            writePem(publicKeyPath, "PUBLIC KEY", keyPair.getPublic().getEncoded(), "JWT public key");
        }
        return keyPair;
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
            log.warn("JWT RSA key is generated in memory at startup. Configure JWT_PRIVATE_KEY_PATH for stable tokens.");
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate RSA JWT key pair", ex);
        }
    }

    private KeyPair generateAndPersistDevelopmentKeyPair(Path privateKeyPath, Path publicKeyPath) {
        KeyPair keyPair = generateDevelopmentKeyPair();
        writePem(privateKeyPath, "PRIVATE KEY", keyPair.getPrivate().getEncoded(), "JWT private key");
        if (publicKeyPath != null) {
            writePem(publicKeyPath, "PUBLIC KEY", keyPair.getPublic().getEncoded(), "JWT public key");
        }
        log.warn("JWT RSA key pair is generated and saved to {}{}", privateKeyPath,
                publicKeyPath != null ? " and " + publicKeyPath : "");
        return keyPair;
    }

    private String readOptionalKey(String inlineValue, String pathValue, String label) {
        if (hasText(inlineValue)) {
            return inlineValue;
        }
        if (!hasText(pathValue)) {
            return null;
        }
        Path path = Path.of(pathValue);
        if (!Files.exists(path)) {
            return null;
        }
        return readRequiredKey(path, label);
    }

    private String readRequiredKey(Path path, String label) {
        try {
            return Files.readString(path);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read " + label + " from " + path, ex);
        }
    }

    private void writePem(Path path, String type, byte[] encoded, String label) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, toPem(type, encoded), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to write " + label + " to " + path, ex);
        }
    }

    private String toPem(String type, byte[] encoded) {
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
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
