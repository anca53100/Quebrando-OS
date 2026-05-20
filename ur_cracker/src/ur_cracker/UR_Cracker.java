package ur_cracker;

import ur_cracker.core.CrackResult;
import ur_cracker.core.HashType;
import ur_cracker.engine.ParallelEngine;
import ur_cracker.engine.SequentialEngine;
import ur_cracker.generator.BruteForceGenerator;
import ur_cracker.generator.DictionaryGenerator;
import ur_cracker.util.HashMaker;

import java.util.Scanner;

/**
 * UR_Cracker – punto de entrada principal y menú interactivo.
 *
 * Orquesta todos los módulos del proyecto:
 *   1. Generar hash             → HashMaker + Hasher
 *   2. Fuerza Bruta Secuencial  → BruteForceGenerator + SequentialEngine
 *   3. Ataque por Diccionario   → DictionaryGenerator + SequentialEngine
 *   4. Fuerza Bruta Paralela    → [Semana 2] ParallelEngine + CrackerWorker
 *   5. Comparativa automática   → [Semana 2]
 *
 * IMPORTANTE: ejecutar siempre desde la raíz del proyecto (ur_cracker/)
 * para que la ruta "data/dictionary.txt" sea resuelta correctamente.
 *   Ejemplo: java -cp build ur_cracker.UR_Cracker
 */
public class UR_Cracker {

    // ── Configuración global de la aplicación ────────────────────────────────────
    private static final String ALFABETO_COMPLETO  = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALFABETO_LETRAS    = "abcdefghijklmnopqrstuvwxyz";
    private static final String RUTA_DICCIONARIO   = "data/dictionary.txt";
    private static final int    LONG_MAX_DEFAULT   = 5;
    private static final int    NUM_HILOS          = 2;

    private static final Scanner scanner = new Scanner(System.in);

    // ════════════════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ════════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        imprimirBanner();

        boolean corriendo = true;
        while (corriendo) {
            imprimirMenu();
            int opcion = leerEntero("Seleccione una opción: ");
            System.out.println();

            switch (opcion) {
                case 1:
                    opGenerarHash();
                    break;
                case 2:
                    opFuerzaBrutaSecuencial();
                    break;
                case 3:
                    opDiccionario();
                    break;
                case 4:
                    opParalelo();
                    break;
                case 5:
                    opComparativa();
                    break;
                case 0:
                    corriendo = false;
                    System.out.println("Saliendo de UR-Cracker. ¡Hasta pronto!");
                    break;
                default:
                    System.out.println("[!] Opción no válida. Elija entre 0 y 5.");
            }

            // Pausa entre opciones para que el usuario lea el resultado
            if (corriendo) {
                System.out.print("\nPresione ENTER para volver al menú...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  OPCIÓN 1 – Generar hash
    // ════════════════════════════════════════════════════════════════════════════

    private static void opGenerarHash() {
        System.out.println("═══ GENERAR HASH ════════════════════════════════");
        System.out.print("Ingrese el texto: ");
        String texto = scanner.nextLine().trim();

        if (texto.isEmpty()) {
            System.out.println("[!] El texto no puede estar vacío.");
            return;
        }

        // HashMaker.imprimirHashes muestra MD5, SHA-1 y SHA-256 de una vez
        HashMaker.imprimirHashes(texto);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  OPCIÓN 2 – Fuerza bruta secuencial (1 hilo)
    // ════════════════════════════════════════════════════════════════════════════

    private static void opFuerzaBrutaSecuencial() {
        System.out.println("═══ FUERZA BRUTA SECUENCIAL (1 hilo) ════════════");

        // Recopilar parámetros del usuario
        System.out.print("Hash objetivo: ");
        String hashObjetivo = scanner.nextLine().trim();
        if (hashObjetivo.isEmpty()) {
            System.out.println("[!] El hash no puede estar vacío.");
            return;
        }

        HashType tipo    = leerTipoHash();
        int      maxLen  = leerEnteroConDefault("Longitud máxima", LONG_MAX_DEFAULT);

        System.out.print("Alfabeto (ENTER para '" + ALFABETO_COMPLETO + "'): ");
        String alfabeto = scanner.nextLine().trim();
        if (alfabeto.isEmpty()) alfabeto = ALFABETO_COMPLETO;

        // Crear el generador y el motor, luego ejecutar
        BruteForceGenerator generador = new BruteForceGenerator(alfabeto, maxLen);
        SequentialEngine    engine    = new SequentialEngine(generador);
        CrackResult         resultado = engine.crack(hashObjetivo, tipo);

        System.out.println("\n" + resultado);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  OPCIÓN 3 – Ataque por diccionario
    // ════════════════════════════════════════════════════════════════════════════

    private static void opDiccionario() {
        System.out.println("═══ ATAQUE POR DICCIONARIO ══════════════════════");

        System.out.print("Hash objetivo: ");
        String hashObjetivo = scanner.nextLine().trim();
        if (hashObjetivo.isEmpty()) {
            System.out.println("[!] El hash no puede estar vacío.");
            return;
        }

        HashType tipo = leerTipoHash();

        // El DictionaryGenerator lee data/dictionary.txt (relativo al directorio de ejecución)
        DictionaryGenerator generador = new DictionaryGenerator(RUTA_DICCIONARIO);
        SequentialEngine    engine    = new SequentialEngine(generador);
        CrackResult         resultado = engine.crack(hashObjetivo, tipo);

        System.out.println("\n" + resultado);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  OPCIÓN 4 – Fuerza bruta paralela (esqueleto Semana 2)
    // ════════════════════════════════════════════════════════════════════════════

    private static void opParalelo() {
        System.out.println("═══ FUERZA BRUTA PARALELA (" + NUM_HILOS + " hilos) ══════════");

        System.out.print("Hash objetivo: ");
        String hashObjetivo = scanner.nextLine().trim();
        if (hashObjetivo.isEmpty()) {
            System.out.println("[!] El hash no puede estar vacío.");
            return;
        }

        HashType tipo   = leerTipoHash();
        int      maxLen = leerEnteroConDefault("Longitud máxima", LONG_MAX_DEFAULT);

        System.out.print("Alfabeto (ENTER para '" + ALFABETO_COMPLETO + "'): ");
        String alfabeto = scanner.nextLine().trim();
        if (alfabeto.isEmpty()) alfabeto = ALFABETO_COMPLETO;

        ParallelEngine engine    = new ParallelEngine(alfabeto, maxLen, NUM_HILOS);
        CrackResult    resultado = engine.crack(hashObjetivo, tipo);

        System.out.println("\n" + resultado);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  OPCIÓN 5 – Comparativa automática secuencial vs paralelo
    // ════════════════════════════════════════════════════════════════════════════

    private static void opComparativa() {
        System.out.println("═══ COMPARATIVA SECUENCIAL vs PARALELA ══════════");

        System.out.print("Hash objetivo: ");
        String hashObjetivo = scanner.nextLine().trim();
        if (hashObjetivo.isEmpty()) {
            System.out.println("[!] El hash no puede estar vacío.");
            return;
        }

        HashType tipo    = leerTipoHash();
        int      maxLen  = leerEnteroConDefault("Longitud máxima", LONG_MAX_DEFAULT);

        System.out.print("Alfabeto (ENTER para '" + ALFABETO_COMPLETO + "'): ");
        String alfabeto = scanner.nextLine().trim();
        if (alfabeto.isEmpty()) alfabeto = ALFABETO_COMPLETO;

        // ── Ataque secuencial ────────────────────────────────────────────────────
        System.out.println("\n[Comparativa] Ejecutando ataque SECUENCIAL...");
        BruteForceGenerator genSeq   = new BruteForceGenerator(alfabeto, maxLen);
        SequentialEngine    seqEngine = new SequentialEngine(genSeq);
        CrackResult         resSeq   = seqEngine.crack(hashObjetivo, tipo);

        // ── Ataque paralelo ──────────────────────────────────────────────────────
        System.out.println("\n[Comparativa] Ejecutando ataque PARALELO (" + NUM_HILOS + " hilos)...");
        ParallelEngine parallelEngine = new ParallelEngine(alfabeto, maxLen, NUM_HILOS);
        CrackResult    resPar         = parallelEngine.crack(hashObjetivo, tipo);

        // ── Tabla de resultados ──────────────────────────────────────────────────
        double tSeq     = resSeq.getTiempoMs() / 1000.0;
        double tPar     = resPar.getTiempoMs() / 1000.0;
        double speedup  = (tPar > 0) ? tSeq / tPar : 0;
        double eficiencia = speedup / NUM_HILOS * 100;

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║               TABLA COMPARATIVA                      ║");
        System.out.println("╠══════════════════╦═══════════════╦═══════════════════╣");
        System.out.println("║ Métrica          ║  Secuencial   ║     Paralelo       ║");
        System.out.println("╠══════════════════╬═══════════════╬═══════════════════╣");
        System.out.printf ("║ Tiempo (s)       ║ %13.3f ║ %17.3f ║%n", tSeq, tPar);
        System.out.printf ("║ Intentos         ║ %,13d ║ %,17d ║%n",
                resSeq.getIntentos(), resPar.getIntentos());
        System.out.printf ("║ Velocidad (i/s)  ║ %,13.0f ║ %,17.0f ║%n",
                resSeq.getIntentosPerSeg(), resPar.getIntentosPerSeg());
        System.out.printf ("║ Resultado        ║ %13s ║ %17s ║%n",
                resSeq.isExitoso() ? resSeq.getContrasena() : "No hallada",
                resPar.isExitoso() ? resPar.getContrasena() : "No hallada");
        System.out.println("╠══════════════════╩═══════════════╩═══════════════════╣");
        System.out.printf ("║ Speedup (tSeq/tPar)  : %-6.2f x                      ║%n", speedup);
        System.out.printf ("║ Eficiencia           : %-6.1f %%  (ideal=100%%)        ║%n", eficiencia);
        System.out.printf ("║ Hilos usados         : %-6d                           ║%n", NUM_HILOS);
        System.out.println("╚═══════════════════════════════════════════════════════╝");

        if (speedup >= 1.0) {
            System.out.printf("%nEl motor paralelo fue %.2fx más rápido que el secuencial.%n", speedup);
        } else if (tPar > 0) {
            System.out.println("\nEl motor secuencial fue más rápido (overhead de hilos supera el beneficio para este tamaño).");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  MÉTODOS AUXILIARES DE INTERFAZ
    // ════════════════════════════════════════════════════════════════════════════

    private static void imprimirBanner() {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║             UR-CRACKER  v2.0  (modular)            ║");
        System.out.println("║    Herramienta Educativa de Recuperación de Hashes  ║");
        System.out.println("║            Sistemas Operativos – Univ. Rosario      ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void imprimirMenu() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║                      MENÚ                          ║");
        System.out.println("╠════════════════════════════════════════════════════╣");
        System.out.println("║  1. Generar hash (MD5 / SHA-1 / SHA-256)           ║");
        System.out.println("║  2. Fuerza Bruta Secuencial        [1 hilo]        ║");
        System.out.println("║  3. Ataque por Diccionario         [1 hilo]        ║");
        System.out.println("║  4. Fuerza Bruta Paralela          [N hilos]       ║");
        System.out.println("║  5. Comparativa automática         [Seq vs Par]    ║");
        System.out.println("║  0. Salir                                          ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }

    /**
     * Pide al usuario que elija el tipo de hash y retorna el HashType correspondiente.
     * Si el usuario presiona ENTER sin ingresar nada, devuelve MD5 por defecto.
     */
    private static HashType leerTipoHash() {
        System.out.println("Algoritmo de hash:");
        System.out.println("  1. MD5     (32 chars hex)");
        System.out.println("  2. SHA-1   (40 chars hex)");
        System.out.println("  3. SHA-256 (64 chars hex)");
        System.out.print("Opción (ENTER = MD5): ");
        String eleccion = scanner.nextLine().trim();
        switch (eleccion) {
            case "2": return HashType.SHA_1;
            case "3": return HashType.SHA_256;
            default:  return HashType.MD5;  // "1" o cualquier otra entrada → MD5
        }
    }

    /**
     * Lee un entero obligatorio desde stdin. Repite hasta obtener uno válido.
     */
    private static int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Por favor ingrese un número entero válido.");
            }
        }
    }

    /**
     * Lee un entero con valor por defecto cuando el usuario presiona ENTER.
     */
    private static int leerEnteroConDefault(String etiqueta, int valorDefault) {
        System.out.print(etiqueta + " (default " + valorDefault + "): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return valorDefault;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Valor inválido; usando " + valorDefault + ".");
            return valorDefault;
        }
    }
}
