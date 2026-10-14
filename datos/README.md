# Datos del Proyecto KDD Cup 1999

Este directorio contiene todos los datos utilizados en el proyecto, tanto los datos originales (raw) como los datos procesados (processed).

## Estructura de Directorios

```
datos/
├── raw/              # Datos originales sin procesar
│   └── kddcup.data   # Dataset principal (descargar manualmente)
├── processed/        # Datos procesados listos para modelado
│   ├── train.parquet
│   ├── test.parquet
│   ├── train_sample.csv/
│   └── test_sample.csv/
└── README.md        # Este archivo
```

## Datos Originales (raw/)

### KDD Cup 1999 Dataset

**Fuente oficial:** [UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/datasets/KDD+Cup+1999+Data)

**Enlace de descarga:** http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html

### Instrucciones de Descarga

#### Opción 1: Dataset Completo (Recomendado)

```bash
# Descargar (aproximadamente 743 MB comprimido)
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data.gz

# Descomprimir
gunzip kddcup.data.gz

# Mover a la carpeta raw/
mv kddcup.data /home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/
```

**Características:**
- Nombre del archivo: `kddcup.data`
- Tamaño: ~743 MB (comprimido), ~2.1 GB (descomprimido)
- Registros: 4,898,431 conexiones
- Formato: CSV sin cabecera

#### Opción 2: Dataset 10% (Para Pruebas Rápidas)

```bash
# Descargar versión del 10%
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data_10_percent.gz

# Descomprimir
gunzip kddcup.data_10_percent.gz

# Renombrar y mover
mv kddcup.data_10_percent /home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/kddcup.data
```

**Características:**
- Nombre del archivo: `kddcup.data_10_percent` (renombrar a `kddcup.data`)
- Tamaño: ~75 MB (comprimido), ~210 MB (descomprimido)
- Registros: 494,021 conexiones
- Útil para desarrollo y pruebas rápidas

### Verificación de Integridad

Para verificar que el archivo se descargó correctamente:

```bash
# Verificar tamaño del archivo
ls -lh kddcup.data

# Contar número de líneas (debe ser 4,898,431 para el dataset completo)
wc -l kddcup.data

# Ver las primeras líneas
head -n 5 kddcup.data
```

### Formato de los Datos

**Estructura:** CSV sin cabecera, 42 columnas separadas por comas.

**Ejemplo de registro:**

```
0,tcp,http,SF,215,45076,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,0.00,0.00,0.00,0.00,1.00,0.00,0.00,0,0,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,normal.
```

**Columnas (41 features + 1 label):**

1. duration (int)
2. protocol_type (string)
3. service (string)
4. flag (string)
5. src_bytes (int)
6. dst_bytes (int)
7-42. [Ver atributos_kdd99.md para lista completa]
42. attack_type (string) - **Variable objetivo**

### Tipos de Ataque en los Datos

El dataset contiene 23 tipos específicos de ataques más "normal" (tráfico legítimo):

**Normal:**
- `normal` (o `normal.`)

**Denial of Service (DoS):**
- `back`, `land`, `neptune`, `pod`, `smurf`, `teardrop`

**Probing/Scanning:**
- `ipsweep`, `nmap`, `portsweep`, `satan`

**Remote to Local (R2L):**
- `ftp_write`, `guess_passwd`, `imap`, `multihop`, `phf`, `spy`, `warezclient`, `warezmaster`

**User to Root (U2R):**
- `buffer_overflow`, `loadmodule`, `perl`, `rootkit`

**Nota:** Algunos tipos de ataque terminan con un punto (`.`), ejemplo: `normal.`, `smurf.`

### Distribución Aproximada de Clases (Dataset Completo)

| Categoría | Porcentaje | Registros Aproximados |
|-----------|------------|----------------------|
| normal    | 19.69%     | ~964,000             |
| DoS       | 79.24%     | ~3,883,000           |
| Probe     | 0.83%      | ~41,000              |
| R2L       | 0.23%      | ~11,000              |
| U2R       | 0.01%      | ~500                 |

**Observación:** Existe un fuerte desbalanceo de clases, especialmente en U2R y R2L.

## Datos Procesados (processed/)

Estos datos son generados automáticamente por el script `procesamiento.scala`.

### Archivos Generados

#### train.parquet

**Descripción:** Conjunto de entrenamiento procesado y listo para modelado.

**Formato:** Apache Parquet (columnar, comprimido)

**Tamaño estimado:**
- Dataset completo: ~1.5 GB
- Dataset 10%: ~150 MB

**Estructura:**
```
root
 |-- features: vector (contains [feature1, feature2, ..., featureN])
 |-- label: double (0.0, 1.0, 2.0, 3.0, 4.0)
 |-- attack_category: string (normal, DoS, Probe, R2L, U2R)
 |-- attack_type: string (tipo específico original)
```

**Mapeo de labels:**
- 0.0 → normal
- 1.0 → DoS
- 2.0 → Probe
- 3.0 → R2L
- 4.0 → U2R

**Dimensión del vector features:** ~120-130 (depende de las categorías en 'service')

**Número de registros:** ~70% del dataset original limpio

#### test.parquet

**Descripción:** Conjunto de prueba procesado.

**Formato:** Apache Parquet (columnar, comprimido)

**Tamaño estimado:**
- Dataset completo: ~650 MB
- Dataset 10%: ~65 MB

**Estructura:** Idéntica a train.parquet

**Número de registros:** ~30% del dataset original limpio

**Importante:** Este conjunto NO debe usarse durante el entrenamiento del modelo.

#### train_sample.csv/

**Descripción:** Muestra de 1000 registros del conjunto de entrenamiento en formato CSV para inspección manual.

**Columnas:** attack_category, attack_type, label

**Propósito:** Verificación rápida de la distribución de clases y mapeo de labels.

#### test_sample.csv/

**Descripción:** Muestra de 1000 registros del conjunto de prueba en formato CSV.

**Propósito:** Similar a train_sample.csv

### Cómo Cargar los Datos Procesados

**En Spark (Scala):**

```scala
import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder()
  .appName("KDD Cup 1999")
  .getOrCreate()

// Cargar conjunto de entrenamiento
val trainData = spark.read.parquet("/home/usuario/Datos/proyecto-kdd-cup-1999/datos/processed/train.parquet")

// Cargar conjunto de prueba
val testData = spark.read.parquet("/home/usuario/Datos/proyecto-kdd-cup-1999/datos/processed/test.parquet")

// Ver estructura
trainData.printSchema()
trainData.show(5)
```

**En PySpark (Python):**

```python
from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("KDD Cup 1999") \
    .getOrCreate()

# Cargar datos
train_data = spark.read.parquet("/home/usuario/Datos/proyecto-kdd-cup-1999/datos/processed/train.parquet")
test_data = spark.read.parquet("/home/usuario/Datos/proyecto-kdd-cup-1999/datos/processed/test.parquet")

# Ver estructura
train_data.printSchema()
train_data.show(5)
```

## Estadísticas de Procesamiento

### Limpieza Aplicada

**Conjunto de Entrenamiento:**
- Registros vacíos eliminados: [completar después de ejecutar]
- Registros inválidos eliminados: [completar]
- Outliers tratados: [método usado]

**Conjunto de Prueba:**
- NTAIE (Ausentes/Inconsistentes Eliminados): [completar]
- NTOE (Outliers Eliminados): [completar]
- Tasa de no clasificados: [completar]%

### Transformaciones Aplicadas

1. **Mapeo de Clases:** 23 tipos de ataque → 5 categorías
2. **Eliminación de Features:** num_outbound_cmds (sin varianza)
3. **Codificación Categórica:**
   - protocol_type: StringIndexer + OneHotEncoder
   - service: StringIndexer + OneHotEncoder
   - flag: StringIndexer + OneHotEncoder
4. **Normalización:** StandardScaler (media=0, std=1) aplicado a features numéricas
5. **Ensamblado:** VectorAssembler para crear vector de features

### Split Train/Test

**Método:** Stratified Random Split

**Proporción:** 70% entrenamiento, 30% prueba

**Semilla aleatoria:** 42 (para reproducibilidad)

**Verificación de estratificación:**

| Categoría | % en Original | % en Train | % en Test |
|-----------|---------------|------------|-----------|
| normal    | 19.69%        | [verificar] | [verificar] |
| DoS       | 79.24%        | [verificar] | [verificar] |
| Probe     | 0.83%         | [verificar] | [verificar] |
| R2L       | 0.23%         | [verificar] | [verificar] |
| U2R       | 0.01%         | [verificar] | [verificar] |

## Espacio en Disco Requerido

**Estimaciones (basadas en dataset completo):**

| Componente | Tamaño |
|-----------|--------|
| kddcup.data.gz (descarga) | 743 MB |
| kddcup.data (descomprimido) | 2.1 GB |
| train.parquet | 1.5 GB |
| test.parquet | 650 MB |
| Samples CSV | ~5 MB |
| **Total** | **~5 GB** |

**Recomendación:** Tener al menos 10 GB de espacio libre en disco.

## Consideraciones de Privacidad y Licencia

### Licencia

El dataset KDD Cup 1999 está disponible públicamente a través del UCI Machine Learning Repository.

**Citas recomendadas:**

```
Stolfo, S. J., Fan, W., Lee, W., Prodromidis, A., & Chan, P. K. (2000). 
Cost-based modeling for fraud and intrusion detection: Results from the JAM project. 
Proceedings DARPA Information Survivability Conference and Exposition. DISCEX'00.

Dua, D. and Graff, C. (2019). UCI Machine Learning Repository 
[http://archive.ics.uci.edu/ml]. Irvine, CA: University of California, 
School of Information and Computer Science.
```

### Privacidad

Los datos fueron generados sintéticamente en un entorno de red simulado para el programa de evaluación de DARPA 1998. No contienen información personal identificable.

## Problemas Conocidos del Dataset

1. **Desbalanceo extremo de clases:** U2R y R2L son minoritarias
2. **Datos sintéticos:** Generados en laboratorio, pueden no reflejar tráfico real actual
3. **Edad del dataset:** Año 1999 - los patrones de ataque han evolucionado
4. **Ataques de test no vistos:** El conjunto de prueba oficial contiene ataques no presentes en entrenamiento

## Referencias

- [KDD Cup 1999 Official Site](http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html)
- [UCI ML Repository - KDD Cup 1999](https://archive.ics.uci.edu/ml/datasets/KDD+Cup+1999+Data)
- [Task Description](http://kdd.ics.uci.edu/databases/kddcup99/task.html)
- [Attribute Names](http://kdd.ics.uci.edu/databases/kddcup99/kddcup.names)

## Soporte

Para problemas con la descarga o el procesamiento de datos:
- Consultar el archivo `../scripts/README.md`
- Verificar los logs de ejecución de los scripts
- Contactar al grupo [X]

---

**Última actualización:** Octubre 2026
