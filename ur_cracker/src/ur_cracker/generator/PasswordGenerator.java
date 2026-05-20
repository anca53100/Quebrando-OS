package ur_cracker.generator;

/**
 * PasswordGenerator – contrato común para todos los generadores de contraseñas.
 *
 * Al extender {@code Iterable<String>}, cualquier generador puede usarse
 * directamente en un bucle for-each, manteniendo los engines desacoplados
 * de la estrategia de generación concreta (Patrón Strategy).
 *
 * Ejemplo de uso en un engine:
 * <pre>
 *   for (String candidato : generador) {
 *       if (Hasher.verificar(candidato, tipo, hashObjetivo)) {
 *           // ¡Encontrado!
 *       }
 *   }
 * </pre>
 *
 * Implementaciones concretas:
 * - {@link BruteForceGenerator} – genera todas las permutaciones del alfabeto
 * - {@link DictionaryGenerator} – lee contraseñas línea a línea desde un archivo
 */
public interface PasswordGenerator extends Iterable<String> {

    /**
     * Descripción legible del generador para logs y mensajes de consola.
     * Debe incluir parámetros relevantes (alfabeto, longitud, archivo, etc.).
     *
     * @return Cadena descriptiva, por ejemplo:
     *         "Fuerza Bruta (alfabeto=36 chars, maxLen=5)"
     */
    String getDescripcion();
}
