package ur_cracker.generator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DictionaryGenerator – genera contraseñas leyendo un archivo de texto línea a línea.
 *
 * Lee el archivo de forma LAZY (bajo demanda): no carga todo el diccionario en
 * memoria de una vez, lo que permite manejar archivos muy grandes sin problema.
 *
 * Reglas de formato del archivo:
 *   - Una contraseña por línea.
 *   - Las líneas vacías se omiten.
 *   - Las líneas que empiezan con '#' se tratan como comentarios y se omiten.
 */
public class DictionaryGenerator implements PasswordGenerator {

    private final String rutaArchivo; // Ruta relativa o absoluta al diccionario

    /**
     * @param rutaArchivo Ruta al archivo de diccionario (ej: "data/dictionary.txt")
     */
    public DictionaryGenerator(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del diccionario no puede estar vacía.");
        }
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public String getDescripcion() {
        return "Diccionario (archivo: '" + rutaArchivo + "')";
    }

    /**
     * Devuelve un iterador que abre y recorre el archivo.
     * Cada llamada a iterator() abre el archivo desde el principio,
     * permitiendo reutilizar el generador.
     */
    @Override
    public Iterator<String> iterator() {
        return new IteradorDiccionario();
    }

    // ── Iterador con estado (lectura lazy del archivo) ───────────────────────────

    private class IteradorDiccionario implements Iterator<String> {

        private BufferedReader lector;   // Lector del archivo
        private String         siguiente; // Próxima contraseña a devolver (pre-leída)

        IteradorDiccionario() {
            try {
                // Abrir el archivo; se usará FileReader estándar
                lector = new BufferedReader(new FileReader(rutaArchivo));
                // Pre-leer la primera contraseña válida
                leerSiguiente();
            } catch (FileNotFoundException e) {
                System.err.println("[Diccionario] Archivo no encontrado: '" + rutaArchivo + "'");
                System.err.println("[Diccionario] Asegúrese de ejecutar desde la raíz del proyecto.");
                siguiente = null;
            }
        }

        /**
         * Avanza el lector hasta la próxima línea válida (no vacía, no comentario).
         * Almacena el resultado en {@code siguiente}.
         * Cierra el archivo cuando se llega al final.
         */
        private void leerSiguiente() {
            siguiente = null;
            try {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    linea = linea.trim();

                    // Omitir líneas vacías y comentarios
                    if (!linea.isEmpty() && !linea.startsWith("#")) {
                        siguiente = linea;
                        return; // Encontramos la próxima contraseña válida
                    }
                }

                // Se llegó al final del archivo: cerrarlo limpiamente
                lector.close();

            } catch (IOException e) {
                System.err.println("[Diccionario] Error al leer el archivo: " + e.getMessage());
                siguiente = null;
            }
        }

        @Override
        public boolean hasNext() {
            return siguiente != null;
        }

        @Override
        public String next() {
            if (siguiente == null) {
                throw new NoSuchElementException("El diccionario no tiene más entradas.");
            }

            // Guardar la contraseña actual y avanzar al siguiente
            String actual = siguiente;
            leerSiguiente();
            return actual;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("DictionaryGenerator es de solo lectura.");
        }
    }
}
