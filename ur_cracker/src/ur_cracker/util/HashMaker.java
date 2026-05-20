package ur_cracker.util;

import ur_cracker.core.HashType;
import ur_cracker.core.Hasher;

import java.util.Scanner;

/**
 * HashMaker – utilidad de línea de comandos para generar hashes rápidamente.
 *
 * Puede ejecutarse de forma independiente con:
 *   java -cp build ur_cracker.util.HashMaker
 *
 * o en modo no-interactivo pasando el texto como argumento:
 *   java -cp build ur_cracker.util.HashMaker "miContraseña"
 *
 * Muestra el hash MD5, SHA-1 y SHA-256 del texto dado.
 * Útil para generar hashes de prueba que luego se pueden usar
 * en las opciones 2 y 3 del menú principal.
 */
public class HashMaker {

    public static void main(String[] args) {
        String texto;

        if (args.length > 0) {
            // Modo no-interactivo: el texto viene como argumento de línea de comandos
            texto = String.join(" ", args);
            System.out.println("[HashMaker] Texto recibido como argumento: '" + texto + "'");
        } else {
            // Modo interactivo: pedir el texto al usuario
            Scanner scanner = new Scanner(System.in);
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║         UR-Cracker – HashMaker           ║");
            System.out.println("║  Genera hashes MD5, SHA-1 y SHA-256      ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nIngrese el texto a hashear: ");
            texto = scanner.nextLine().trim();
            scanner.close();
        }

        if (texto.isEmpty()) {
            System.err.println("Error: el texto no puede estar vacío.");
            System.exit(1);
        }

        // Calcular e imprimir el hash con cada algoritmo disponible
        imprimirHashes(texto);
    }

    /**
     * Genera e imprime los hashes de un texto con todos los algoritmos soportados.
     * Puede usarse también desde otras clases (ej: UR_Cracker.java).
     *
     * @param texto Texto del que se generarán los hashes
     */
    public static void imprimirHashes(String texto) {
        System.out.println();
        System.out.println("Texto     : \"" + texto + "\"");
        System.out.println("─".repeat(60));

        // Iterar por todos los HashType definidos en el enum
        for (HashType tipo : HashType.values()) {
            String hash = Hasher.hashear(texto, tipo);
            System.out.printf("%-10s: %s%n", tipo.name(), hash);
        }

        System.out.println("─".repeat(60));
        System.out.println("(Copie cualquiera de estos hashes para usar en el menú)");
        System.out.println();
    }
}
