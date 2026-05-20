package ur_cracker.core;

/**
 * HashType – enumeración de los algoritmos de hashing soportados.
 *
 * Cada constante almacena el nombre exacto que reconoce
 * java.security.MessageDigest, evitando strings mágicos dispersos
 * por el código.
 */
public enum HashType {

    /** MD5 – 128 bits, rápido, no recomendado para contraseñas reales */
    MD5("MD5"),

    /** SHA-1 – 160 bits, deprecado para criptografía moderna */
    SHA_1("SHA-1"),

    /** SHA-256 – 256 bits, estándar actual */
    SHA_256("SHA-256");

    // Nombre que espera MessageDigest.getInstance(...)
    private final String nombreAlgoritmo;

    HashType(String nombreAlgoritmo) {
        this.nombreAlgoritmo = nombreAlgoritmo;
    }

    /** Devuelve el nombre del algoritmo tal como lo requiere la JCA. */
    public String getNombreAlgoritmo() {
        return nombreAlgoritmo;
    }

    /**
     * Busca un HashType a partir de su nombre de algoritmo (insensible a mayúsculas).
     * Útil para parsear la entrada del usuario.
     *
     * @param nombre  p.ej. "MD5", "sha-256"
     * @return El HashType correspondiente, o MD5 por defecto si no se reconoce.
     */
    public static HashType desdeCadena(String nombre) {
        for (HashType tipo : values()) {
            if (tipo.nombreAlgoritmo.equalsIgnoreCase(nombre)
                    || tipo.name().equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        return MD5; // valor por defecto seguro
    }
}
