# UR-Cracker v2.0

Herramienta educativa modular para recuperar contraseñas a partir de sus hashes,
desarrollada para la clase de **Sistemas Operativos** – Universidad del Rosario.

---

## Descripción del proyecto

UR-Cracker implementa tres estrategias de ataque:

| Estrategia              | Motor             | Semana |
|-------------------------|-------------------|--------|
| Fuerza bruta secuencial | `SequentialEngine`| 1      |
| Ataque por diccionario  | `SequentialEngine`| 1      |
| Fuerza bruta paralela   | `ParallelEngine`  | 2      |

El proyecto está diseñado para ilustrar conceptos de SO:
- **Semana 1**: proceso secuencial, medición de tiempo, complejidad combinatoria.
- **Semana 2**: creación de hilos, variables `volatile`, secciones críticas (`synchronized`), `join()`, y la Ley de Amdahl.

---

## Arquitectura de carpetas

```
ur_cracker/
├── data/
│   ├── dictionary.txt         → 20 contraseñas comunes
│   └── targets.txt            → Hashes de prueba con su contraseña esperada
│
├── scripts/
│   ├── build.sh               → Compila el proyecto → build/
│   └── test.sh                → Pruebas automáticas sin intervención
│
├── src/
│   └── ur_cracker/
│       ├── core/
│       │   ├── HashType.java  → Enum: MD5, SHA_1, SHA_256
│       │   ├── Hasher.java    → MessageDigest → String hexadecimal
│       │   └── CrackResult.java → Resultado inmutable del ataque
│       │
│       ├── engine/
│       │   ├── SequentialEngine.java → Motor 1 hilo (Semana 1)
│       │   └── ParallelEngine.java   → Motor N hilos  (Semana 2, esqueleto)
│       │
│       ├── generator/
│       │   ├── PasswordGenerator.java    → Interfaz Iterable<String>
│       │   ├── BruteForceGenerator.java  → Contador en base N (lazy)
│       │   └── DictionaryGenerator.java  → Lectura lazy de archivo
│       │
│       ├── workers/
│       │   └── CrackerWorker.java  → extends Thread (Semana 2, esqueleto)
│       │
│       ├── util/
│       │   └── HashMaker.java      → Utilidad CLI independiente
│       │
│       └── UR_Cracker.java         → Menú interactivo principal
│
├── build/                     → Generado por build.sh (no editar)
└── README.md
```

---

## Requisitos

- **Java 11** o superior (JDK completo, no solo JRE)
- Ubuntu 20.04 / 22.04 (probado en VirtualBox)

Verificar la versión instalada:

```bash
java -version
javac -version
```

Si Java no está instalado:

```bash
sudo apt update && sudo apt install default-jdk -y
```

---

## Paso 1 – Dar permisos de ejecución a los scripts

```bash
cd ~/UR-Cracker-2/ur_cracker
chmod +x scripts/build.sh scripts/test.sh
```

---

## Paso 2 – Compilar con build.sh

```bash
# Desde la raíz del proyecto ur_cracker/
./scripts/build.sh
```

Salida esperada:
```
========================================
    UR-Cracker Build System v2.0
========================================
...
OK. Compilado en build/
========================================
```

---

## Paso 3 – Ejecutar el programa

> **Siempre ejecutar desde la raíz** (`ur_cracker/`) para que la ruta
> `data/dictionary.txt` se resuelva correctamente.

```bash
java -cp build ur_cracker.UR_Cracker
```

---

## Paso 4 – Ejecutar pruebas automáticas

```bash
./scripts/test.sh
```

Salida esperada:
```
========================================
    UR-Cracker Test Suite v2.0
========================================
  ✓ PASADO : Diccionario encuentra 'password'
  ✓ PASADO : Secuencial encuentra 'abc'
  ✓ PASADO : HashMaker genera hash de 'test'
========================================
  Resultados: 3 pasado(s), 0 fallido(s)
```

---

## Flujo recomendado para la demo

### 1. Generar hashes (opción 1)

Escriba cualquier contraseña y obtendrá sus hashes en MD5, SHA-1 y SHA-256:

```
Texto     : "cat"
────────────────────────────────────────────────────────────
MD5       : d077f244def8a70e5ea758bd8352fcd8
SHA_1     : 9d989e8d27dc9e0ec3389fc855f142c3d40f0c50
SHA_256   : 77af778b51abd4a3c51c5ddd97204a9c3ae614ebccb75a606c3b6865aed6744e
```

Copie cualquiera de esos hashes para usar en las opciones 2 o 3.

### 2. Fuerza bruta secuencial (opción 2)

Use el hash de una contraseña **corta** (2-4 letras) para ver resultados rápidos:

```
Hash objetivo: 900150983cd24fb0d6963f7d28e17f72
Algoritmo    : MD5
Long. máxima : 3
Alfabeto     : abc   ← reducido para la demo
```

### 3. Diccionario (opción 3)

Use el hash MD5 de `password`:

```
Hash objetivo: 5f4dcc3b5aa765d61d8327deb882cf99
Algoritmo    : MD5
```

### 4. Utilidad HashMaker

Se puede usar de forma independiente sin abrir el menú:

```bash
# Interactivo
java -cp build ur_cracker.util.HashMaker

# Con argumento (ideal para scripts o para obtener el hash de targets.txt)
java -cp build ur_cracker.util.HashMaker "password"
java -cp build ur_cracker.util.HashMaker "abc"
```

---

## Parámetros recomendados según la contraseña

| Contraseña | Charset   | Long. máx | Tiempo aprox. |
|------------|-----------|-----------|---------------|
| `ab`       | `ab`      | 2         | < 0.01 s      |
| `abc`      | `abc`     | 3         | < 0.1 s       |
| `cat`      | letras    | 3         | < 0.5 s       |
| `dog`      | letras    | 3         | < 1 s         |
| `pass`     | letras    | 4         | 5–30 s        |
| `hello`    | letras    | 5         | varios min.   |

---

## Conceptos de SO que ilustra el proyecto

| Concepto                    | Dónde se ve                                        |
|-----------------------------|-----------------------------------------------------|
| Proceso secuencial (1 hilo) | `SequentialEngine.crack()`                         |
| Creación de hilos           | `CrackerWorker extends Thread` + `.start()`        |
| Variable `volatile`         | `CrackerWorker.found` – visible entre todos los hilos |
| Sección crítica             | `synchronized(CrackerWorker.class)` al escribir resultado |
| Sincronización              | `thread.join()` en `ParallelEngine`                |
| Patrón Strategy             | `PasswordGenerator` como interfaz intercambiable   |
| Value Object                | `CrackResult` inmutable                            |
| Iterador lazy               | `BruteForceGenerator` / `DictionaryGenerator`      |

---

## Comandos rápidos (resumen)

```bash
# Permisos (solo la primera vez)
chmod +x scripts/build.sh scripts/test.sh

# Compilar
./scripts/build.sh

# Ejecutar
java -cp build ur_cracker.UR_Cracker

# Tests automáticos
./scripts/test.sh

# HashMaker independiente
java -cp build ur_cracker.util.HashMaker "miClave"
```

---

> **Nota ética**: Esta herramienta es exclusivamente para uso educativo
> en el entorno controlado de la clase de Sistemas Operativos.
