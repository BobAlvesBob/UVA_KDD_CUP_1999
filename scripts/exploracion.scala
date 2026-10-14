/*
 * Script de Exploración del Dataset KDD Cup 1999
 * 
 * Propósito: Realizar análisis exploratorio inicial de los datos
 * - Cargar el dataset
 * - Mostrar estadísticas básicas
 * - Analizar distribución de clases
 * - Identificar valores ausentes
 * - Calcular estadísticas descriptivas
 * 
 * Autor: Grupo [X]
 * Fecha: Octubre 2026
 * Asignatura: TESCC - Clasificadores
 */

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

println("=" * 80)
println("EXPLORACIÓN DEL DATASET KDD CUP 1999")
println("=" * 80)

// Variable PATH para la localización del fichero
// Los profesores modificarán esta ruta para ejecutar en sus máquinas
val PATH = "/home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/"

// Nombre del archivo de datos
val DATA_FILE = "kddcup.data"

// Definir los nombres de las columnas (el archivo no tiene cabecera)
val columnNames = Array(
  "duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes",
  "land", "wrong_fragment", "urgent", "hot", "num_failed_logins", "logged_in",
  "num_compromised", "root_shell", "su_attempted", "num_root", "num_file_creations",
  "num_shells", "num_access_files", "num_outbound_cmds", "is_host_login",
  "is_guest_login", "count", "srv_count", "serror_rate", "srv_serror_rate",
  "rerror_rate", "srv_rerror_rate", "same_srv_rate", "diff_srv_rate",
  "srv_diff_host_rate", "dst_host_count", "dst_host_srv_count",
  "dst_host_same_srv_rate", "dst_host_diff_srv_rate", "dst_host_same_src_port_rate",
  "dst_host_srv_diff_host_rate", "dst_host_serror_rate", "dst_host_srv_serror_rate",
  "dst_host_rerror_rate", "dst_host_srv_rerror_rate", "attack_type"
)

// Definir el schema del dataset
// NOTA: nullable = true permite manejar valores vacíos/nulos en los datos
val schema = StructType(Array(
  StructField("duration", IntegerType, nullable = true),
  StructField("protocol_type", StringType, nullable = true),
  StructField("service", StringType, nullable = true),
  StructField("flag", StringType, nullable = true),
  StructField("src_bytes", IntegerType, nullable = true),
  StructField("dst_bytes", IntegerType, nullable = true),
  StructField("land", IntegerType, nullable = true),
  StructField("wrong_fragment", IntegerType, nullable = true),
  StructField("urgent", IntegerType, nullable = true),
  StructField("hot", IntegerType, nullable = true),
  StructField("num_failed_logins", IntegerType, nullable = true),
  StructField("logged_in", IntegerType, nullable = true),
  StructField("num_compromised", IntegerType, nullable = true),
  StructField("root_shell", IntegerType, nullable = true),
  StructField("su_attempted", IntegerType, nullable = true),
  StructField("num_root", IntegerType, nullable = true),
  StructField("num_file_creations", IntegerType, nullable = true),
  StructField("num_shells", IntegerType, nullable = true),
  StructField("num_access_files", IntegerType, nullable = true),
  StructField("num_outbound_cmds", IntegerType, nullable = true),
  StructField("is_host_login", IntegerType, nullable = true),
  StructField("is_guest_login", IntegerType, nullable = true),
  StructField("count", IntegerType, nullable = true),
  StructField("srv_count", IntegerType, nullable = true),
  StructField("serror_rate", DoubleType, nullable = true),
  StructField("srv_serror_rate", DoubleType, nullable = true),
  StructField("rerror_rate", DoubleType, nullable = true),
  StructField("srv_rerror_rate", DoubleType, nullable = true),
  StructField("same_srv_rate", DoubleType, nullable = true),
  StructField("diff_srv_rate", DoubleType, nullable = true),
  StructField("srv_diff_host_rate", DoubleType, nullable = true),
  StructField("dst_host_count", IntegerType, nullable = true),
  StructField("dst_host_srv_count", IntegerType, nullable = true),
  StructField("dst_host_same_srv_rate", DoubleType, nullable = true),
  StructField("dst_host_diff_srv_rate", DoubleType, nullable = true),
  StructField("dst_host_same_src_port_rate", DoubleType, nullable = true),
  StructField("dst_host_srv_diff_host_rate", DoubleType, nullable = true),
  StructField("dst_host_serror_rate", DoubleType, nullable = true),
  StructField("dst_host_srv_serror_rate", DoubleType, nullable = true),
  StructField("dst_host_rerror_rate", DoubleType, nullable = true),
  StructField("dst_host_srv_rerror_rate", DoubleType, nullable = true),
  StructField("attack_type", StringType, nullable = true)
))

println("\n1. CARGANDO DATOS...")
println("-" * 80)

// Cargar el dataset
// NOTA: mode=PERMISSIVE permite registros con errores de parsing (los marca como null)
//       emptyValue=null convierte strings vacíos en null para campos numéricos
val rawData = spark.read
  .format("csv")
  .option("header", "false")
  .option("inferSchema", "false")
  .option("mode", "PERMISSIVE")
  .option("emptyValue", null)
  .option("nullValue", "")
  .schema(schema)
  .load(PATH + DATA_FILE)

println(s"✓ Datos cargados desde: ${PATH}${DATA_FILE}")
println(s"✓ Número total de registros: ${rawData.count()}")
println(s"✓ Número de columnas: ${rawData.columns.length}")

println("\n2. SCHEMA DEL DATASET")
println("-" * 80)
rawData.printSchema()

println("\n3. PRIMERAS 10 FILAS DEL DATASET")
println("-" * 80)
rawData.show(10, truncate = false)

println("\n4. ESTADÍSTICAS BÁSICAS DEL DATASET")
println("-" * 80)
println(s"Número total de registros: ${rawData.count()}")
println(s"Número de columnas: ${rawData.columns.length}")
println(s"Número de particiones: ${rawData.rdd.getNumPartitions}")

println("\n5. ANÁLISIS DE VALORES AUSENTES/NULOS")
println("-" * 80)

// Contar valores nulos por columna
// NOTA: isNaN solo funciona con tipos Double/Float, no con Integer o String
val nullCounts = rawData.columns.map { colName =>
  val dataType = rawData.schema(colName).dataType
  val nullCount = dataType match {
    case DoubleType | FloatType => 
      rawData.filter(col(colName).isNull || col(colName).isNaN).count()
    case StringType => 
      rawData.filter(col(colName).isNull || col(colName) === "").count()
    case _ => 
      rawData.filter(col(colName).isNull).count()
  }
  (colName, nullCount)
}

println("Columnas con valores ausentes:")
nullCounts.filter(_._2 > 0).foreach { case (colName, count) =>
  println(f"  - $colName%-35s: $count%10d valores ausentes")
}

if (nullCounts.filter(_._2 > 0).isEmpty) {
  println("  ✓ No se encontraron valores ausentes/nulos en ninguna columna")
}

println("\n6. DISTRIBUCIÓN DE LA VARIABLE OBJETIVO (attack_type)")
println("-" * 80)

// Mapeo de tipos de ataque específicos a categorías generales
val attackCategoryMap = Map(
  "normal" -> "normal",
  // DoS attacks
  "back" -> "DoS", "land" -> "DoS", "neptune" -> "DoS", "pod" -> "DoS", 
  "smurf" -> "DoS", "teardrop" -> "DoS",
  // Probe attacks
  "ipsweep" -> "Probe", "nmap" -> "Probe", "portsweep" -> "Probe", "satan" -> "Probe",
  // R2L attacks
  "ftp_write" -> "R2L", "guess_passwd" -> "R2L", "imap" -> "R2L", "multihop" -> "R2L",
  "phf" -> "R2L", "spy" -> "R2L", "warezclient" -> "R2L", "warezmaster" -> "R2L",
  // U2R attacks
  "buffer_overflow" -> "U2R", "loadmodule" -> "U2R", "perl" -> "U2R", "rootkit" -> "U2R"
)

// UDF para mapear tipos de ataque a categorías
val attackCategoryUDF = udf((attackType: String) => {
  // Remover el punto al final si existe (el dataset tiene "normal." en lugar de "normal")
  val cleanType = attackType.stripSuffix(".")
  attackCategoryMap.getOrElse(cleanType, "unknown")
})

// Añadir columna de categoría de ataque
val dataWithCategory = rawData.withColumn("attack_category", attackCategoryUDF(col("attack_type")))

println("\nDistribución de tipos de ataque específicos (top 25):")
val attackTypeCounts = dataWithCategory.groupBy("attack_type").count().orderBy(desc("count"))
attackTypeCounts.show(25, truncate = false)

println("\nDistribución de categorías de ataque:")
val totalRecords = dataWithCategory.count().toDouble
val categoryCounts = dataWithCategory.groupBy("attack_category").count().orderBy(desc("count"))

categoryCounts.collect().foreach { row =>
  val category = row.getString(0)
  val count = row.getLong(1)
  val percentage = (count / totalRecords) * 100
  println(f"  - $category%-15s: $count%10d registros ($percentage%6.2f%%)")
}

println("\n7. ANÁLISIS DE ATRIBUTOS CATEGÓRICOS")
println("-" * 80)

println("\n7.1. protocol_type:")
rawData.groupBy("protocol_type").count().orderBy(desc("count")).show(10, false)

println("\n7.2. service (top 20):")
rawData.groupBy("service").count().orderBy(desc("count")).show(20, false)

println("\n7.3. flag:")
rawData.groupBy("flag").count().orderBy(desc("count")).show(15, false)

println("\n8. ESTADÍSTICAS DESCRIPTIVAS DE ATRIBUTOS NUMÉRICOS CONTINUOS")
println("-" * 80)

// Seleccionar atributos numéricos continuos principales
val continuousAttrs = Seq("duration", "src_bytes", "dst_bytes", "wrong_fragment", "urgent")

println("\nEstadísticas para atributos numéricos continuos:")
rawData.select(continuousAttrs.map(col): _*).describe().show()

println("\n9. ESTADÍSTICAS DESCRIPTIVAS DE ATRIBUTOS NUMÉRICOS DISCRETOS")
println("-" * 80)

// Seleccionar algunos atributos discretos importantes
val discreteAttrs = Seq("hot", "num_failed_logins", "count", "srv_count", "dst_host_count")

println("\nEstadísticas para atributos numéricos discretos:")
rawData.select(discreteAttrs.map(col): _*).describe().show()

println("\n10. ESTADÍSTICAS DESCRIPTIVAS DE ATRIBUTOS TIPO RATE (PROPORCIONES)")
println("-" * 80)

val rateAttrs = Seq("serror_rate", "srv_serror_rate", "rerror_rate", "srv_rerror_rate",
                    "same_srv_rate", "diff_srv_rate")

println("\nEstadísticas para atributos de tipo rate:")
rawData.select(rateAttrs.map(col): _*).describe().show()

println("\n11. ANÁLISIS DE ATRIBUTOS BINARIOS")
println("-" * 80)

val binaryAttrs = Seq("land", "logged_in", "root_shell", "is_host_login", "is_guest_login")

binaryAttrs.foreach { attr =>
  println(s"\n$attr:")
  rawData.groupBy(attr).count().orderBy(attr).show(false)
}

println("\n12. DETECCIÓN INICIAL DE OUTLIERS (Método IQR)")
println("-" * 80)

// Función para detectar outliers usando el método IQR
def detectOutliers(df: org.apache.spark.sql.DataFrame, colName: String): (Long, Double, Double, Double, Double) = {
  val quantiles = df.stat.approxQuantile(colName, Array(0.25, 0.75), 0.0)
  val q1 = quantiles(0)
  val q3 = quantiles(1)
  val iqr = q3 - q1
  val lowerBound = q1 - 1.5 * iqr
  val upperBound = q3 + 1.5 * iqr
  
  val outlierCount = df.filter(col(colName) < lowerBound || col(colName) > upperBound).count()
  
  (outlierCount, q1, q3, lowerBound, upperBound)
}

println("\nAnálisis de outliers para atributos numéricos continuos:")
println(f"${"Atributo"}%-20s ${"Outliers"}%12s ${"Q1"}%12s ${"Q3"}%12s ${"Lower"}%12s ${"Upper"}%12s")
println("-" * 90)

continuousAttrs.foreach { attr =>
  val (outlierCount, q1, q3, lower, upper) = detectOutliers(rawData, attr)
  val totalCount = rawData.count()
  val outlierPercentage = (outlierCount.toDouble / totalCount) * 100
  println(f"$attr%-20s $outlierCount%12d $q1%12.2f $q3%12.2f $lower%12.2f $upper%12.2f ($outlierPercentage%.2f%%)")
}

println("\n13. ANÁLISIS DE CORRELACIÓN ENTRE VARIABLES CATEGÓRICAS Y OBJETIVO")
println("-" * 80)

println("\n13.1. Distribución de protocol_type por attack_category:")
dataWithCategory.groupBy("protocol_type", "attack_category").count()
  .orderBy(col("protocol_type"), desc("count")).show(20, false)

println("\n13.2. Distribución de flag por attack_category (top 20 combinaciones):")
dataWithCategory.groupBy("flag", "attack_category").count()
  .orderBy(desc("count")).show(20, false)

println("\n14. ANÁLISIS DEL ATRIBUTO num_outbound_cmds")
println("-" * 80)
println("Este atributo es conocido por tener siempre valor 0 en KDD Cup 1999")

val outboundStats = rawData.select("num_outbound_cmds").describe()
outboundStats.show()

val nonZeroOutbound = rawData.filter(col("num_outbound_cmds") =!= 0).count()
println(s"Registros con num_outbound_cmds != 0: $nonZeroOutbound")
println("Conclusión: Este atributo puede ser candidato para eliminación")

println("\n15. RESUMEN DE CALIDAD DE DATOS")
println("-" * 80)

val totalRows = rawData.count()
val totalCols = rawData.columns.length

println(s"Total de registros: $totalRows")
println(s"Total de columnas: $totalCols (41 features + 1 label)")
println(s"Registros completamente nulos: 0 (se verificará en limpieza)")
println(s"Columnas con valores ausentes: ${nullCounts.filter(_._2 > 0).length}")

println("\nClases identificadas:")
categoryCounts.collect().foreach { row =>
  val category = row.getString(0)
  val count = row.getLong(1)
  val percentage = (count / totalRecords) * 100
  println(f"  - $category%-15s: $count%10d ($percentage%6.2f%%)")
}

println("\nProblemas de calidad identificados:")
println("  1. Desbalanceo extremo de clases (DoS: ~79%, U2R: ~0.01%)")
println("  2. Atributo num_outbound_cmds sin variación (candidato a eliminar)")
println("  3. Posibles outliers en atributos continuos (requiere análisis contextual)")
println("  4. Gran número de categorías en 'service' (70 valores) - requerirá encoding")

println("\n" + "=" * 80)
println("EXPLORACIÓN COMPLETADA")
println("=" * 80)

// Cachear el dataframe con categoría para uso posterior
dataWithCategory.cache()

println("\n✓ DataFrame 'dataWithCategory' cacheado y disponible para análisis adicional")
println("✓ Use dataWithCategory.show() para ver los datos con la columna attack_category")
