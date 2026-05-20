package ur_cracker.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

/**
 * Hasher – capa de abstracción sobre java.security.MessageDigest.
 *
 * Convierte texto plano en su representación hexadecimal según el
 * algoritmo indicado por HashType. Todos los métodos son estáticos
 * para facilitar su uso sin instanciación.
 */
public class Hasher {

    // Constructor privado: clase de utilidad, no debe instanciarse
    private Hasher() {}

    /**
     * Genera el hash hexadecimal de un texto.
     *
     * @param texto Texto a hashear (ej: "password123")
     * @param tipo  Algoritmo deseado (MD5, SHA_1, SHA_256)
     * @return Cadena hexadecimal en minúsculas con el hash resultante
     * @throws RuntimeException si el algoritmo no está disponible en la JVM
     */
    public static String hashear(String texto, HashType tipo) {
        try {
            // Obtener la instancia del motor criptográfico
            MessageDigest md = MessageDigest.getInstance(tipo.getNombreAlgoritmo());

            // Convertir el texto a bytes UTF-8 y calcular el digest
            byte[] bytesHash = md.digest(texto.getBytes("UTF-8"));

            // Convertir el arreglo de bytes a su representación hexadecimal
            StringBuilder hexBuilder = new StringBuilder(bytesHash.length * 2);
            for (byte b : bytesHash) {
                // Cada byte → 2 dígitos hex en minúsculas, con relleno de cero
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            // No debería ocurrir: MD5, SHA-1 y SHA-256 son obligatorios en la JCA
            throw new RuntimeException("Algoritmo no disponible en esta JVM: "
                    + tipo.getNombreAlgoritmo(), e);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 está garantizado en cualquier JVM estándar
            throw new RuntimeException("Codificación UTF-8 no disponible.", e);
        }
    }

    /**
     * Comprueba si el hash de un texto coincide con un hash dado.
     * Útil para evitar comparaciones directas de strings en el código cliente.
     *
     * @param texto       Texto candidato
     * @param tipo        Algoritmo de hash
     * @param hashObjetivo Hash que se desea verificar
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verificar(String texto, HashType tipo, String hashObjetivo) {
        return hashear(texto, tipo).equals(hashObjetivo);
    }
}
