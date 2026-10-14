# Scripts del Proyecto KDD Cup 1999

Este directorio contiene todos los scripts Scala necesarios para el procesamiento del dataset KDD Cup 1999.

## Estructura de Archivos

```
scripts/
├── procesamiento.scala      # Script principal (ejecutar este)
├── exploracion.scala         # Script de exploración de datos
├── limpieza.scala           # Módulo de limpieza de datos
├── transformacion.scala     # Módulo de transformación de datos
└── README.md               # Este archivo
```

## Requisitos

- **Apache Spark:** 4.1.3 o superior
- **Scala:** 2.12 (incluido con Spark)
- **Java:** JDK 8 o 11
- **Memoria recomendada:** Mínimo 8 GB RAM

## Instrucciones de Ejecución

### 1. Descargar los Datos

Antes de ejecutar los scripts, descargue el dataset KDD Cup 1999:

```bash
# Descargar el dataset completo
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data.gz

# Descomprimir
gunzip kddcup.data.gz

# Mover a la carpeta de datos
mv kddcup.data ../datos/raw/
```

**Alternativa (dataset 10%):** Si el dataset completo es muy grande, puede usar la versión del 10%:

```bash
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data_10_percent.gz
gunzip kddcup.data_10_percent.gz
mv kddcup.data_10_percent ../datos/raw/kddcup.data
```

### 2. Iniciar Spark Shell

```bash
spark-shell --driver-memory 6g --executor-memory 4g
```

**Parámetros recomendados:**
- `--driver-memory 6g`: Memoria para el driver (ajustar según disponibilidad)
- `--executor-memory 4g`: Memoria para executors
- Para máquinas con menos memoria, reducir estos valores

### 3. Ejecutar el Script Principal

**Opción A: Ejecutar solo el script principal (recomendado)**

El script principal carga automáticamente los módulos auxiliares:

```scala
// Modificar la variable PATH si es necesario
:load procesamiento.scala
```

**IMPORTANTE:** Antes de ejecutar, verificar la variable `PATH` dentro de `procesamiento.scala`:

```scala
val PATH = "/home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/"
```

Cambiar a la ruta donde se encuentran sus datos.

**Opción B: Ejecutar exploración independiente**

Si solo desea explorar los datos sin procesarlos:

```scala
:load exploracion.scala
```

Este script es independiente y no requiere los otros módulos.

### 4. Verificar Resultados

Los datos procesados se guardarán en:

```
../datos/processed/
├── train.parquet         # Conjunto de entrenamiento
├── test.parquet          # Conjunto de prueba
├── train_sample.csv/     # Muestra del entrenamiento (1000 registros)
└── test_sample.csv/      # Muestra de prueba (1000 registros)
```

## Descripción de los Scripts

### procesamiento.scala (Script Principal)

**Propósito:** Pipeline completo de procesamiento que integra todos los módulos.

**Pipeline:**
1. Carga de datos con schema definido
2. Limpieza inicial (registros vacíos/inválidos)
3. Split stratificado train/test (70/30)
4. Limpieza completa del conjunto de entrenamiento
5. Transformación del entrenamiento
6. Limpieza del conjunto de prueba (con cálculo de NTAIE y NTOE)
7. Transformación del conjunto de prueba (usando parámetros del entrenamiento)
8. Guardado de datos procesados en formato Parquet

**Parámetros configurables:**

```scala
val TRAIN_TEST_SPLIT = Array(0.7, 0.3)  // Proporción train/test
val RANDOM_SEED = 42                     // Semilla para reproducibilidad
val OUTLIER_ACTION = "cap"               // "cap", "remove", o "keep"
val SCALER_TYPE = "standard"             // "standard" o "minmax"
```

**Tiempo de ejecución estimado:** 10-30 minutos (depende del hardware y tamaño del dataset)

### exploracion.scala

**Propósito:** Análisis exploratorio inicial del dataset.

**Funcionalidades:**
- Carga y visualización de datos
- Estadísticas descriptivas
- Análisis de valores ausentes
- Distribución de clases
- Análisis de atributos categóricos
- Detección inicial de outliers
- Análisis de correlaciones

**Uso:**
```scala
:load exploracion.scala
// Los resultados se imprimen en la consola
// El DataFrame 'dataWithCategory' queda disponible para análisis adicional
```

**Tiempo de ejecución:** 5-15 minutos

### limpieza.scala

**Propósito:** Módulo con funciones de limpieza de datos.

**Funciones principales:**

```scala
// Eliminar registros vacíos
removeEmptyRecords(df: DataFrame): DataFrame

// Validar formato de registros
validateRecords(df: DataFrame): DataFrame

// Tratar valores ausentes
handleMissingValues(df: DataFrame): DataFrame

// Manejar outliers
handleOutliers(df: DataFrame, cols: Seq[String], action: String): DataFrame

// Pipeline completo de limpieza
cleanData(df: DataFrame, action: String): DataFrame

// Limpieza de conjunto de prueba con métricas
cleanTestData(df: DataFrame, action: String): (DataFrame, Long, Long)
```

**Estrategias de limpieza:**
- Valores ausentes: Imputación por mediana (numéricos) o moda (categóricos)
- Outliers: Detección por IQR, tratamiento por winsorización o eliminación
- Validación: Rangos válidos para variables binarias, proporciones, etc.

### transformacion.scala

**Propósito:** Módulo con funciones de transformación de datos.

**Funciones principales:**

```scala
// Mapear tipos de ataque a categorías
mapAttackCategories(df: DataFrame): DataFrame

// Seleccionar atributos relevantes
selectFeatures(df: DataFrame): DataFrame

// Codificar variables categóricas
encodeCategorialFeatures(df: DataFrame, cols: Seq[String]): (DataFrame, Array[StringIndexer])

// One-hot encoding
oneHotEncode(df: DataFrame, cols: Seq[String]): (DataFrame, Array[OneHotEncoder])

// Normalizar features (StandardScaler o MinMaxScaler)
standardScaleFeatures(df: DataFrame, ...): (DataFrame, StandardScalerModel)
minMaxScaleFeatures(df: DataFrame, ...): (DataFrame, MinMaxScalerModel)

// Crear labels numéricos
createLabels(df: DataFrame): (DataFrame, StringIndexerModel)

// Pipeline completo de transformación
transformData(df: DataFrame, scalerType: String): (DataFrame, Map[String, Any])

// Transformar conjunto de prueba
transformTestData(df: DataFrame, models: Map[String, Any]): DataFrame
```

**Transformaciones aplicadas:**
- Mapeo de 23 tipos de ataque a 5 categorías (normal, DoS, Probe, R2L, U2R)
- Eliminación de atributo `num_outbound_cmds` (sin varianza)
- Codificación de variables categóricas: StringIndexer + OneHotEncoder
- Normalización de features numéricas
- Ensamblado de vector de features para Spark ML

## Formato de Datos Procesados

### Estructura del DataFrame Final

```
DataFrame[features: vector, label: double, attack_category: string, attack_type: string]
```

**Columnas:**
- `features`: Vector denso con todas las features transformadas
  - Features numéricas normalizadas (40 atributos)
  - Features categóricas codificadas (protocol_type, service, flag)
  - Dimensión total: ~120-130 (depende del número de categorías en service)
  
- `label`: Etiqueta numérica (0-4)
  - 0: normal
  - 1: DoS
  - 2: Probe
  - 3: R2L
  - 4: U2R
  
- `attack_category`: Categoría textual (para referencia)
- `attack_type`: Tipo de ataque original (para trazabilidad)

### Formato Parquet

Los datos se guardan en formato Parquet por las siguientes ventajas:
- Compresión eficiente (reduce tamaño en ~80%)
- Almacenamiento columnar optimizado para Spark
- Lectura rápida de columnas específicas
- Preserva tipos de datos complejos (Vector)

**Cargar datos procesados:**

```scala
val trainData = spark.read.parquet("/ruta/datos/processed/train.parquet")
val testData = spark.read.parquet("/ruta/datos/processed/test.parquet")
```

## Métricas de Limpieza

El script calcula y reporta las siguientes métricas (según Apéndice I del enunciado):

- **NTAIE:** Número Total de Ausentes/Inconsistentes Eliminados
- **NTOE:** Número Total de Outliers Eliminados
- **Tasa de no clasificados:** (NTAIE + NTOE) / Total de registros de prueba

**Requisito:** La tasa de no clasificados debe ser al menos un orden de magnitud menor que la tasa de error esperada del clasificador.

## Solución de Problemas

### Error: OutOfMemoryError

**Solución:** Aumentar memoria del driver y executors:

```bash
spark-shell --driver-memory 8g --executor-memory 6g
```

O reducir el tamaño del dataset usando la versión del 10%.

### Error: File not found

**Solución:** Verificar que:
1. El archivo `kddcup.data` existe en `../datos/raw/`
2. La variable `PATH` en el script apunta a la ubicación correcta

### Error: Package not found

**Solución:** Verificar la versión de Spark:

```bash
spark-shell --version
```

Debe ser Spark 4.1.3 o compatible. Si usa una versión anterior, algunas funciones pueden no estar disponibles.

### Script muy lento

**Solución:**
1. Usar la versión del 10% del dataset para pruebas
2. Aumentar el paralelismo: `spark.conf.set("spark.sql.shuffle.partitions", "200")`
3. Verificar que Spark está usando múltiples cores

## Notas Importantes

1. **Orden de ejecución:** Los módulos `limpieza.scala` y `transformacion.scala` son cargados automáticamente por `procesamiento.scala`. No es necesario cargarlos manualmente.

2. **Reproducibilidad:** La semilla aleatoria (`RANDOM_SEED = 42`) garantiza que el split train/test sea siempre el mismo.

3. **Conjunto de prueba:** NUNCA se debe usar el conjunto de prueba para:
   - Calcular estadísticas (media, mediana, percentiles, etc.)
   - Entrenar modelos o transformadores
   - Ajustar hiperparámetros
   - Tomar decisiones sobre limpieza o selección de features

4. **Validación:** Siempre verificar la distribución de clases después del split para asegurar que la estratificación funcionó correctamente.

## Referencias

- [KDD Cup 1999 Data](http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html)
- [Spark ML Guide](https://spark.apache.org/docs/latest/ml-guide.html)
- [Scala API Docs](https://spark.apache.org/docs/latest/api/scala/index.html)

## Contacto

Para preguntas o problemas:
- Grupo [X]
- [Nombres de los integrantes]
- [Emails]

---

**Última actualización:** Octubre 2026
