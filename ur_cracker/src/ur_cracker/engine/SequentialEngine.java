package ur_cracker.engine;

import ur_cracker.core.CrackResult;
import ur_cracker.core.HashType;
import ur_cracker.core.Hasher;
import ur_cracker.generator.PasswordGenerator;

/**
 * SequentialEngine – motor de ataque en UN SOLO HILO (Semana 1).
 *
 * Recorre secuencialmente todos los candidatos producidos por el generador
 * dado, hashea cada uno y lo compara con el hash objetivo. Si encuentra
 * coincidencia, retorna un CrackResult exitoso; si agota el generador,
 * retorna un resultado fallido.
 *
 * Conceptos de SO ilustrados:
 *   - Proceso secuencial: todo el trabajo ocurre en el hilo principal.
 *   - No hay concurrencia, lo que simplifica el razonamiento pero limita
 *     el rendimiento a un solo núcleo del CPU.
 */
public class SequentialEngine {

    private final PasswordGenerator generador; // Fuente de candidatos (bruta o diccionario)

    /**
     * @param generador Generador de contraseñas a usar (BruteForceGenerator o DictionaryGenerator)
     */
    public SequentialEngine(PasswordGenerator generador) {
        this.generador = generador;
    }

    /**
     * Ejecuta el ataque secuencial. Bloquea hasta encontrar la contraseña
     * o agotar todos los candidatos del generador.
     *
     * @param hashObjetivo Hash en hexadecimal que se desea revertir
     * @param tipo         Algoritmo utilizado para generar el hash objetivo
     * @return CrackResult con el resultado (exitoso o fallido)
     */
    public CrackResult crack(String hashObjetivo, HashType tipo) {
        long inicio    = System.currentTimeMillis();
        long intentos  = 0;

        // Encabezado del ataque
        System.out.println("\n[Secuencial] Iniciando ataque...");
        System.out.println("[Secuencial] Generador  : " + generador.getDescripcion());
        System.out.println("[Secuencial] Algoritmo  : " + tipo.getNombreAlgoritmo());
        System.out.println("[Secuencial] Hash obj.  : " + hashObjetivo);
        System.out.println();

        // Recorrer todos los candidatos que produce el generador
        for (String candidato : generador) {

            // Calcular el hash del candidato y comparar
            String hashCandidato = Hasher.hashear(candidato, tipo);
            intentos++;

            // Reporte de progreso cada 500 000 intentos para no silenciar la consola
            if (intentos % 500_000 == 0) {
                long transcurrido = System.currentTimeMillis() - inicio;
                System.out.printf("[Secuencial] Intentos: %,d | Tiempo: %.1f s | Último: '%s'%n",
                        intentos, transcurrido / 1000.0, candidato);
            }

            // Verificar coincidencia
            if (hashCandidato.equals(hashObjetivo)) {
                long tiempoTotal = System.currentTimeMillis() - inicio;
                System.out.printf("[Secuencial] ¡ENCONTRADA! '%s' en %,d intentos (%.3f s)%n",
                        candidato, intentos, tiempoTotal / 1000.0);
                return CrackResult.exito(candidato, tiempoTotal, intentos);
            }
        }

        // El generador se agotó sin encontrar coincidencia
        long tiempoTotal = System.currentTimeMillis() - inicio;
        System.out.printf("[Secuencial] Contraseña no encontrada. Intentos: %,d | Tiempo: %.3f s%n",
                intentos, tiempoTotal / 1000.0);
        return CrackResult.fallo(tiempoTotal, intentos);
    }
}
