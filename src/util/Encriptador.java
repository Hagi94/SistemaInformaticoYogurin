package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase de utilidad para el cifrado de contrasenas.
 *
 * Usa bcrypt, que es lento a proposito: eso encarece los ataques de fuerza bruta.
 * Ademas agrega un salt distinto a cada clave, por lo que dos usuarios con la misma
 * contrasena quedan guardados de forma diferente en la base de datos.
 *
 * Se conserva la verificacion contra el formato anterior (SHA-256) para que ningun
 * usuario quede bloqueado; esas claves se actualizan solas al iniciar sesion.
 */
public class Encriptador {

    /** Coste del algoritmo: a mayor valor, mas lento y mas seguro. 10 = ~0.1 s por intento. */
    private static final int COSTE = 10;

    private static final String ALGORITMO_ANTERIOR = "SHA-256";
    private static final int LARGO_HASH_ANTERIOR = 64;   // (luiggi) SHA-256 en hexadecimal ocupa 64 caracteres

    private Encriptador() {                              // (luiggi) clase de utilidad, no se instancia
    }

    /** Cifra una clave en texto plano generando un salt nuevo cada vez. */
    public static String encriptar(String clavePlana) {

        if (clavePlana == null || clavePlana.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacia");
        }
        return BCrypt.hashpw(clavePlana, BCrypt.gensalt(COSTE)); // (luiggi) el salt viaja dentro del hash
    }

    /**
     * Compara la clave escrita por el usuario contra el hash guardado.
     * Acepta tanto el formato bcrypt como el SHA-256 anterior.
     */
    public static boolean verificar(String clavePlana, String hashGuardado) {

        if (clavePlana == null || hashGuardado == null || hashGuardado.isBlank()) {
            return false;                                // (luiggi) sin datos no hay coincidencia
        }

        if (esFormatoAnterior(hashGuardado)) {
            return sha256(clavePlana).equalsIgnoreCase(hashGuardado); // (luiggi) clave aun no migrada
        }

        try {
            return BCrypt.checkpw(clavePlana, hashGuardado);
        } catch (IllegalArgumentException e) {
            System.err.println("Hash con formato invalido: " + e.getMessage());
            return false;                                // (luiggi) hash corrupto: se rechaza el acceso
        }
    }

    /**
     * Indica si el hash guardado usa el formato anterior y conviene regenerarlo.
     * Permite migrar las claves de forma transparente cuando el usuario inicia sesion.
     */
    public static boolean necesitaActualizarse(String hashGuardado) {
        return esFormatoAnterior(hashGuardado);
    }

    // ------------------------------------------------------------------

    /** Un hash bcrypt siempre empieza con $2a$, $2b$ o $2y$; el anterior era hexadecimal puro. */
    private static boolean esFormatoAnterior(String hash) {
        return hash.length() == LARGO_HASH_ANTERIOR && hash.matches("[0-9a-fA-F]+");
    }

    /** Calculo SHA-256, conservado solo para validar las claves que aun no se migraron. */
    private static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITMO_ANTERIOR);
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo " + ALGORITMO_ANTERIOR + " no disponible", e);
        }
    }
}
