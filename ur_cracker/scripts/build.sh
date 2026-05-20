#!/bin/bash
# build.sh – Compila todo el proyecto UR-Cracker en la carpeta build/
#
# Uso: ./scripts/build.sh   (ejecutar desde la raíz del proyecto: ur_cracker/)
# ─────────────────────────────────────────────────────────────────────────────

# Detener inmediatamente si cualquier comando falla
set -e

# ── Calcular rutas absolutas a partir de la ubicación de este script ──────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROYECTO_DIR="$(dirname "$SCRIPT_DIR")"
SRC_DIR="$PROYECTO_DIR/src"
BUILD_DIR="$PROYECTO_DIR/build"

echo "========================================"
echo "    UR-Cracker Build System v2.0"
echo "========================================"
echo ""
echo "Raíz del proyecto : $PROYECTO_DIR"
echo "Fuentes           : $SRC_DIR"
echo "Destino           : $BUILD_DIR"
echo ""

# ── Verificar que existe el directorio src/ ───────────────────────────────────
if [ ! -d "$SRC_DIR" ]; then
    echo "ERROR: No se encontró el directorio src/ en $PROYECTO_DIR"
    echo "       Asegúrese de ejecutar este script desde la raíz del proyecto."
    exit 1
fi

# ── Verificar que javac esté disponible ───────────────────────────────────────
if ! command -v javac &> /dev/null; then
    echo "ERROR: 'javac' no está instalado o no está en el PATH."
    echo "       Instálelo con: sudo apt update && sudo apt install default-jdk -y"
    exit 1
fi

echo "Versión de Java detectada:"
javac -version
echo ""

# ── Crear (o limpiar) el directorio de salida build/ ─────────────────────────
if [ -d "$BUILD_DIR" ]; then
    echo "Limpiando compilación anterior en build/..."
    rm -rf "$BUILD_DIR"
fi
mkdir -p "$BUILD_DIR"

# ── Recolectar todos los archivos .java bajo src/ ────────────────────────────
ARCHIVO_FUENTES="/tmp/ur_cracker_sources_$$.txt"
find "$SRC_DIR" -name "*.java" > "$ARCHIVO_FUENTES"

TOTAL=$(wc -l < "$ARCHIVO_FUENTES")
if [ "$TOTAL" -eq 0 ]; then
    echo "ERROR: No se encontraron archivos .java en $SRC_DIR"
    rm -f "$ARCHIVO_FUENTES"
    exit 1
fi

echo "Archivos .java encontrados: $TOTAL"
cat "$ARCHIVO_FUENTES" | sed 's|'"$PROYECTO_DIR/"'||g' | sort
echo ""
echo "Compilando..."

# ── Compilar con javac usando @archivo para manejar muchos fuentes ────────────
# -encoding UTF-8  → soporta caracteres especiales y tildes en el código
# -sourcepath      → permite a javac resolver importaciones automáticamente
# -d build/        → los .class se organizan en subcarpetas según el paquete
javac \
    -encoding UTF-8 \
    -sourcepath "$SRC_DIR" \
    -d "$BUILD_DIR" \
    @"$ARCHIVO_FUENTES"

# ── Limpiar archivo temporal ──────────────────────────────────────────────────
rm -f "$ARCHIVO_FUENTES"

# ── Mostrar árbol de clases generadas ────────────────────────────────────────
echo ""
echo "Clases generadas en build/:"
find "$BUILD_DIR" -name "*.class" | sed 's|'"$BUILD_DIR/"'||g' | sort

echo ""
echo "========================================"
echo "OK. Compilado en build/"
echo "========================================"
echo ""
echo "Para ejecutar el programa principal:"
echo "  java -cp build ur_cracker.UR_Cracker"
echo ""
echo "Para ejecutar la utilidad HashMaker:"
echo "  java -cp build ur_cracker.util.HashMaker"
echo "  java -cp build ur_cracker.util.HashMaker \"miContraseña\""
