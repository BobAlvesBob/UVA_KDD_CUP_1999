# Cómo Ejecutar los Scripts de Spark

## Problema: "value apache is not a member of org"

Este error aparece cuando se intenta compilar/ejecutar scripts Spark con el compilador Scala estándar (`scala` o `scalac`) en lugar de las herramientas de Spark.

## Solución

Los scripts `.scala` en esta carpeta **DEBEN ejecutarse con `spark-shell`**, no con el compilador Scala estándar.

### Ejecución Correcta

```bash
# Ejecutar el script de exploración
spark-shell -i scripts/exploracion.scala

# Con más memoria (recomendado para datasets grandes)
spark-shell --driver-memory 4g -i scripts/exploracion.scala

# Con configuración específica
spark-shell --master local[2] --driver-memory 4g -i scripts/exploracion.scala
```

###  Incorrecto
```bash
scala scripts/exploracion.scala  # ERROR: "value apache is not a member of org"
```

###  Correcto
```bash
spark-shell -i scripts/exploracion.scala  # Funciona correctamente
```

## Verificación

Para verificar que Spark está instalado:
```bash
which spark-shell
spark-shell --version
```

## Notas

- Los imports de Spark (`org.apache.spark.*`) solo están disponibles en el entorno de Spark
- `spark-shell` proporciona automáticamente el SparkSession como variable `spark`
- El flag `-i` ejecuta el script en modo interactivo
