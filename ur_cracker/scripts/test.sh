#!/bin/bash
# test.sh – Pruebas automáticas de UR-Cracker (sin intervención del usuario)
#
# Verifica que:
#   - El ataque por diccionario encuentra "password" (MD5)
#   - La fuerza bruta secuencial encuentra "abc" (MD5, charset "abc", maxLen 3)
#
# Uso: ./scripts/test.sh   (ejecutar desde la raíz del proyecto: ur_cracker/)
# ─────────────────────────────────────────────────────────────────────────────

set -e

# ── Rutas ─────────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROYECTO_DIR="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROYECTO_DIR/build"

# ── Hashes de prueba (MD5) ────────────────────────────────────────────────────
HASH_ABC="900150983cd24fb0d6963f7d28e17f72"       # MD5("abc")
HASH_PASSWORD="5f4dcc3b5aa765d61d8327deb882cf99"  # MD5("password")

# ── Contadores ────────────────────────────────────────────────────────────────
PASADOS=0
FALLIDOS=0

# ── Función de ayuda para reportar resultados ────────────────────────────────
reportar() {
    local nombre="$1"
    local salida="$2"
    local patron="$3"

    if echo "$salida" | grep -q "$patron"; then
        echo "  ✓ PASADO : $nombre"
        PASADOS=$((PASADOS + 1))
    else
        echo "  ✗ FALLIDO: $nombre"
        echo "    Salida del programa:"
        echo "$salida" | sed 's/^/      /'
        FALLIDOS=$((FALLIDOS + 1))
    fi
}

# ═══════════════════════════════════════════════════════════════════════════════
echo "========================================"
echo "    UR-Cracker Test Suite v2.0"
echo "========================================"
echo ""

# ── Verificar que build/ existe ───────────────────────────────────────────────
if [ ! -d "$BUILD_DIR" ]; then
    echo "ERROR: No existe $BUILD_DIR"
    echo "       Ejecute primero: ./scripts/build.sh"
    exit 1
fi

# ── Verificar que java esté disponible ───────────────────────────────────────
if ! command -v java &> /dev/null; then
    echo "ERROR: 'java' no está en el PATH."
    exit 1
fi

echo "JRE detectado: $(java -version 2>&1 | head -1)"
echo "Ejecutando desde: $PROYECTO_DIR"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════
#  TEST 1 – Diccionario: buscar "password" con MD5
# ═══════════════════════════════════════════════════════════════════════════════
echo "─── Test 1: Diccionario (password → MD5) ───"
echo "    Hash objetivo : $HASH_PASSWORD"

# Simular entrada del usuario para opción 3:
#   Línea 1: "3"      → selecciona opción 3 (Diccionario)
#   Línea 2: hash     → ingresa el hash objetivo
#   Línea 3: ""       → ENTER = elige MD5
#   Línea 4: ""       → ENTER = "Presione ENTER para continuar"
#   Línea 5: "0"      → selecciona opción 0 (Salir)
SALIDA_T1=$(printf "3\n%s\n\n\n0\n" "$HASH_PASSWORD" \
    | java -cp "$BUILD_DIR" ur_cracker.UR_Cracker 2>&1)

reportar "Diccionario encuentra 'password'" "$SALIDA_T1" "password"

# ═══════════════════════════════════════════════════════════════════════════════
#  TEST 2 – Fuerza bruta secuencial: buscar "abc" con MD5
# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "─── Test 2: Fuerza Bruta Secuencial (abc → MD5) ───"
echo "    Hash objetivo : $HASH_ABC"
echo "    Alfabeto      : abc   (mínimo para ser rápido)"
echo "    Long. máxima  : 3"

# Simular entrada del usuario para opción 2:
#   Línea 1: "2"      → selecciona opción 2 (Fuerza Bruta Secuencial)
#   Línea 2: hash     → ingresa el hash objetivo
#   Línea 3: ""       → ENTER = elige MD5
#   Línea 4: "3"      → longitud máxima = 3
#   Línea 5: "abc"    → alfabeto reducido para la prueba
#   Línea 6: ""       → ENTER = "Presione ENTER para continuar"
#   Línea 7: "0"      → selecciona opción 0 (Salir)
SALIDA_T2=$(printf "2\n%s\n\n3\nabc\n\n0\n" "$HASH_ABC" \
    | java -cp "$BUILD_DIR" ur_cracker.UR_Cracker 2>&1)

reportar "Secuencial encuentra 'abc'" "$SALIDA_T2" "abc"

# ═══════════════════════════════════════════════════════════════════════════════
#  TEST 3 – HashMaker: generar hash de "test" sin interacción
# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "─── Test 3: HashMaker (argumento de línea de comandos) ───"

SALIDA_T3=$(java -cp "$BUILD_DIR" ur_cracker.util.HashMaker "test" 2>&1)
reportar "HashMaker genera hash de 'test'" "$SALIDA_T3" "098f6bcd4621d373cade4e832627b4f6"

# ═══════════════════════════════════════════════════════════════════════════════
#  TEST 4 – Motor paralelo: buscar "abc" con MD5 y 2 hilos
# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "─── Test 4: Motor Paralelo (abc → MD5, 2 hilos) ───"
echo "    Hash objetivo : $HASH_ABC"
echo "    Hilos         : 2"
echo "    Alfabeto      : abc"
echo "    Long. máxima  : 3"

# Simular entrada para opción 4:
#   Línea 1: "4"      → opción 4 (Paralelo)
#   Línea 2: hash     → hash objetivo
#   Línea 3: ""       → ENTER = MD5
#   Línea 4: "2"      → número de hilos
#   Línea 5: "3"      → longitud máxima
#   Línea 6: "abc"    → alfabeto reducido
#   Línea 7: ""       → ENTER para continuar
#   Línea 8: "0"      → Salir
SALIDA_T4=$(printf "4\n%s\n\n2\n3\nabc\n\n0\n" "$HASH_ABC" \
    | java -cp "$BUILD_DIR" ur_cracker.UR_Cracker 2>&1)

reportar "Paralelo encuentra 'abc'" "$SALIDA_T4" "abc"

# ═══════════════════════════════════════════════════════════════════════════════
#  TEST 5 – Comparativa automática muestra tabla de speedup
# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "─── Test 5: Comparativa automática (abc → MD5) ───"

# Simular entrada para opción 5:
#   Línea 1: "5"      → opción 5 (Comparativa)
#   Línea 2: hash     → hash objetivo
#   Línea 3: ""       → ENTER = MD5
#   Línea 4: "3"      → longitud máxima
#   Línea 5: "abc"    → alfabeto reducido
#   Línea 6: "2"      → número de hilos
#   Línea 7: ""       → ENTER para continuar
#   Línea 8: "0"      → Salir
SALIDA_T5=$(printf "5\n%s\n\n3\nabc\n2\n\n0\n" "$HASH_ABC" \
    | java -cp "$BUILD_DIR" ur_cracker.UR_Cracker 2>&1)

reportar "Comparativa muestra tabla de speedup" "$SALIDA_T5" "Speedup"

# ═══════════════════════════════════════════════════════════════════════════════
#  RESUMEN FINAL
# ═══════════════════════════════════════════════════════════════════════════════
echo ""
echo "========================================"
echo "  Resultados: $PASADOS pasado(s), $FALLIDOS fallido(s)"
echo "========================================"

if [ "$FALLIDOS" -gt 0 ]; then
    exit 1
else
    echo "  Todos los tests pasaron correctamente."
    exit 0
fi
