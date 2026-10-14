/*
 * Script de Limpieza del Dataset KDD Cup 1999
 * 
 * Propósito: Funciones para limpiar y preparar los datos
 * - Eliminar registros vacíos o no válidos
 * - Tratar valores ausentes
 * - Detectar y manejar outliers
 * - Calcular métricas NTAIE y NTOE
 * 
 * Autor: Grupo [X]
 * Fecha: Octubre 2026
 * Asignatura: TESCC - Clasificadores
 */

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

println("=" * 80)
println("MÓDULO DE LIMPIEZA DE DATOS - KDD CUP 1999")
println("=" * 80)

// ============================================================================
// FUNCIONES DE LIMPIEZA
// ============================================================================

/**
 * Elimina registros completamente vacíos o con valores nulos
 * 
 * @param df DataFrame original
 * @return DataFrame sin registros vacíos
 */
def removeEmptyRecords(df: DataFrame): DataFrame = {
  println("\n--- Eliminando registros vacíos ---")
  val initialCount = df.count()
  
  // Eliminar filas donde TODAS las columnas son nulas
  val cleanedDF = df.na.drop("all")
  
  val finalCount = cleanedDF.count()
  val removedCount = initialCount - finalCount
  
  println(s"Registros iniciales: $initialCount")
  println(s"Registros eliminados: $removedCount")
  println(s"Registros finales: $finalCount")
  
  cleanedDF
}

/**
 * Valida que todos los registros tengan el formato correcto
 * Elimina registros con tipos de datos incorrectos o valores fuera de rango esperado
 * 
 * @param df DataFrame a validar
 * @return DataFrame con solo registros válidos
 */
def validateRecords(df: DataFrame): DataFrame = {
  println("\n--- Validando formato de registros ---")
  val initialCount = df.count()
  
  // Validar que los valores binarios estén en {0, 1}
  val binaryColumns = Seq("land", "logged_in", "root_shell", "is_host_login", "is_guest_login")
  var validatedDF = df
  
  binaryColumns.foreach { colName =>
    validatedDF = validatedDF.filter(col(colName).isin(0, 1))
  }
  
  // Validar que las proporciones (rates) estén en [0, 1]
  val rateColumns = df.columns.filter(_.endsWith("_rate"))
  rateColumns.foreach { colName =>
    validatedDF = validatedDF.filter(col(colName) >= 0.0 && col(colName) <= 1.0)
  }
  
  // Validar que duration, bytes, etc. no sean negativos
  val nonNegativeColumns = Seq("duration", "src_bytes", "dst_bytes", "wrong_fragment", "urgent",
                                "hot", "num_failed_logins", "count", "srv_count")
  nonNegativeColumns.foreach { colName =>
    validatedDF = validatedDF.filter(col(colName) >= 0)
  }
  
  val finalCount = validatedDF.count()
  val removedCount = initialCount - finalCount
  
  println(s"Registros iniciales: $initialCount")
  println(s"Registros inválidos: $removedCount")
  println(s"Registros válidos: $finalCount")
  
  validatedDF
}

/**
 * Trata valores ausentes en el DataFrame
 * Estrategia: Dado que KDD Cup 1999 no tiene valores ausentes típicamente,
 * esta función está preparada por si se encuentran
 * 
 * @param df DataFrame con posibles valores ausentes
 * @return DataFrame con valores ausentes tratados
 */
def handleMissingValues(df: DataFrame): DataFrame = {
  println("\n--- Tratando valores ausentes ---")
  
  // Contar valores nulos por columna
  val nullCounts = df.columns.map { colName =>
    (colName, df.filter(col(colName).isNull).count())
  }
  
  val columnsWithNulls = nullCounts.filter(_._2 > 0)
  
  if (columnsWithNulls.isEmpty) {
    println("✓ No se encontraron valores ausentes")
    return df
  }
  
  println("Columnas con valores ausentes:")
  columnsWithNulls.foreach { case (colName, count) =>
    println(f"  - $colName%-35s: $count%10d valores ausentes")
  }
  
  // Estrategia de imputación
  var cleanedDF = df
  
  // Para atributos numéricos: imputar con la mediana
  val numericColumns = df.schema.fields
    .filter(f => f.dataType == IntegerType || f.dataType == DoubleType)
    .map(_.name)
  
  numericColumns.foreach { colName =>
    val nullCount = df.filter(col(colName).isNull).count()
    if (nullCount > 0) {
      // Calcular mediana (usando percentil 0.5)
      val median = df.stat.approxQuantile(colName, Array(0.5), 0.0)(0)
      cleanedDF = cleanedDF.na.fill(median, Seq(colName))
      println(s"  ✓ Imputados $nullCount valores en $colName con mediana: $median")
    }
  }
  
  // Para atributos categóricos: imputar con la moda o una categoría "unknown"
  val categoricalColumns = Seq("protocol_type", "service", "flag")
  categoricalColumns.foreach { colName =>
    val nullCount = df.filter(col(colName).isNull).count()
    if (nullCount > 0) {
      // Obtener la moda (valor más frecuente)
      val mode = df.groupBy(colName).count().orderBy(desc("count")).first().getString(0)
      cleanedDF = cleanedDF.na.fill(mode, Seq(colName))
      println(s"  ✓ Imputados $nullCount valores en $colName con moda: $mode")
    }
  }
  
  cleanedDF
}

/**
 * Detecta outliers usando el método IQR (Interquartile Range)
 * 
 * @param df DataFrame a analizar
 * @param colName Nombre de la columna
 * @param factor Factor multiplicador del IQR (por defecto 1.5)
 * @return Tupla con (DataFrame con columna outlier_flag, lowerBound, upperBound, outlierCount)
 */
def detectOutliersIQR(df: DataFrame, colName: String, factor: Double = 1.5): 
    (DataFrame, Double, Double, Long) = {
  
  // Calcular cuartiles
  val quantiles = df.stat.approxQuantile(colName, Array(0.25, 0.75), 0.0)
  val q1 = quantiles(0)
  val q3 = quantiles(1)
  val iqr = q3 - q1
  
  val lowerBound = q1 - factor * iqr
  val upperBound = q3 + factor * iqr
  
  // Marcar outliers
  val dfWithFlag = df.withColumn(
    s"${colName}_outlier",
    when(col(colName) < lowerBound || col(colName) > upperBound, 1).otherwise(0)
  )
  
  val outlierCount = dfWithFlag.filter(col(s"${colName}_outlier") === 1).count()
  
  (dfWithFlag, lowerBound, upperBound, outlierCount)
}

/**
 * Maneja outliers en atributos numéricos continuos
 * Estrategia: Winsorización (cap values) en lugar de eliminación para no perder datos
 * 
 * @param df DataFrame original
 * @param columnsToProcess Columnas a procesar
 * @param action "cap" (winsorización) o "remove" (eliminación)
 * @return DataFrame con outliers manejados
 */
def handleOutliers(df: DataFrame, columnsToProcess: Seq[String], action: String = "cap"): DataFrame = {
  println(s"\n--- Manejando outliers (acción: $action) ---")
  
  var processedDF = df
  var totalOutliersRemoved = 0L
  
  columnsToProcess.foreach { colName =>
    println(s"\nProcesando columna: $colName")
    
    val (dfWithFlag, lowerBound, upperBound, outlierCount) = detectOutliersIQR(processedDF, colName)
    val totalRecords = processedDF.count()
    val outlierPercentage = (outlierCount.toDouble / totalRecords) * 100
    
    println(f"  Q1: $lowerBound%.2f, Q3: $upperBound%.2f")
    println(f"  Outliers detectados: $outlierCount ($outlierPercentage%.2f%%)")
    
    action match {
      case "cap" =>
        // Winsorización: limitar valores a los bounds
        processedDF = dfWithFlag.withColumn(
          colName,
          when(col(colName) < lowerBound, lowerBound)
            .when(col(colName) > upperBound, upperBound)
            .otherwise(col(colName))
        ).drop(s"${colName}_outlier")
        println(s"  ✓ Valores limitados a [$lowerBound, $upperBound]")
        
      case "remove" =>
        // Eliminar registros con outliers
        processedDF = dfWithFlag.filter(col(s"${colName}_outlier") === 0).drop(s"${colName}_outlier")
        totalOutliersRemoved += outlierCount
        println(s"  ✓ $outlierCount registros eliminados")
        
      case _ =>
        println(s"  ! Acción '$action' no reconocida. Manteniendo datos sin cambios.")
        processedDF = dfWithFlag.drop(s"${colName}_outlier")
    }
  }
  
  if (action == "remove" && totalOutliersRemoved > 0) {
    println(f"\nTotal de registros eliminados por outliers: $totalOutliersRemoved")
  }
  
  processedDF
}

/**
 * Calcula métricas NTAIE (Número Total de Ausentes/Inconsistentes Eliminados)
 * y NTOE (Número Total de Outliers Eliminados) para el conjunto de prueba
 * 
 * @param originalCount Número de registros originales
 * @param afterCleaningCount Número después de limpieza básica
 * @param afterOutlierCount Número después de eliminar outliers
 */
def calculateCleaningMetrics(originalCount: Long, afterCleaningCount: Long, afterOutlierCount: Long): Unit = {
  println("\n" + "=" * 80)
  println("MÉTRICAS DE LIMPIEZA")
  println("=" * 80)
  
  val ntaie = originalCount - afterCleaningCount
  val ntoe = afterCleaningCount - afterOutlierCount
  val totalRemoved = originalCount - afterOutlierCount
  
  val ntaiePercentage = (ntaie.toDouble / originalCount) * 100
  val ntoePercentage = (ntoe.toDouble / originalCount) * 100
  val totalRemovedPercentage = (totalRemoved.toDouble / originalCount) * 100
  
  println(f"Registros originales:                    $originalCount%10d")
  println(f"NTAIE (Ausentes/Inconsistentes):         $ntaie%10d ($ntaiePercentage%5.2f%%)")
  println(f"NTOE (Outliers):                         $ntoe%10d ($ntoePercentage%5.2f%%)")
  println(f"Total de registros eliminados:           $totalRemoved%10d ($totalRemovedPercentage%5.2f%%)")
  println(f"Registros finales:                       $afterOutlierCount%10d")
  println()
  println(f"Tasa de no clasificados: $totalRemovedPercentage%.4f%%")
  println("(Debe ser al menos un orden de magnitud menor que la tasa de error esperada)")
  println("=" * 80)
}

/**
 * Pipeline completo de limpieza
 * 
 * @param df DataFrame original
 * @param handleOutliersAction "cap", "remove", o "keep"
 * @return DataFrame limpio
 */
def cleanData(df: DataFrame, handleOutliersAction: String = "cap"): DataFrame = {
  println("\n" + "=" * 80)
  println("INICIANDO PIPELINE DE LIMPIEZA")
  println("=" * 80)
  
  val initialCount = df.count()
  
  // Paso 1: Eliminar registros vacíos
  var cleanedDF = removeEmptyRecords(df)
  
  // Paso 2: Validar formato de registros
  cleanedDF = validateRecords(cleanedDF)
  
  // Paso 3: Tratar valores ausentes
  cleanedDF = handleMissingValues(cleanedDF)
  
  val afterBasicCleaningCount = cleanedDF.count()
  
  // Paso 4: Manejar outliers (si se requiere)
  if (handleOutliersAction != "keep") {
    val continuousAttrs = Seq("duration", "src_bytes", "dst_bytes")
    cleanedDF = handleOutliers(cleanedDF, continuousAttrs, handleOutliersAction)
  } else {
    println("\n--- Outliers mantenidos (no se aplica tratamiento) ---")
  }
  
  val finalCount = cleanedDF.count()
  
  // Calcular métricas
  calculateCleaningMetrics(initialCount, afterBasicCleaningCount, finalCount)
  
  println("\n✓ LIMPIEZA COMPLETADA")
  
  cleanedDF
}

/**
 * Limpieza específica para el conjunto de prueba
 * Aplica las mismas transformaciones que al conjunto de entrenamiento,
 * pero registra NTAIE y NTOE para la evaluación
 * 
 * @param testDF DataFrame de prueba
 * @param handleOutliersAction Acción para outliers
 * @return Tupla (DataFrame limpio, NTAIE, NTOE)
 */
def cleanTestData(testDF: DataFrame, handleOutliersAction: String = "cap"): (DataFrame, Long, Long) = {
  println("\n" + "=" * 80)
  println("LIMPIEZA DEL CONJUNTO DE PRUEBA")
  println("=" * 80)
  
  val initialCount = testDF.count()
  
  var cleanedDF = removeEmptyRecords(testDF)
  cleanedDF = validateRecords(cleanedDF)
  cleanedDF = handleMissingValues(cleanedDF)
  
  val afterBasicCleaningCount = cleanedDF.count()
  val ntaie = initialCount - afterBasicCleaningCount
  
  if (handleOutliersAction != "keep") {
    val continuousAttrs = Seq("duration", "src_bytes", "dst_bytes")
    cleanedDF = handleOutliers(cleanedDF, continuousAttrs, handleOutliersAction)
  }
  
  val finalCount = cleanedDF.count()
  val ntoe = afterBasicCleaningCount - finalCount
  
  println("\n" + "=" * 80)
  println("MÉTRICAS PARA EVALUACIÓN DEL CONJUNTO DE PRUEBA")
  println("=" * 80)
  println(f"NTAIE: $ntaie")
  println(f"NTOE:  $ntoe")
  println(f"Total de registros no clasificados: ${ntaie + ntoe}")
  println(f"Tasa de no clasificados: ${((ntaie + ntoe).toDouble / initialCount) * 100}%.4f%%")
  println("=" * 80)
  
  (cleanedDF, ntaie, ntoe)
}

// ============================================================================
// FUNCIONES DE UTILIDAD
// ============================================================================

/**
 * Imprime un resumen de la calidad de datos después de la limpieza
 */
def printDataQualitySummary(df: DataFrame): Unit = {
  println("\n" + "=" * 80)
  println("RESUMEN DE CALIDAD DE DATOS POST-LIMPIEZA")
  println("=" * 80)
  
  println(s"Total de registros: ${df.count()}")
  println(s"Total de columnas: ${df.columns.length}")
  
  // Verificar valores nulos
  val nullCounts = df.columns.map { colName =>
    (colName, df.filter(col(colName).isNull).count())
  }
  
  val columnsWithNulls = nullCounts.filter(_._2 > 0)
  if (columnsWithNulls.isEmpty) {
    println("✓ No hay valores nulos en ninguna columna")
  } else {
    println(s"⚠ Columnas con valores nulos: ${columnsWithNulls.length}")
    columnsWithNulls.foreach { case (colName, count) =>
      println(f"  - $colName%-35s: $count%10d")
    }
  }
  
  // Verificar tipos de datos
  println("\nTipos de datos:")
  df.schema.fields.groupBy(_.dataType.typeName).foreach { case (typeName, fields) =>
    println(f"  - $typeName%-15s: ${fields.length}%3d columnas")
  }
  
  println("=" * 80)
}

println("\n✓ Módulo de limpieza cargado correctamente")
println("✓ Funciones disponibles:")
println("  - cleanData(df, action): Pipeline completo de limpieza")
println("  - cleanTestData(df, action): Limpieza para conjunto de prueba con métricas")
println("  - removeEmptyRecords(df): Eliminar registros vacíos")
println("  - validateRecords(df): Validar formato de registros")
println("  - handleMissingValues(df): Tratar valores ausentes")
println("  - handleOutliers(df, cols, action): Manejar outliers")
println("  - printDataQualitySummary(df): Imprimir resumen de calidad")
