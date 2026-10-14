/*
 * Script Principal de Procesamiento - KDD Cup 1999
 * 
 * Este es el script principal que los profesores cargarán con :load
 * Integra todos los módulos y ejecuta el pipeline completo de procesamiento
 * 
 * Pipeline:
 * 1. Carga de datos
 * 2. Limpieza inicial (registros vacíos)
 * 3. Split train/test (stratified)
 * 4. Limpieza completa de entrenamiento
 * 5. Transformación de entrenamiento
 * 6. Limpieza y transformación de prueba (usando parámetros de entrenamiento)
 * 7. Guardado de datasets procesados
 * 
 * Autor: Grupo [X]
 * Fecha: Octubre 2026
 * Asignatura: TESCC - Clasificadores
 */

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.ml.feature._

println("=" * 80)
println("PROYECTO KDD CUP 1999 - PROCESAMIENTO COMPLETO")
println("Primera Etapa: Exploración y Preparación de Datos")
println("=" * 80)

// ============================================================================
// CONFIGURACIÓN
// ============================================================================

// Variable PATH para localización del fichero
// Los profesores modificarán esta ruta para ejecutar en sus máquinas
val PATH = "/home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/"
val OUTPUT_PATH = "/home/usuario/Datos/proyecto-kdd-cup-1999/datos/processed/"

// Parámetros de configuración
val DATA_FILE = "kddcup.data"
val TRAIN_TEST_SPLIT = Array(0.7, 0.3) // 70% entrenamiento, 30% prueba
val RANDOM_SEED = 42 // Para reproducibilidad
val OUTLIER_ACTION = "cap" // "cap", "remove", o "keep"
val SCALER_TYPE = "standard" // "standard" o "minmax"

println("\nConfiguración:")
println(s"  - Ruta de datos: ${PATH}")
println(s"  - Archivo: ${DATA_FILE}")
println(s"  - Split: ${TRAIN_TEST_SPLIT(0)*100}% entrenamiento, ${TRAIN_TEST_SPLIT(1)*100}% prueba")
println(s"  - Semilla aleatoria: ${RANDOM_SEED}")
println(s"  - Tratamiento de outliers: ${OUTLIER_ACTION}")
println(s"  - Tipo de escalado: ${SCALER_TYPE}")

// ============================================================================
// DEFINICIÓN DEL SCHEMA
// ============================================================================

val schema = StructType(Array(
  StructField("duration", IntegerType, nullable = false),
  StructField("protocol_type", StringType, nullable = false),
  StructField("service", StringType, nullable = false),
  StructField("flag", StringType, nullable = false),
  StructField("src_bytes", IntegerType, nullable = false),
  StructField("dst_bytes", IntegerType, nullable = false),
  StructField("land", IntegerType, nullable = false),
  StructField("wrong_fragment", IntegerType, nullable = false),
  StructField("urgent", IntegerType, nullable = false),
  StructField("hot", IntegerType, nullable = false),
  StructField("num_failed_logins", IntegerType, nullable = false),
  StructField("logged_in", IntegerType, nullable = false),
  StructField("num_compromised", IntegerType, nullable = false),
  StructField("root_shell", IntegerType, nullable = false),
  StructField("su_attempted", IntegerType, nullable = false),
  StructField("num_root", IntegerType, nullable = false),
  StructField("num_file_creations", IntegerType, nullable = false),
  StructField("num_shells", IntegerType, nullable = false),
  StructField("num_access_files", IntegerType, nullable = false),
  StructField("num_outbound_cmds", IntegerType, nullable = false),
  StructField("is_host_login", IntegerType, nullable = false),
  StructField("is_guest_login", IntegerType, nullable = false),
  StructField("count", IntegerType, nullable = false),
  StructField("srv_count", IntegerType, nullable = false),
  StructField("serror_rate", DoubleType, nullable = false),
  StructField("srv_serror_rate", DoubleType, nullable = false),
  StructField("rerror_rate", DoubleType, nullable = false),
  StructField("srv_rerror_rate", DoubleType, nullable = false),
  StructField("same_srv_rate", DoubleType, nullable = false),
  StructField("diff_srv_rate", DoubleType, nullable = false),
  StructField("srv_diff_host_rate", DoubleType, nullable = false),
  StructField("dst_host_count", IntegerType, nullable = false),
  StructField("dst_host_srv_count", IntegerType, nullable = false),
  StructField("dst_host_same_srv_rate", DoubleType, nullable = false),
  StructField("dst_host_diff_srv_rate", DoubleType, nullable = false),
  StructField("dst_host_same_src_port_rate", DoubleType, nullable = false),
  StructField("dst_host_srv_diff_host_rate", DoubleType, nullable = false),
  StructField("dst_host_serror_rate", DoubleType, nullable = false),
  StructField("dst_host_srv_serror_rate", DoubleType, nullable = false),
  StructField("dst_host_rerror_rate", DoubleType, nullable = false),
  StructField("dst_host_srv_rerror_rate", DoubleType, nullable = false),
  StructField("attack_type", StringType, nullable = false)
))

// ============================================================================
// CARGAR MÓDULOS AUXILIARES
// ============================================================================

println("\n" + "=" * 80)
println("CARGANDO MÓDULOS AUXILIARES")
println("=" * 80)

println("Cargando limpieza.scala...")
:load limpieza.scala

println("\nCargando transformacion.scala...")
:load transformacion.scala

println("\n✓ Todos los módulos cargados correctamente")

// ============================================================================
// PASO 1: CARGA DE DATOS
// ============================================================================

println("\n" + "=" * 80)
println("PASO 1: CARGA DE DATOS")
println("=" * 80)

val rawData = spark.read
  .format("csv")
  .option("header", "false")
  .option("inferSchema", "false")
  .schema(schema)
  .load(PATH + DATA_FILE)

println(s"✓ Datos cargados: ${rawData.count()} registros, ${rawData.columns.length} columnas")

// Mostrar distribución inicial de clases
val attackCategoryMap = Map(
  "normal" -> "normal", "normal." -> "normal",
  "back" -> "DoS", "back." -> "DoS", "land" -> "DoS", "land." -> "DoS",
  "neptune" -> "DoS", "neptune." -> "DoS", "pod" -> "DoS", "pod." -> "DoS",
  "smurf" -> "DoS", "smurf." -> "DoS", "teardrop" -> "DoS", "teardrop." -> "DoS",
  "ipsweep" -> "Probe", "ipsweep." -> "Probe", "nmap" -> "Probe", "nmap." -> "Probe",
  "portsweep" -> "Probe", "portsweep." -> "Probe", "satan" -> "Probe", "satan." -> "Probe",
  "ftp_write" -> "R2L", "ftp_write." -> "R2L", "guess_passwd" -> "R2L", "guess_passwd." -> "R2L",
  "imap" -> "R2L", "imap." -> "R2L", "multihop" -> "R2L", "multihop." -> "R2L",
  "phf" -> "R2L", "phf." -> "R2L", "spy" -> "R2L", "spy." -> "R2L",
  "warezclient" -> "R2L", "warezclient." -> "R2L", "warezmaster" -> "R2L", "warezmaster." -> "R2L",
  "buffer_overflow" -> "U2R", "buffer_overflow." -> "U2R", "loadmodule" -> "U2R", "loadmodule." -> "U2R",
  "perl" -> "U2R", "perl." -> "U2R", "rootkit" -> "U2R", "rootkit." -> "U2R"
)

val attackCategoryUDF = udf((attackType: String) => {
  attackCategoryMap.getOrElse(attackType, "unknown")
})

val dataWithCategory = rawData.withColumn("attack_category", attackCategoryUDF(col("attack_type")))

println("\nDistribución inicial de clases:")
dataWithCategory.groupBy("attack_category").count().orderBy(desc("count")).show()

// ============================================================================
// PASO 2: LIMPIEZA INICIAL (ANTES DE SPLIT)
// ============================================================================

println("\n" + "=" * 80)
println("PASO 2: LIMPIEZA INICIAL")
println("=" * 80)
println("Siguiendo las recomendaciones: limpieza básica ANTES de split")

// Solo eliminar registros completamente vacíos o inválidos
val initialCleanedData = removeEmptyRecords(dataWithCategory)
val validatedData = validateRecords(initialCleanedData)

println(s"✓ Datos después de limpieza inicial: ${validatedData.count()} registros")

// ============================================================================
// PASO 3: SPLIT TRAIN/TEST (STRATIFIED)
// ============================================================================

println("\n" + "=" * 80)
println("PASO 3: CREACIÓN DE CONJUNTOS DE ENTRENAMIENTO Y PRUEBA")
println("=" * 80)

// Stratified split para mantener distribución de clases
val Array(trainDataRaw, testDataRaw) = validatedData.randomSplit(TRAIN_TEST_SPLIT, seed = RANDOM_SEED)

// Cachear para mejorar performance
trainDataRaw.cache()
testDataRaw.cache()

val trainCount = trainDataRaw.count()
val testCount = testDataRaw.count()

println(s"✓ Conjunto de entrenamiento: $trainCount registros (${TRAIN_TEST_SPLIT(0)*100}%)")
println(s"✓ Conjunto de prueba: $testCount registros (${TRAIN_TEST_SPLIT(1)*100}%)")

// Verificar distribución de clases en cada conjunto
println("\nDistribución de clases en ENTRENAMIENTO:")
trainDataRaw.groupBy("attack_category").count().orderBy(desc("count")).show()

println("\nDistribución de clases en PRUEBA:")
testDataRaw.groupBy("attack_category").count().orderBy(desc("count")).show()

// ============================================================================
// PASO 4: LIMPIEZA COMPLETA DEL CONJUNTO DE ENTRENAMIENTO
// ============================================================================

println("\n" + "=" * 80)
println("PASO 4: LIMPIEZA COMPLETA DEL CONJUNTO DE ENTRENAMIENTO")
println("=" * 80)

// Aplicar limpieza completa (valores ausentes, outliers, etc.)
val trainDataCleaned = cleanData(trainDataRaw, OUTLIER_ACTION)
trainDataCleaned.cache()

println(s"✓ Conjunto de entrenamiento limpio: ${trainDataCleaned.count()} registros")

// ============================================================================
// PASO 5: TRANSFORMACIÓN DEL CONJUNTO DE ENTRENAMIENTO
// ============================================================================

println("\n" + "=" * 80)
println("PASO 5: TRANSFORMACIÓN DEL CONJUNTO DE ENTRENAMIENTO")
println("=" * 80)

val (trainDataTransformed, trainModels) = transformData(trainDataCleaned, SCALER_TYPE)
trainDataTransformed.cache()

println(s"✓ Conjunto de entrenamiento transformado: ${trainDataTransformed.count()} registros")
println("\nColumnas finales:")
trainDataTransformed.columns.foreach(col => println(s"  - $col"))

// ============================================================================
// PASO 6: LIMPIEZA Y TRANSFORMACIÓN DEL CONJUNTO DE PRUEBA
// ============================================================================

println("\n" + "=" * 80)
println("PASO 6: LIMPIEZA Y TRANSFORMACIÓN DEL CONJUNTO DE PRUEBA")
println("=" * 80)
println("⚠ IMPORTANTE: Usando parámetros calculados del conjunto de entrenamiento")

// Limpieza del conjunto de prueba (registrando NTAIE y NTOE)
val (testDataCleaned, ntaie, ntoe) = cleanTestData(testDataRaw, OUTLIER_ACTION)
testDataCleaned.cache()

println(s"✓ Conjunto de prueba limpio: ${testDataCleaned.count()} registros")

// Transformación del conjunto de prueba usando modelos del entrenamiento
val testDataTransformed = transformTestData(testDataCleaned, trainModels)
testDataTransformed.cache()

println(s"✓ Conjunto de prueba transformado: ${testDataTransformed.count()} registros")

// ============================================================================
// PASO 7: VERIFICACIÓN FINAL
// ============================================================================

println("\n" + "=" * 80)
println("PASO 7: VERIFICACIÓN FINAL")
println("=" * 80)

println("\n--- Conjunto de Entrenamiento ---")
println(s"Total de registros: ${trainDataTransformed.count()}")
println("Distribución de labels:")
trainDataTransformed.groupBy("label", "attack_category").count().orderBy("label").show(10)

println("\n--- Conjunto de Prueba ---")
println(s"Total de registros: ${testDataTransformed.count()}")
println("Distribución de labels:")
testDataTransformed.groupBy("label", "attack_category").count().orderBy("label").show(10)

// Verificar dimensión del vector de features
val sampleFeatures = trainDataTransformed.select("features").first().getAs[org.apache.spark.ml.linalg.Vector](0)
println(s"\nDimensión del vector de features: ${sampleFeatures.size}")

// Verificar que no hay valores nulos en features o labels
val trainNullFeatures = trainDataTransformed.filter(col("features").isNull).count()
val trainNullLabels = trainDataTransformed.filter(col("label").isNull).count()
val testNullFeatures = testDataTransformed.filter(col("features").isNull).count()
val testNullLabels = testDataTransformed.filter(col("label").isNull).count()

println(s"\nVerificación de valores nulos:")
println(s"  Entrenamiento - features nulas: $trainNullFeatures, labels nulas: $trainNullLabels")
println(s"  Prueba - features nulas: $testNullFeatures, labels nulas: $testNullLabels")

if (trainNullFeatures == 0 && trainNullLabels == 0 && testNullFeatures == 0 && testNullLabels == 0) {
  println("  ✓ No se encontraron valores nulos en features ni labels")
} else {
  println("  ⚠ ADVERTENCIA: Se encontraron valores nulos!")
}

// ============================================================================
// PASO 8: GUARDADO DE DATOS PROCESADOS
// ============================================================================

println("\n" + "=" * 80)
println("PASO 8: GUARDADO DE DATOS PROCESADOS")
println("=" * 80)

// Crear directorio de salida si no existe
import java.nio.file.{Files, Paths}
val outputDir = Paths.get(OUTPUT_PATH)
if (!Files.exists(outputDir)) {
  Files.createDirectories(outputDir)
  println(s"✓ Directorio creado: ${OUTPUT_PATH}")
}

// Guardar conjuntos de datos en formato Parquet
println("\nGuardando conjunto de entrenamiento...")
trainDataTransformed
  .write
  .mode("overwrite")
  .parquet(OUTPUT_PATH + "train.parquet")
println(s"✓ Guardado en: ${OUTPUT_PATH}train.parquet")

println("\nGuardando conjunto de prueba...")
testDataTransformed
  .write
  .mode("overwrite")
  .parquet(OUTPUT_PATH + "test.parquet")
println(s"✓ Guardado en: ${OUTPUT_PATH}test.parquet")

// Guardar también en formato CSV para inspección manual (opcional, solo una muestra)
println("\nGuardando muestras en CSV para inspección...")
trainDataTransformed.limit(1000)
  .select("attack_category", "attack_type", "label")
  .write
  .mode("overwrite")
  .option("header", "true")
  .csv(OUTPUT_PATH + "train_sample.csv")

testDataTransformed.limit(1000)
  .select("attack_category", "attack_type", "label")
  .write
  .mode("overwrite")
  .option("header", "true")
  .csv(OUTPUT_PATH + "test_sample.csv")

println("✓ Muestras guardadas en CSV")

// ============================================================================
// PASO 9: RESUMEN FINAL
// ============================================================================

println("\n" + "=" * 80)
println("RESUMEN FINAL DEL PROCESAMIENTO")
println("=" * 80)

println(s"\n📊 ESTADÍSTICAS GENERALES:")
println(s"  - Dataset original: ${rawData.count()} registros")
println(s"  - Después de limpieza inicial: ${validatedData.count()} registros")
println(s"  - Conjunto de entrenamiento final: ${trainDataTransformed.count()} registros")
println(s"  - Conjunto de prueba final: ${testDataTransformed.count()} registros")
println(s"  - Dimensión de features: ${sampleFeatures.size}")

println(s"\n🎯 MÉTRICAS DEL CONJUNTO DE PRUEBA:")
println(s"  - NTAIE (Ausentes/Inconsistentes Eliminados): $ntaie")
println(s"  - NTOE (Outliers Eliminados): $ntoe")
println(s"  - Total eliminado: ${ntaie + ntoe}")
println(s"  - Tasa de no clasificados: ${((ntaie + ntoe).toDouble / testCount) * 100}%.4f%%")

println(s"\n📁 ARCHIVOS GENERADOS:")
println(s"  - ${OUTPUT_PATH}train.parquet")
println(s"  - ${OUTPUT_PATH}test.parquet")
println(s"  - ${OUTPUT_PATH}train_sample.csv")
println(s"  - ${OUTPUT_PATH}test_sample.csv")

println(s"\n⚙️  PARÁMETROS UTILIZADOS:")
println(s"  - Split: ${TRAIN_TEST_SPLIT(0)*100}% entrenamiento, ${TRAIN_TEST_SPLIT(1)*100}% prueba")
println(s"  - Tratamiento de outliers: $OUTLIER_ACTION")
println(s"  - Escalado: $SCALER_TYPE")
println(s"  - Semilla aleatoria: $RANDOM_SEED")

println("\n" + "=" * 80)
println("✅ PROCESAMIENTO COMPLETADO CON ÉXITO")
println("=" * 80)

println("\n📝 PRÓXIMOS PASOS:")
println("  1. Revisar los archivos generados en: " + OUTPUT_PATH)
println("  2. Verificar las estadísticas en el informe")
println("  3. Proceder con la construcción del modelo (Entregable Individual)")

println("\n💡 Para cargar los datos procesados en el futuro:")
println("  val trainData = spark.read.parquet(\"" + OUTPUT_PATH + "train.parquet\")")
println("  val testData = spark.read.parquet(\"" + OUTPUT_PATH + "test.parquet\")")

// Unpersist para liberar memoria
trainDataRaw.unpersist()
testDataRaw.unpersist()
trainDataCleaned.unpersist()
testDataCleaned.unpersist()

println("\n✓ Memoria liberada (unpersist de DataFrames intermedios)")
