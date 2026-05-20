package ur_cracker.engine;

import ur_cracker.core.CrackResult;
import ur_cracker.core.HashType;
import ur_cracker.workers.CrackerWorker;

import java.util.ArrayList;
import java.util.List;

/**
 * ParallelEngine – motor de ataque con MÚLTIPLES HILOS (Semana 2).
 *
 * ══════════════════════════════════════════════════════════════════
 *  ESQUELETO – Implementación completa disponible en Semana 2
 * ══════════════════════════════════════════════════════════════════
 *
 * Estrategia de división del trabajo:
 *   El alfabeto se divide entre N hilos asignando a cada uno un
 *   sub-rango del PRIMER carácter. Los caracteres en posiciones
 *   posteriores siempre usan el alfabeto completo.
 *
 *   Ejemplo con alfabeto "abcdefghijklmnopqrstuvwxyz" (26 chars) y 2 hilos:
 *     Worker-0 → primer carácter de 'a' a 'm' (13 chars)
 *     Worker-1 → primer carácter de 'n' a 'z' (13 chars)
 *
 * Conceptos de SO ilustrados:
 *   - Creación y ciclo de vida de hilos (Thread.start(), Thread.join())
 *   - Variable compartida volatile (CrackerWorker.found)
 *   - Sección crítica con synchronized
 *   - Speedup (Ley de Amdahl)
 */
public class ParallelEngine {

    private final String   alfabeto;        // Charset completo a repartir entre hilos
    private final int      longitudMaxima;  // Longitud máxima de contraseña
    private final int      numHilos;        // Número de hilos a lanzar

    // Resultados recolectados después del join()
    private long tiempoMs;       // Tiempo total (wall-clock) del ataque paralelo
    private long intentosTotales; // Suma de intentos de todos los workers

    /**
     * @param alfabeto       Charset completo (ej: "abcdefghijklmnopqrstuvwxyz0123456789")
     * @param longitudMaxima Longitud máxima de contraseñas a probar
     * @param numHilos       Número de hilos a lanzar (≥ 1)
     */
    public ParallelEngine(String alfabeto, int longitudMaxima, int numHilos) {
        this.alfabeto       = alfabeto;
        this.longitudMaxima = longitudMaxima;
        this.numHilos       = numHilos;
    }

    /**
     * Lanza N CrackerWorkers en paralelo, espera a que terminen y retorna
     * el resultado consolidado.
     *
     * @param hashObjetivo Hash en hexadecimal a revertir
     * @param tipo         Algoritmo de hash
     * @return CrackResult con contraseña encontrada (o fallo si no se halló)
     */
    public CrackResult crack(String hashObjetivo, HashType tipo) {

        // Paso 1: limpiar banderas compartidas de una búsqueda anterior
        CrackerWorker.reset();

        // Paso 2: ajustar hilos si el alfabeto es más pequeño que numHilos
        int hilosEfectivos = Math.min(numHilos, alfabeto.length());

        System.out.println("\n[Paralelo] Iniciando ataque...");
        System.out.println("[Paralelo]   Hilos efectivos  : " + hilosEfectivos);
        System.out.println("[Paralelo]   Alfabeto         : " + alfabeto.length() + " caracteres");
        System.out.println("[Paralelo]   Longitud máxima  : " + longitudMaxima);
        System.out.println("[Paralelo]   Hash objetivo    : " + hashObjetivo);
        System.out.println();

        // Paso 3: calcular tamaño del sub-rango por hilo
        int chunkSize = alfabeto.length() / hilosEfectivos;

        // Paso 4: crear workers asignando a cada uno un sub-alfabeto del primer carácter
        List<CrackerWorker> workers = new ArrayList<>();
        for (int i = 0; i < hilosEfectivos; i++) {
            int startIdx   = i * chunkSize;
            int endIdx     = (i == hilosEfectivos - 1)
                             ? alfabeto.length() - 1
                             : (i + 1) * chunkSize - 1;
            String subAlfabeto = alfabeto.substring(startIdx, endIdx + 1);
            CrackerWorker w = new CrackerWorker(
                    i, hashObjetivo, tipo, alfabeto, subAlfabeto, longitudMaxima);
            workers.add(w);
        }

        // Paso 5: medir tiempo de inicio y lanzar todos los hilos
        long inicio = System.currentTimeMillis();
        for (CrackerWorker w : workers) { w.start(); }

        // Paso 6: esperar a que TODOS los hilos terminen (join)
        for (CrackerWorker w : workers) {
            try { w.join(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // Paso 7: medir tiempo total y sumar intentos de todos los workers
        this.tiempoMs        = System.currentTimeMillis() - inicio;
        this.intentosTotales = workers.stream().mapToLong(CrackerWorker::getIntentos).sum();

        // Paso 8: construir y retornar el resultado
        if (CrackerWorker.resultPassword != null) {
            return CrackResult.exito(CrackerWorker.resultPassword, tiempoMs, intentosTotales);
        }
        return CrackResult.fallo(tiempoMs, intentosTotales);
    }

    public long getTiempoMs()        { return tiempoMs; }
    public long getIntentosTotales() { return intentosTotales; }
}
