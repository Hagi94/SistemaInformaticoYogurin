package Utilidades;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SegurridadUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SegurridadUtil() {
    }

    public static String generarSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return bytesAHex(salt);
    }

    public static String hashearConSalt(String valorPlano, String salt) {
        if (valorPlano == null || salt == null) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + valorPlano).getBytes(StandardCharsets.UTF_8));
            return bytesAHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo aplicar SHA-256", e);
        }
    }

    public static boolean verificar(String valorPlano, String salt, String hashEsperado) {
        if (valorPlano == null || salt == null || hashEsperado == null) {
            return false;
        }
        String hashCalculado = hashearConSalt(valorPlano, salt);
        return hashCalculado.equals(hashEsperado);
    }

    private static String bytesAHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
