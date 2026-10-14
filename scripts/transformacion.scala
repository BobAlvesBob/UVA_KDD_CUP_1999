/*
 * Script de Transformación del Dataset KDD Cup 1999
 * 
 * Propósito: Funciones para transformar los datos
 * - Codificación de variables categóricas
 * - Normalización/estandarización de atributos numéricos
 * - Feature engineering
 * - Creación de DataFrames para Spark ML (features + labels)
 * 
 * Autor: Grupo [X]
 * Fecha: Octubre 2026
 * Asignatura: TESCC - Clasificadores
 */

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vector

println("=" * 80)
println("MÓDULO DE TRANSFORMACIÓN DE DATOS - KDD CUP 1999")
println("=" * 80)

// ============================================================================
// MAPEO DE TIPOS DE ATAQUE A CATEGORÍAS
// ============================================================================

// Mapeo de tipos de ataque específicos a categorías generales (5 clases)
val attackCategoryMap = Map(
  "normal" -> "normal",
  "normal." -> "normal",
  // DoS attacks
  "back" -> "DoS", "back." -> "DoS",
  "land" -> "DoS", "land." -> "DoS",
  "neptune" -> "DoS", "neptune." -> "DoS",
  "pod" -> "DoS", "pod." -> "DoS",
  "smurf" -> "DoS", "smurf." -> "DoS",
  "teardrop" -> "DoS", "teardrop." -> "DoS",
  // Probe attacks
  "ipsweep" -> "Probe", "ipsweep." -> "Probe",
  "nmap" -> "Probe", "nmap." -> "Probe",
  "portsweep" -> "Probe", "portsweep." -> "Probe",
  "satan" -> "Probe", "satan." -> "Probe",
  // R2L attacks
  "ftp_write" -> "R2L", "ftp_write." -> "R2L",
  "guess_passwd" -> "R2L", "guess_passwd." -> "R2L",
  "imap" -> "R2L", "imap." -> "R2L",
  "multihop" -> "R2L", "multihop." -> "R2L",
  "phf" -> "R2L", "phf." -> "R2L",
  "spy" -> "R2L", "spy." -> "R2L",
  "warezclient" -> "R2L", "warezclient." -> "R2L",
  "warezmaster" -> "R2L", "warezmaster." -> "R2L",
  // U2R attacks
  "buffer_overflow" -> "U2R", "buffer_overflow." -> "U2R",
  "loadmodule" -> "U2R", "loadmodule." -> "U2R",
  "perl" -> "U2R", "perl." -> "U2R",
  "rootkit" -> "U2R", "rootkit." -> "U2R"
)

/**
 * Mapea tipos de ataque específicos a categorías generales
 * 
 * @param df DataFrame con columna attack_type
 * @return DataFrame con columna attack_category añadida
 */
def mapAttackCategories(df: DataFrame): DataFrame = {
  println("\n--- Mapeando tipos de ataque a categorías ---")
  
  val attackCategoryUDF = udf((attackType: String) => {
    attackCategoryMap.getOrElse(attackType, "unknown")
  })
  
  val dfWithCategory = df.withColumn("attack_category", attackCategoryUDF(col("attack_type")))
  
  // Verificar distribución
  println("\nDistribución de categorías:")
  dfWithCategory.groupBy("attack_category").count().orderBy(desc("count")).show()
  
  dfWithCategory
}

// ============================================================================
// SELECCIÓN DE ATRIBUTOS
// ============================================================================

/**
 * Elimina atributos no útiles o redundantes
 * 
 * @param df DataFrame original
 * @return DataFrame con atributos seleccionados
 */
def selectFeatures(df: DataFrame): DataFrame = {
  println("\n--- Seleccionando atributos ---")
  
  // Eliminar num_outbound_cmds (siempre 0 en KDD99)
  val attributesToRemove = Seq("num_outbound_cmds")
  
  println(s"Atributos a eliminar: ${attributesToRemove.mkString(", ")}")
  
  val selectedDF = df.drop(attributesToRemove: _*)
  
  println(s"Atributos restantes: ${selectedDF.columns.length}")
  
  selectedDF
}

// ============================================================================
// CODIFICACIÓN DE VARIABLES CATEGÓRICAS
// ============================================================================

/**
 * Codifica variables categóricas usando StringIndexer
 * 
 * @param df DataFrame con variables categóricas
 * @param columns Columnas a codificar
 * @return Tupla (DataFrame transformado, Array de StringIndexers entrenados)
 */
def encodeCategorialFeatures(df: DataFrame, columns: Seq[String]): (DataFrame, Array[StringIndexer]) = {
  println("\n--- Codificando variables categóricas ---")
  
  var transformedDF = df
  val indexers = scala.collection.mutable.ArrayBuffer[StringIndexer]()
  
  columns.foreach { colName =>
    println(s"  Codificando: $colName")
    
    val indexer = new StringIndexer()
      .setInputCol(colName)
      .setOutputCol(s"${colName}_indexed")
      .setHandleInvalid("keep") // Mantener valores desconocidos
    
    val indexerModel = indexer.fit(transformedDF)
    transformedDF = indexerModel.transform(transformedDF)
    
    indexers += indexer
    
    val numCategories = transformedDF.select(s"${colName}_indexed").distinct().count()
    println(s"    ✓ $numCategories categorías indexadas")
  }
  
  (transformedDF, indexers.toArray)
}

/**
 * Aplica One-Hot Encoding a variables categóricas indexadas
 * 
 * @param df DataFrame con columnas indexadas
 * @param columns Columnas originales (se buscarán las versiones _indexed)
 * @return Tupla (DataFrame transformado, Array de OneHotEncoders)
 */
def oneHotEncode(df: DataFrame, columns: Seq[String]): (DataFrame, Array[OneHotEncoder]) = {
  println("\n--- Aplicando One-Hot Encoding ---")
  
  var transformedDF = df
  val encoders = scala.collection.mutable.ArrayBuffer[OneHotEncoder]()
  
  columns.foreach { colName =>
    val indexedColName = s"${colName}_indexed"
    val encodedColName = s"${colName}_encoded"
    
    println(s"  Encoding: $indexedColName -> $encodedColName")
    
    val encoder = new OneHotEncoder()
      .setInputCol(indexedColName)
      .setOutputCol(encodedColName)
      .setDropLast(false) // No eliminar última categoría (para mantener info completa)
    
    val encoderModel = encoder.fit(transformedDF)
    transformedDF = encoderModel.transform(transformedDF)
    
    encoders += encoder
    
    println(s"    ✓ Encoding aplicado")
  }
  
  (transformedDF, encoders.toArray)
}

// ============================================================================
// NORMALIZACIÓN Y ESTANDARIZACIÓN
// ============================================================================

/**
 * Ensambla features numéricas en un vector
 * 
 * @param df DataFrame con features
 * @param featureColumns Columnas a ensamblar
 * @param outputCol Nombre de la columna de salida
 * @return Tupla (DataFrame con vector, VectorAssembler)
 */
def assembleNumericFeatures(df: DataFrame, featureColumns: Seq[String], 
                            outputCol: String = "numeric_features"): (DataFrame, VectorAssembler) = {
  println(s"\n--- Ensamblando ${featureColumns.length} features numéricas ---")
  
  val assembler = new VectorAssembler()
    .setInputCols(featureColumns.toArray)
    .setOutputCol(outputCol)
    .setHandleInvalid("keep")
  
  val assembledDF = assembler.transform(df)
  
  println(s"  ✓ Vector '$outputCol' creado")
  
  (assembledDF, assembler)
}

/**
 * Normaliza features usando StandardScaler (media 0, desviación 1)
 * 
 * @param df DataFrame con vector de features
 * @param inputCol Columna con el vector a escalar
 * @param outputCol Nombre de la columna de salida
 * @return Tupla (DataFrame escalado, StandardScalerModel entrenado)
 */
def standardScaleFeatures(df: DataFrame, inputCol: String = "numeric_features", 
                          outputCol: String = "scaled_numeric_features"): (DataFrame, StandardScalerModel) = {
  println("\n--- Aplicando StandardScaler ---")
  
  val scaler = new StandardScaler()
    .setInputCol(inputCol)
    .setOutputCol(outputCol)
    .setWithMean(true)
    .setWithStd(true)
  
  val scalerModel = scaler.fit(df)
  val scaledDF = scalerModel.transform(df)
  
  println("  ✓ Escalado completado (media=0, std=1)")
  
  (scaledDF, scalerModel)
}

/**
 * Normaliza features usando MinMaxScaler (rango [0, 1])
 * 
 * @param df DataFrame con vector de features
 * @param inputCol Columna con el vector a escalar
 * @param outputCol Nombre de la columna de salida
 * @return Tupla (DataFrame escalado, MinMaxScalerModel entrenado)
 */
def minMaxScaleFeatures(df: DataFrame, inputCol: String = "numeric_features", 
                        outputCol: String = "scaled_numeric_features"): (DataFrame, MinMaxScalerModel) = {
  println("\n--- Aplicando MinMaxScaler ---")
  
  val scaler = new MinMaxScaler()
    .setInputCol(inputCol)
    .setOutputCol(outputCol)
    .setMin(0.0)
    .setMax(1.0)
  
  val scalerModel = scaler.fit(df)
  val scaledDF = scalerModel.transform(df)
  
  println("  ✓ Escalado completado (rango [0, 1])")
  
  (scaledDF, scalerModel)
}

// ============================================================================
// CREACIÓN DE LABELS
// ============================================================================

/**
 * Convierte attack_category a índices numéricos para labels
 * 
 * @param df DataFrame con columna attack_category
 * @return Tupla (DataFrame con label, StringIndexer entrenado)
 */
def createLabels(df: DataFrame): (DataFrame, StringIndexerModel) = {
  println("\n--- Creando labels para clasificación ---")
  
  val labelIndexer = new StringIndexer()
    .setInputCol("attack_category")
    .setOutputCol("label")
    .setHandleInvalid("keep")
  
  val indexerModel = labelIndexer.fit(df)
  val labeledDF = indexerModel.transform(df)
  
  // Mostrar mapeo de labels
  println("\nMapeo de categorías a labels:")
  val labels = indexerModel.labelsArray(0)
  labels.zipWithIndex.foreach { case (category, idx) =>
    val count = labeledDF.filter(col("label") === idx).count()
    println(f"  $idx -> $category%-15s ($count%10d registros)")
  }
  
  (labeledDF, indexerModel)
}

// ============================================================================
// ENSAMBLADO FINAL DE FEATURES
// ============================================================================

/**
 * Ensambla todas las features (numéricas escaladas + categóricas codificadas) en un único vector
 * 
 * @param df DataFrame con todas las features transformadas
 * @param numericFeaturesCol Columna con features numéricas (escaladas)
 * @param categoricalFeatureCols Columnas con features categóricas (encoded)
 * @param outputCol Nombre de la columna de salida final
 * @return Tupla (DataFrame final, VectorAssembler)
 */
def assembleFinalFeatures(df: DataFrame, numericFeaturesCol: String, 
                          categoricalFeatureCols: Seq[String], 
                          outputCol: String = "features"): (DataFrame, VectorAssembler) = {
  println("\n--- Ensamblando vector final de features ---")
  
  val allFeatureCols = Seq(numericFeaturesCol) ++ categoricalFeatureCols
  
  println(s"Columnas a ensamblar:")
  println(s"  - Features numéricas: $numericFeaturesCol")
  println(s"  - Features categóricas: ${categoricalFeatureCols.mkString(", ")}")
  
  val assembler = new VectorAssembler()
    .setInputCols(allFeatureCols.toArray)
    .setOutputCol(outputCol)
    .setHandleInvalid("keep")
  
  val finalDF = assembler.transform(df)
  
  // Obtener dimensión del vector de features
  val featureVector = finalDF.select(outputCol).first().getAs[Vector](0)
  val featureDim = featureVector.size
  
  println(s"  ✓ Vector '$outputCol' creado con dimensión: $featureDim")
  
  (finalDF, assembler)
}

// ============================================================================
// PIPELINE COMPLETO DE TRANSFORMACIÓN
// ============================================================================

/**
 * Aplica pipeline completo de transformación
 * 
 * @param df DataFrame limpio
 * @param scalerType "standard" o "minmax"
 * @return Tupla (DataFrame transformado, Map con modelos entrenados)
 */
def transformData(df: DataFrame, scalerType: String = "standard"): 
    (DataFrame, Map[String, Any]) = {
  
  println("\n" + "=" * 80)
  println("INICIANDO PIPELINE DE TRANSFORMACIÓN")
  println("=" * 80)
  
  var transformedDF = df
  val models = scala.collection.mutable.Map[String, Any]()
  
  // Paso 1: Mapear tipos de ataque a categorías
  transformedDF = mapAttackCategories(transformedDF)
  
  // Paso 2: Seleccionar atributos
  transformedDF = selectFeatures(transformedDF)
  
  // Paso 3: Codificar variables categóricas
  val categoricalCols = Seq("protocol_type", "service", "flag")
  val (dfEncoded, stringIndexers) = encodeCategorialFeatures(transformedDF, categoricalCols)
  transformedDF = dfEncoded
  models("stringIndexers") = stringIndexers
  
  // Paso 4: Aplicar One-Hot Encoding
  val (dfOneHot, oneHotEncoders) = oneHotEncode(transformedDF, categoricalCols)
  transformedDF = dfOneHot
  models("oneHotEncoders") = oneHotEncoders
  
  // Paso 5: Identificar features numéricas
  val numericFeatures = Seq(
    "duration", "src_bytes", "dst_bytes", "land", "wrong_fragment", "urgent",
    "hot", "num_failed_logins", "logged_in", "num_compromised", "root_shell",
    "su_attempted", "num_root", "num_file_creations", "num_shells", "num_access_files",
    "is_host_login", "is_guest_login", "count", "srv_count",
    "serror_rate", "srv_serror_rate", "rerror_rate", "srv_rerror_rate",
    "same_srv_rate", "diff_srv_rate", "srv_diff_host_rate",
    "dst_host_count", "dst_host_srv_count", "dst_host_same_srv_rate",
    "dst_host_diff_srv_rate", "dst_host_same_src_port_rate",
    "dst_host_srv_diff_host_rate", "dst_host_serror_rate",
    "dst_host_srv_serror_rate", "dst_host_rerror_rate", "dst_host_srv_rerror_rate"
  )
  
  // Paso 6: Ensamblar features numéricas
  val (dfNumericAssembled, numericAssembler) = assembleNumericFeatures(
    transformedDF, numericFeatures, "numeric_features"
  )
  transformedDF = dfNumericAssembled
  models("numericAssembler") = numericAssembler
  
  // Paso 7: Escalar features numéricas
  val (dfScaled, scalerModel) = scalerType match {
    case "standard" => standardScaleFeatures(transformedDF, "numeric_features", "scaled_numeric_features")
    case "minmax" => minMaxScaleFeatures(transformedDF, "numeric_features", "scaled_numeric_features")
    case _ => 
      println(s"⚠ Tipo de scaler '$scalerType' no reconocido. Usando standard.")
      standardScaleFeatures(transformedDF, "numeric_features", "scaled_numeric_features")
  }
  transformedDF = dfScaled
  models("scaler") = scalerModel
  
  // Paso 8: Crear labels
  val (dfLabeled, labelIndexer) = createLabels(transformedDF)
  transformedDF = dfLabeled
  models("labelIndexer") = labelIndexer
  
  // Paso 9: Ensamblar vector final de features
  val categoricalEncodedCols = categoricalCols.map(_ + "_encoded")
  val (dfFinal, finalAssembler) = assembleFinalFeatures(
    transformedDF, "scaled_numeric_features", categoricalEncodedCols, "features"
  )
  transformedDF = dfFinal
  models("finalAssembler") = finalAssembler
  
  // Paso 10: Seleccionar solo columnas necesarias para ML
  val finalDF = transformedDF.select("features", "label", "attack_category", "attack_type")
  
  println("\n" + "=" * 80)
  println("TRANSFORMACIÓN COMPLETADA")
  println("=" * 80)
  println(s"Columnas finales: ${finalDF.columns.mkString(", ")}")
  println(s"Total de registros: ${finalDF.count()}")
  
  // Mostrar ejemplo de datos transformados
  println("\nEjemplo de datos transformados:")
  finalDF.show(5, truncate = false)
  
  (finalDF, models.toMap)
}

/**
 * Aplica transformaciones a conjunto de prueba usando modelos entrenados
 * IMPORTANTE: No se entrenan nuevos modelos, se usan los del conjunto de entrenamiento
 * 
 * @param testDF DataFrame de prueba
 * @param models Map con modelos entrenados del conjunto de entrenamiento
 * @return DataFrame transformado
 */
def transformTestData(testDF: DataFrame, models: Map[String, Any]): DataFrame = {
  println("\n" + "=" * 80)
  println("TRANSFORMANDO CONJUNTO DE PRUEBA")
  println("=" * 80)
  println("⚠ Usando modelos entrenados del conjunto de entrenamiento")
  
  var transformedDF = testDF
  
  // Paso 1: Mapear categorías (sin entrenamiento)
  transformedDF = mapAttackCategories(transformedDF)
  
  // Paso 2: Seleccionar atributos
  transformedDF = selectFeatures(transformedDF)
  
  // Paso 3-4: Codificación categórica (aplicar modelos entrenados)
  val categoricalCols = Seq("protocol_type", "service", "flag")
  val stringIndexers = models("stringIndexers").asInstanceOf[Array[StringIndexer]]
  
  categoricalCols.zip(stringIndexers).foreach { case (colName, indexer) =>
    val indexerModel = indexer.fit(transformedDF) // Nota: idealmente usar el modelo ya entrenado
    transformedDF = indexerModel.transform(transformedDF)
  }
  
  val oneHotEncoders = models("oneHotEncoders").asInstanceOf[Array[OneHotEncoder]]
  categoricalCols.zip(oneHotEncoders).foreach { case (colName, encoder) =>
    val encoderModel = encoder.fit(transformedDF)
    transformedDF = encoderModel.transform(transformedDF)
  }
  
  // Paso 5-6: Ensamblar y escalar numéricas
  val numericAssembler = models("numericAssembler").asInstanceOf[VectorAssembler]
  transformedDF = numericAssembler.transform(transformedDF)
  
  val scaler = models("scaler")
  transformedDF = scaler match {
    case s: StandardScalerModel => s.transform(transformedDF)
    case s: MinMaxScalerModel => s.transform(transformedDF)
    case _ => transformedDF
  }
  
  // Paso 7: Crear labels
  val labelIndexerModel = models("labelIndexer").asInstanceOf[StringIndexerModel]
  transformedDF = labelIndexerModel.transform(transformedDF)
  
  // Paso 8: Ensamblar features finales
  val finalAssembler = models("finalAssembler").asInstanceOf[VectorAssembler]
  transformedDF = finalAssembler.transform(transformedDF)
  
  val finalDF = transformedDF.select("features", "label", "attack_category", "attack_type")
  
  println(s"✓ Conjunto de prueba transformado: ${finalDF.count()} registros")
  
  finalDF
}

println("\n✓ Módulo de transformación cargado correctamente")
println("✓ Funciones disponibles:")
println("  - transformData(df, scalerType): Pipeline completo de transformación")
println("  - transformTestData(df, models): Transformar conjunto de prueba")
println("  - mapAttackCategories(df): Mapear tipos de ataque")
println("  - selectFeatures(df): Seleccionar atributos")
println("  - encodeCategorialFeatures(df, cols): Codificar categóricos")
println("  - createLabels(df): Crear labels numéricos")
println("  - assembleFinalFeatures(df, ...): Ensamblar vector de features")
