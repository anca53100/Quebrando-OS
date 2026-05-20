package ur_cracker.workers;

import ur_cracker.core.HashType;
import ur_cracker.core.Hasher;

/**
 * CrackerWorker – hilo trabajador para el motor paralelo (Semana 2).
 *
 * ══════════════════════════════════════════════════════════════════
 *  ESQUELETO – El método run() está preparado para ser completado
 *              en la práctica de Semana 2
 * ══════════════════════════════════════════════════════════════════
 *
 * Cada instancia es un hilo independiente (extends Thread) que busca
 * contraseñas cuyo PRIMER carácter esté dentro del sub-alfabeto asignado.
 * Los caracteres en posiciones ≥ 1 usan el alfabeto completo.
 *
 * Mecanismo de parada cooperativa:
 *   La bandera estática `found` es `volatile`, lo que garantiza que
 *   cualquier escritura en un hilo sea visible inmediatamente por todos
 *   los demás (sin caché de CPU). En cuanto un Worker la pone en `true`,
 *   los demás la leen en su próxima iteración y terminan.
 *
 * Conceptos de SO ilustrados:
 *   - Creación de hilos con extends Thread
 *   - Variable compartida `volatile` (memoria visible entre hilos)
 *   - Exclusión mutua con `synchronized` (sección crítica al escribir resultado)
 *   - Condición de carrera y cómo prevenirla
 */
public class CrackerWorker extends Thread {

    // ── Banderas estáticas COMPARTIDAS entre todos los workers ──────────────────
    //
    // 'volatile' asegura que:
    //   a) No se guarda en caché de registro/CPU
    //   b) Cada hilo lee/escribe directamente en memoria principal
    //   c) Los cambios son visibles para todos los hilos sin sincronización adicional
    //
    /** true cuando algún worker encontró la contraseña */
    public static volatile boolean found = false;

    /** Contraseña encontrada en texto plano (null si aún no se encontró) */
    public static volatile String resultPassword = null;

    // ── Atributos de instancia (privados a cada Worker) ─────────────────────────

    private final String   hashObjetivo;     // Hash que este hilo intenta revertir
    private final HashType tipo;             // Algoritmo (MD5, SHA_1, SHA_256)
    private final String   alfabetoCompleto; // Charset completo (para posiciones > 0)
    private final String   subAlfabeto;      // Sub-rango del primer carácter de ESTE hilo
    private final int      longitudMaxima;   // Longitud máxima a probar

    private long intentos; // Contador local: cuántas combinaciones probó ESTE hilo

    /**
     * @param id               Número de hilo (0-based)
     * @param hashObjetivo     Hash hexadecimal objetivo
     * @param tipo             Algoritmo de hash
     * @param alfabetoCompleto Charset completo compartido
     * @param subAlfabeto      Sub-rango del primer carácter asignado a este hilo
     * @param longitudMaxima   Longitud máxima de contraseñas a probar
     */
    public CrackerWorker(int id, String hashObjetivo, HashType tipo,
                          String alfabetoCompleto, String subAlfabeto, int longitudMaxima) {
        this.hashObjetivo     = hashObjetivo;
        this.tipo             = tipo;
        this.alfabetoCompleto = alfabetoCompleto;
        this.subAlfabeto      = subAlfabeto;
        this.longitudMaxima   = longitudMaxima;
        this.intentos         = 0;

        // Nombre del hilo: visible en logs y en herramientas como jstack/VisualVM
        setName("Worker-" + id);
    }

    /**
     * Reinicia las banderas compartidas antes de iniciar una nueva búsqueda.
     * DEBE llamarse desde ParallelEngine antes de lanzar los workers.
     */
    public static void reset() {
        found          = false;
        resultPassword = null;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  MÉTODO run() – Punto de entrada del hilo
    //  TODO Semana 2: Implementar la búsqueda recursiva
    // ════════════════════════════════════════════════════════════════════════════

    @Override
    public void run() {
        System.out.println("[" + getName() + "] Iniciado. Sub-alfabeto: '"
                + subAlfabeto + "' (" + subAlfabeto.length() + " chars de primer posición)");

        for (int longitud = 1; longitud <= longitudMaxima && !found; longitud++) {
            char[] buffer = new char[longitud];
            buscarRecursivo(buffer, 0, longitud);
        }

        System.out.printf("[%s] Terminado. Intentos: %,d%n", getName(), intentos);
    }

    private void buscarRecursivo(char[] buffer, int pos, int longitud) {
        if (found) return;

        if (pos == longitud) {
            String candidato = new String(buffer);
            intentos++;

            if (Hasher.hashear(candidato, tipo).equals(hashObjetivo)) {
                // SECCIÓN CRÍTICA: solo el primer hilo en llegar escribe el resultado
                synchronized (CrackerWorker.class) {
                    if (!found) {
                        found          = true;
                        resultPassword = candidato;
                        System.out.println("[" + getName() + "] ¡ENCONTRADA! '" + candidato + "'");
                    }
                }
            }
            return;
        }

        // Posición 0 → solo el sub-alfabeto de este hilo; posiciones > 0 → alfabeto completo
        String charsActuales = (pos == 0) ? subAlfabeto : alfabetoCompleto;

        for (char c : charsActuales.toCharArray()) {
            if (found) return;
            buffer[pos] = c;
            buscarRecursivo(buffer, pos + 1, longitud);
        }
    }

    // ── Getter para que ParallelEngine sume los intentos de todos los workers ───
    public long getIntentos() { return intentos; }
}
