package ur_cracker.generator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * BruteForceGenerator – genera todas las combinaciones posibles del alfabeto
 * desde longitud 1 hasta {@code longitudMaxima}, en orden lexicográfico.
 *
 * Algoritmo: contador en base N (N = tamaño del alfabeto).
 * Ejemplo con alfabeto "ab" y longitud máx 2:
 *   a → b → aa → ab → ba → bb
 *
 * La generación es LAZY (bajo demanda): no almacena todas las combinaciones
 * en memoria, lo que permite manejar alfabetos y longitudes grandes.
 */
public class BruteForceGenerator implements PasswordGenerator {

    private final String alfabeto;        // Caracteres permitidos
    private final int    longitudMaxima;  // Máxima longitud de contraseña a generar

    /**
     * @param alfabeto       Conjunto de caracteres a usar (ej: "abcdefghijklmnopqrstuvwxyz0123456789")
     * @param longitudMaxima Longitud máxima de las contraseñas generadas (inclusive)
     */
    public BruteForceGenerator(String alfabeto, int longitudMaxima) {
        if (alfabeto == null || alfabeto.isEmpty()) {
            throw new IllegalArgumentException("El alfabeto no puede estar vacío.");
        }
        if (longitudMaxima < 1) {
            throw new IllegalArgumentException("La longitud máxima debe ser al menos 1.");
        }
        this.alfabeto       = alfabeto;
        this.longitudMaxima = longitudMaxima;
    }

    @Override
    public String getDescripcion() {
        return String.format("Fuerza Bruta (alfabeto=%d chars, maxLen=%d, total≈%s combinaciones)",
                alfabeto.length(), longitudMaxima,
                estimarTotal());
    }

    /**
     * Devuelve un iterador independiente. Se puede llamar múltiples veces
     * para reiniciar la generación desde el principio.
     */
    @Override
    public Iterator<String> iterator() {
        return new IteradorFuerzaBruta();
    }

    // ── Estimación aproximada de combinaciones totales ───────────────────────────

    private String estimarTotal() {
        // Suma de alfabeto^1 + alfabeto^2 + ... + alfabeto^longitudMaxima
        long total = 0;
        long potencia = 1;
        int base = alfabeto.length();
        for (int i = 1; i <= longitudMaxima; i++) {
            potencia *= base;
            total += potencia;
            if (total < 0) return ">2^63"; // Desbordamiento
        }
        return String.format("%,d", total);
    }

    // ── Iterador con estado (contador en base N) ─────────────────────────────────

    private class IteradorFuerzaBruta implements Iterator<String> {

        /**
         * Arreglo de índices que representa la combinación actual.
         * Cada índice apunta a un carácter en el alfabeto.
         * Funciona como un número en base alfabeto.length().
         *
         * Ejemplo con alfabeto "abc": [0] → "a", [1] → "b", [2] → "c",
         * luego aumenta longitud: [0,0] → "aa", [0,1] → "ab", etc.
         */
        private int[] indices;
        private int   longitudActual;
        private boolean terminado;

        IteradorFuerzaBruta() {
            this.longitudActual = 1;
            this.indices        = new int[longitudActual]; // Todos ceros → primer char del alfabeto
            this.terminado      = false;
        }

        @Override
        public boolean hasNext() {
            return !terminado;
        }

        @Override
        public String next() {
            if (terminado) throw new NoSuchElementException("No hay más combinaciones.");

            // 1. Construir la cadena correspondiente al estado actual de indices[]
            char[] chars = new char[longitudActual];
            for (int i = 0; i < longitudActual; i++) {
                chars[i] = alfabeto.charAt(indices[i]);
            }
            String combinacionActual = new String(chars);

            // 2. Avanzar el contador para la próxima llamada
            avanzarContador();

            return combinacionActual;
        }

        /**
         * Incrementa el contador de derecha a izquierda (como sumar 1 a un número).
         * Si todos los dígitos se desbordan, se aumenta la longitud.
         */
        private void avanzarContador() {
            int pos = longitudActual - 1; // Empezar desde el dígito menos significativo

            while (pos >= 0) {
                indices[pos]++;

                if (indices[pos] < alfabeto.length()) {
                    // Incremento exitoso: no hay acarreo
                    return;
                }

                // Acarreo: este dígito se desbordó, resetearlo y avanzar al siguiente
                indices[pos] = 0;
                pos--;
            }

            // Todos los dígitos se desbordaron → necesitamos una longitud mayor
            longitudActual++;

            if (longitudActual > longitudMaxima) {
                terminado = true; // Hemos agotado todas las combinaciones posibles
            } else {
                indices = new int[longitudActual]; // Nuevo arreglo, todos en cero
            }
        }

        /** No soportado: los generadores son de solo lectura. */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("BruteForceGenerator es de solo lectura.");
        }
    }
}
