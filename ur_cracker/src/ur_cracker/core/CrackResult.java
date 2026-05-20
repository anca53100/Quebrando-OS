package ur_cracker.core;

/**
 * CrackResult – Value Object que encapsula el resultado de un ataque.
 *
 * Inmutable por diseño: una vez creado, sus campos no cambian.
 * Centraliza la lógica de presentación de resultados.
 */
public class CrackResult {

    private final boolean exitoso;       // true si se encontró la contraseña
    private final String  contrasena;    // Contraseña en texto plano (null si no se encontró)
    private final long    tiempoMs;      // Tiempo total del ataque en milisegundos
    private final long    intentos;      // Número de candidatos probados

    /**
     * @param exitoso    Indica si el ataque fue exitoso
     * @param contrasena Contraseña encontrada, o null si el ataque falló
     * @param tiempoMs   Duración del ataque en milisegundos
     * @param intentos   Cantidad de combinaciones probadas
     */
    public CrackResult(boolean exitoso, String contrasena, long tiempoMs, long intentos) {
        this.exitoso    = exitoso;
        this.contrasena = contrasena;
        this.tiempoMs   = tiempoMs;
        this.intentos   = intentos;
    }

    // ── Fábricas estáticas para mayor legibilidad en los engines ────────────────

    /** Crea un resultado exitoso. */
    public static CrackResult exito(String contrasena, long tiempoMs, long intentos) {
        return new CrackResult(true, contrasena, tiempoMs, intentos);
    }

    /** Crea un resultado fallido (contraseña no encontrada). */
    public static CrackResult fallo(long tiempoMs, long intentos) {
        return new CrackResult(false, null, tiempoMs, intentos);
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public boolean isExitoso()    { return exitoso; }
    public String  getContrasena(){ return contrasena; }
    public long    getTiempoMs()  { return tiempoMs; }
    public long    getIntentos()  { return intentos; }

    /** Velocidad aproximada en intentos por segundo. */
    public double getIntentosPerSeg() {
        return (tiempoMs > 0) ? intentos / (tiempoMs / 1000.0) : 0;
    }

    // ── Presentación ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String estado = exitoso ? "[OK]   " : "[FAIL] ";
        String clave  = exitoso ? "Contraseña : '" + contrasena + "'" : "No encontrada";

        return String.format(
                "%s%s%n" +
                "       Tiempo     : %.3f s%n" +
                "       Intentos   : %,d%n" +
                "       Velocidad  : %,.0f intentos/s",
                estado, clave,
                tiempoMs / 1000.0,
                intentos,
                getIntentosPerSeg()
        );
    }
}
