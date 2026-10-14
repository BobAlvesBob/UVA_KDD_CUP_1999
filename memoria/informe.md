# Proyecto de Clasificación: Detección de Intrusiones en Redes
## Dataset KDD Cup 1999

**Asignatura:** Técnicas Escalables de Análisis de Datos en Entornos Big Data: Clasificadores  
**Primera Etapa:** Resumen Ejecutivo, Exploración y Preparación de Datos  
**Fecha:** 14 de octubre de 2026

**Integrantes del Grupo [NÚMERO]:**
- [Nombre Estudiante 1]
- [Nombre Estudiante 2]
- [Nombre Estudiante 3]

---

## Tabla de Contenidos

1. [Introducción](#introducción)
2. [Resumen Ejecutivo del Conjunto de Datos](#resumen-ejecutivo-del-conjunto-de-datos)
3. [Exploración de Datos](#exploración-de-datos)
   - 3.1 [Descripción del Problema](#31-descripción-del-problema)
   - 3.2 [Descripción Detallada del Conjunto de Datos](#32-descripción-detallada-del-conjunto-de-datos)
   - 3.3 [Análisis de Correlaciones](#33-análisis-de-correlaciones)
4. [Preparación de Datos](#preparación-de-datos)
   - 4.1 [Creación de Conjuntos de Entrenamiento y Prueba](#41-creación-de-conjuntos-de-entrenamiento-y-prueba)
   - 4.2 [Limpieza de Datos](#42-limpieza-de-datos)
   - 4.3 [Selección de Atributos](#43-selección-de-atributos)
   - 4.4 [Transformación de Datos](#44-transformación-de-datos)
5. [Conclusiones](#conclusiones)
6. [Distribución del Trabajo](#distribución-del-trabajo)
7. [Referencias Bibliográficas](#referencias-bibliográficas)
8. [Apéndices](#apéndices)

---

## 1. Introducción

El presente documento corresponde a la primera etapa del proyecto de clasificación de la asignatura "Técnicas Escalables de Análisis de Datos en Entornos Big Data: Clasificadores". El objetivo de esta etapa es realizar una exploración exhaustiva del conjunto de datos seleccionado, comprender sus características y prepararlo adecuadamente para la posterior construcción de modelos de clasificación utilizando Apache Spark ML.

Nuestro grupo ha seleccionado el conjunto de datos **KDD Cup 1999**, un benchmark clásico en el campo de la detección de intrusiones en redes. Este dataset presenta características que lo hacen ideal para el aprendizaje de técnicas de Big Data: gran volumen de datos (más de 4 millones de registros), diversidad en los tipos de atributos (numéricos y categóricos) y un problema de clasificación multiclase relevante en el ámbito de la ciberseguridad.

Los objetivos específicos de esta primera etapa son:
- Proporcionar un resumen ejecutivo del conjunto de datos
- Realizar una exploración detallada para comprender la naturaleza de los datos
- Identificar y documentar problemas de calidad de datos
- Preparar los datos mediante limpieza, selección y transformación
- Crear conjuntos de entrenamiento y prueba listos para el modelado

---

## 2. Resumen Ejecutivo del Conjunto de Datos

### Origen de los Datos

El conjunto de datos **KDD Cup 1999** fue creado para la competición "The Third International Knowledge Discovery and Data Mining Tools Competition" organizada en conjunción con KDD'99 y celebrada en San Diego, California. Los datos originales provienen del programa de evaluación de detección de intrusiones de DARPA (Defense Advanced Research Projects Agency) de 1998.

**Enlace oficial:** [KDD Cup 1999 Data - UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/datasets/KDD+Cup+1999+Data)

**Enlace de descarga directa:** http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html

### Propósito de los Datos

Los datos fueron recopilados con el objetivo de desarrollar y evaluar sistemas de detección de intrusiones (IDS - Intrusion Detection Systems) basados en aprendizaje automático. El dataset simula tráfico de red de una red militar típica, donde se incluyen tanto conexiones normales como diversos tipos de ataques cibernéticos.

El conjunto de datos ha sido ampliamente utilizado en:
- Investigación en ciberseguridad
- Desarrollo de sistemas de detección de intrusiones
- Benchmarking de algoritmos de clasificación
- Educación en machine learning y seguridad informática

### Tamaño del Conjunto de Datos

- **Conjunto completo (kddcup.data.gz):** 4.898.431 registros (aproximadamente 743 MB comprimido)
- **Conjunto 10% (kddcup.data_10_percent.gz):** 494.021 registros (aproximadamente 75 MB comprimido)
- **Conjunto de prueba (corrected.gz):** 311.029 registros

Para este proyecto utilizaremos el **conjunto completo de entrenamiento** con 4.898.431 instancias.

### Número Total de Atributos

El dataset contiene **42 columnas** en total:
- **41 atributos predictores (features)**
- **1 atributo objetivo (label)**

**Clasificación por tipo de atributo:**

| Tipo de Atributo | Cantidad | Ejemplos |
|-----------------|----------|----------|
| **Numéricos Continuos** | 9 | duration, src_bytes, dst_bytes, wrong_fragment, urgent |
| **Numéricos Discretos** | 23 | count, srv_count, serror_rate, srv_serror_rate, dst_host_count |
| **Nominales (Categóricos)** | 9 | protocol_type, service, flag, land, logged_in, is_host_login, is_guest_login |
| **Variable Objetivo** | 1 | attack_type (etiqueta de clase) |

### Descripción de la Clase

El atributo objetivo representa el tipo de conexión, clasificándose en **5 categorías principales:**

1. **normal:** Tráfico de red legítimo y normal
2. **DoS (Denial of Service):** Ataques de denegación de servicio
3. **Probe:** Exploración y escaneo de vulnerabilidades
4. **R2L (Remote to Local):** Acceso no autorizado desde una máquina remota
5. **U2R (User to Root):** Escalada de privilegios no autorizada

**Distribución aproximada de clases en el conjunto completo:**

| Categoría | Tipos de Ataque Específicos | Porcentaje Aproximado |
|-----------|----------------------------|----------------------|
| **normal** | normal | 19.69% |
| **DoS** | back, land, neptune, pod, smurf, teardrop | 79.24% |
| **Probe** | ipsweep, nmap, portsweep, satan | 0.83% |
| **R2L** | ftp_write, guess_passwd, imap, multihop, phf, spy, warezclient, warezmaster | 0.23% |
| **U2R** | buffer_overflow, loadmodule, perl, rootkit | 0.01% |

**Observaciones importantes:**
- Existe un **fuerte desbalanceo de clases**, con DoS representando casi el 80% de los registros
- Las clases minoritarias (U2R y R2L) presentan un desafío particular para la clasificación
- El dataset original contiene **23 tipos de ataques específicos** que se agrupan en las 4 categorías de ataque

---

## 3. Exploración de Datos

### 3.1 Descripción del Problema

#### Contexto del Problema

La detección de intrusiones en redes es un problema crítico en ciberseguridad. Los sistemas de detección de intrusiones (IDS) son herramientas fundamentales para identificar actividades maliciosas o no autorizadas en redes de computadoras. Tradicionalmente, estos sistemas se basaban en reglas y firmas conocidas, pero el aprendizaje automático ha demostrado ser efectivo para detectar patrones complejos y ataques desconocidos.

#### Definición del Problema de Clasificación

El problema planteado por el dataset KDD Cup 1999 es un **problema de clasificación multiclase supervisado** donde:

- **Entrada:** 41 características que describen una conexión de red
- **Salida:** Clasificación de la conexión como "normal" o uno de los 4 tipos de ataque (DoS, Probe, R2L, U2R)

#### Estrategias de Clasificación

Podemos abordar este problema de dos formas:
1. **Clasificación binaria:** Normal vs. Ataque (cualquier tipo)
2. **Clasificación multiclase:** Normal vs. DoS vs. Probe vs. R2L vs. U2R

Para este proyecto, adoptaremos la **clasificación multiclase en 5 categorías**, que es más desafiante y proporciona información más detallada sobre el tipo de ataque.

#### Importancia en Ciberseguridad

Los IDS basados en machine learning son importantes porque:
- Detectan patrones de ataque nuevos o variantes de ataques conocidos
- Se adaptan a la evolución de las amenazas cibernéticas
- Reducen falsos positivos en comparación con sistemas basados en reglas rígidas
- Permiten respuestas automatizadas ante amenazas detectadas

#### Desafíos del Problema

1. **Desbalanceo extremo de clases:** Especialmente en U2R y R2L
2. **Alta dimensionalidad:** 41 características requieren análisis cuidadoso
3. **Datos ruidosos:** Pueden existir errores en la captura o etiquetado
4. **Interpretabilidad:** Necesidad de entender qué características son más relevantes
5. **Eficiencia computacional:** Gran volumen de datos requiere procesamiento distribuido

---

### 3.2 Descripción Detallada del Conjunto de Datos

#### 3.2.1 Atributos del Dataset

A continuación se presenta una descripción detallada de los 41 atributos predictores, organizados por categorías funcionales. Para la descripción completa de cada atributo, consultar el **Apéndice A**.

**Categorías de Atributos:**

1. **Atributos Básicos de la Conexión (9 atributos)**
   - Información fundamental sobre la conexión TCP
   - Ejemplos: duration, protocol_type, service, flag, src_bytes, dst_bytes

2. **Atributos de Contenido (13 atributos)**
   - Características del contenido de la conexión
   - Ejemplos: hot, num_failed_logins, logged_in, num_compromised, root_shell

3. **Atributos de Tráfico Basados en Tiempo (9 atributos)**
   - Estadísticas calculadas en ventanas de tiempo (2 segundos)
   - Ejemplos: count, srv_count, serror_rate, srv_serror_rate

4. **Atributos de Tráfico Basados en Host (10 atributos)**
   - Estadísticas calculadas sobre el mismo host destino (últimas 100 conexiones)
   - Ejemplos: dst_host_count, dst_host_srv_count, dst_host_same_src_port_rate

#### 3.2.2 Análisis de Tipos de Datos

**[Completar con resultados de exploracion.scala]**

| Tipo de Dato | Cantidad | Nombres de Atributos |
|--------------|----------|---------------------|
| Entero | [X] | [lista] |
| Real (Double/Float) | [X] | [lista] |
| Categórico | [X] | [lista] |

#### 3.2.3 Estadísticas Descriptivas

**Atributos Numéricos Continuos**

**[Completar con resultados del script de exploración]**

Tabla de estadísticas para atributos continuos principales:

| Atributo | Media | Mediana | Desv. Std. | Mínimo | Máximo | Rango |
|----------|-------|---------|------------|---------|---------|-------|
| duration | [X] | [X] | [X] | [X] | [X] | [X] |
| src_bytes | [X] | [X] | [X] | [X] | [X] | [X] |
| dst_bytes | [X] | [X] | [X] | [X] | [X] | [X] |
| ... | ... | ... | ... | ... | ... | ... |

**Observaciones:**
- [Describir patrones observados en las estadísticas]
- [Identificar atributos con rangos muy amplios que requerirán normalización]
- [Comentar sobre asimetrías en las distribuciones]

#### 3.2.4 Análisis de Valores Ausentes

**[Completar con resultados de exploracion.scala]**

| Atributo | Número de Valores Ausentes | Porcentaje |
|----------|---------------------------|------------|
| [atributo1] | [X] | [X%] |
| [atributo2] | [X] | [X%] |

**Conclusión sobre valores ausentes:**
- [Estrategia para manejar valores ausentes]

#### 3.2.5 Análisis de Valores Categóricos

**protocol_type (3 valores únicos):**
- tcp: [X%]
- udp: [X%]
- icmp: [X%]

**service (70 valores únicos):**
- Servicios más frecuentes: [completar]
- Servicios raros (< 1%): [completar]

**flag (11 valores únicos):**
- SF (Normal): [X%]
- S0, REJ, RSTR: [porcentajes]

#### 3.2.6 Detección Inicial de Outliers

**[Completar después de ejecutar exploracion.scala]**

Método de detección: [IQR / Z-score / Otro]

| Atributo | Número de Outliers | Porcentaje | Rango de Outliers |
|----------|-------------------|------------|-------------------|
| [atributo1] | [X] | [X%] | [min - max] |

**Visualizaciones:**
- [Referencia a gráficos de caja (boxplots) en carpeta imagenes/]

---

### 3.3 Análisis de Correlaciones

#### 3.3.1 Matriz de Correlación entre Atributos Numéricos

**[Insertar imagen de la matriz de correlación generada]**

![Matriz de Correlación](imagenes/matriz_correlacion.png)

**Correlaciones Fuertes Identificadas (|r| > 0.7):**

**[Completar con resultados del análisis]**

| Par de Atributos | Coeficiente de Correlación | Interpretación |
|------------------|---------------------------|----------------|
| [attr1] - [attr2] | [r] | [positiva/negativa, posible redundancia] |
| ... | ... | ... |

**Observaciones:**
- [Identificar pares de atributos altamente correlacionados]
- [Discutir posible redundancia y candidatos para eliminación]
- [Analizar correlaciones desde el punto de vista del dominio]

#### 3.3.2 Correlación con la Variable Objetivo

**[Completar con análisis de importancia de features]**

Análisis de la relación entre cada atributo y la clase objetivo:

**Atributos con Alta Correlación con la Clase:**
1. [atributo1]: [descripción de la relación]
2. [atributo2]: [descripción de la relación]
3. ...

**Atributos con Baja Correlación:**
- [lista de atributos poco informativos]

#### 3.3.3 Análisis de Varianza por Clase

**[Completar con ANOVA o test similar]**

Identificación de atributos que muestran diferencias significativas entre clases:

| Atributo | F-statistic | p-value | Conclusión |
|----------|-------------|---------|------------|
| [attr1] | [X] | [X] | [significativo/no significativo] |

#### 3.3.4 Visualizaciones de Relaciones

**[Insertar gráficos relevantes]**

- Gráfico de dispersión: [atributo_x] vs [atributo_y] coloreado por clase
- Histogramas por clase para atributos clave
- Gráficos de densidad

![Visualización de Relaciones](imagenes/scatter_plot_ejemplo.png)

---

## 4. Preparación de Datos

### 4.1 Creación de Conjuntos de Entrenamiento y Prueba

#### 4.1.1 Estrategia de Partición

**Decisión:** Hemos optado por una partición **70% entrenamiento / 30% prueba**.

**Justificación:**
- El dataset es suficientemente grande (4.9M registros) para que el 30% de prueba (~1.47M registros) sea representativo
- El 70% de entrenamiento (~3.43M registros) proporciona datos suficientes para el aprendizaje
- Esta proporción es estándar en problemas con grandes volúmenes de datos

#### 4.1.2 Estratificación

**Método:** Stratified Random Split

Dada la alta desbalanceación de clases, es crucial mantener la misma distribución de clases en ambos conjuntos:

| Clase | Entrenamiento (70%) | Prueba (30%) | Total |
|-------|-------------------|--------------|-------|
| normal | [X] ([Y%]) | [X] ([Y%]) | [Total] (19.69%) |
| DoS | [X] ([Y%]) | [X] ([Y%]) | [Total] (79.24%) |
| Probe | [X] ([Y%]) | [X] ([Y%]) | [Total] (0.83%) |
| R2L | [X] ([Y%]) | [X] ([Y%]) | [Total] (0.23%) |
| U2R | [X] ([Y%]) | [X] ([Y%]) | [Total] (0.01%) |

**Semilla aleatoria:** [Especificar para reproducibilidad]

#### 4.1.3 Verificación de la Partición

**[Completar después de ejecutar procesamiento.scala]**

```
Tamaño conjunto de entrenamiento: [X] registros ([Y] MB)
Tamaño conjunto de prueba: [X] registros ([Y] MB)
Verificación de distribución: [OK/FAILED]
```

#### 4.1.4 Restricciones Aplicadas

Siguiendo las recomendaciones del enunciado:
1. ✓ Partición realizada **después** de eliminar registros vacíos/no válidos
2. ✓ Partición realizada **antes** de cualquier otra transformación
3. ✓ Conjunto de prueba nunca utilizado para ajustar parámetros o crear el modelo
4. ✓ Todas las transformaciones aplicadas al conjunto de entrenamiento se replican en el de prueba

---

### 4.2 Limpieza de Datos

#### 4.2.1 Eliminación de Registros Vacíos o No Válidos

**Criterios de Validación:**
- Registro completo (41 atributos + 1 label)
- Ningún campo completamente vacío
- Tipos de datos correctos

**Resultados:**
- Registros inválidos encontrados: [X]
- Porcentaje de registros inválidos: [Y%]
- Registros válidos restantes: [Z]

#### 4.2.2 Tratamiento de Valores Ausentes

**Estrategia adoptada:**

| Tipo de Atributo | Estrategia | Justificación |
|------------------|-----------|---------------|
| Numéricos continuos | [Imputación por media/mediana/eliminación] | [razón] |
| Numéricos discretos | [Imputación por moda/eliminación] | [razón] |
| Categóricos | [Categoría "unknown"/moda/eliminación] | [razón] |

**Aplicación en conjunto de prueba:**
- Valores imputados utilizando estadísticas del conjunto de **entrenamiento**
- **NTAIE (Número Total de Ausentes/Inconsistentes Eliminados):** [X] registros ([Y%] del conjunto de prueba)

#### 4.2.3 Detección y Tratamiento de Inconsistencias

**Inconsistencias detectadas:**
1. [Tipo de inconsistencia 1]: [cantidad] registros
   - Ejemplo: [descripción]
   - Acción: [corrección/eliminación]

2. [Tipo de inconsistencia 2]: [cantidad] registros
   - Ejemplo: [descripción]
   - Acción: [corrección/eliminación]

#### 4.2.4 Tratamiento de Outliers

**Método de Detección:** [IQR con factor 1.5 / Z-score con umbral 3 / Otro]

**Decisiones por Atributo:**

| Atributo | Outliers Detectados | Acción | Justificación |
|----------|-------------------|---------|---------------|
| duration | [X] | [Mantener/Winsorizar/Eliminar] | [razón desde perspectiva del dominio] |
| src_bytes | [X] | [acción] | [razón] |
| ... | ... | ... | ... |

**Importante:** En el contexto de ciberseguridad, muchos "outliers" pueden ser realmente ataques legítimos. Se debe tener precaución al eliminarlos.

**Aplicación en conjunto de prueba:**
- **NTOE (Número Total de Outliers Eliminados):** [X] registros ([Y%] del conjunto de prueba)

#### 4.2.5 Tratamiento de Ruido en los Datos

**Estrategias aplicadas:**
- [Suavizado / Binning / Regresión / Clustering]
- [Descripción de la aplicación específica]

#### 4.2.6 Métricas de Calidad Post-Limpieza

**Conjunto de Entrenamiento:**
- Registros originales: [X]
- Registros después de limpieza: [Y]
- Tasa de retención: [Z%]

**Conjunto de Prueba:**
- Registros originales: [X]
- NTAIE: [A]
- NTOE: [B]
- Total eliminado: [A+B] ([C%])
- **Tasa de no clasificados: [C%]** (debe ser << tasa de error esperada)

---

### 4.3 Selección de Atributos

#### 4.3.1 Motivación para la Selección de Atributos

Objetivos de la selección de atributos:
1. Reducir la dimensionalidad y complejidad computacional
2. Mejorar el rendimiento del modelo
3. Reducir el riesgo de overfitting
4. Mejorar la interpretabilidad del modelo
5. Eliminar atributos redundantes o irrelevantes

#### 4.3.2 Atributos Eliminados

**Basado en Correlación Alta (multicolinealidad):**

| Par de Atributos Correlacionados | Correlación | Atributo Eliminado | Atributo Retenido |
|--------------------------------|-------------|-------------------|------------------|
| [attr1] - [attr2] | [r] | [attr1/attr2] | [attr1/attr2] |

**Basado en Baja Varianza o Importancia:**

| Atributo | Razón de Eliminación | Estadística |
|----------|---------------------|-------------|
| [attr] | [razón] | [valor] |

**Basado en Dominio del Problema:**

Atributos que no aportan valor predictivo en el contexto de detección de intrusiones:
- [lista]

#### 4.3.3 Nuevos Atributos Creados (Feature Engineering)

**[Si aplica]**

| Nuevo Atributo | Fórmula/Construcción | Justificación |
|----------------|---------------------|---------------|
| [nombre] | [fórmula] | [por qué es útil] |

Ejemplo hipotético:
- `bytes_ratio = src_bytes / (dst_bytes + 1)`: Ratio de tráfico que puede indicar ciertos tipos de ataque

#### 4.3.4 Conjunto Final de Atributos

**Atributos seleccionados para el modelado:** [X] atributos

**Lista de atributos finales:**
1. [atributo1] - [tipo]
2. [atributo2] - [tipo]
...

---

### 4.4 Transformación de Datos

#### 4.4.1 Codificación de Variables Categóricas

**Estrategia de codificación:**

| Atributo Categórico | Número de Categorías | Método de Codificación | Justificación |
|---------------------|---------------------|----------------------|---------------|
| protocol_type | 3 | One-Hot Encoding | Pocas categorías, sin orden |
| service | 70 | StringIndexer + OneHotEncoder | Muchas categorías |
| flag | 11 | One-Hot Encoding | Sin orden natural |
| [otros binarios] | 2 | Encoding binario (0/1) | Variables booleanas |

**Detalles de implementación en Spark:**
- `StringIndexer`: Para convertir strings a índices numéricos
- `OneHotEncoder`: Para crear variables dummy
- `VectorAssembler`: Para combinar todas las features en un vector

#### 4.4.2 Normalización/Estandarización de Atributos Numéricos

**Método seleccionado:** [StandardScaler / MinMaxScaler / Normalizer]

**Justificación:** [Por qué se eligió este método]

**Atributos transformados:**

| Atributo Original | Escala Original | Escala Transformada | Método |
|-------------------|----------------|---------------------|---------|
| duration | [min - max] | [min - max] | [método] |
| src_bytes | [min - max] | [min - max] | [método] |
| ... | ... | ... | ... |

**Importante:** Los parámetros de escalado (media, desviación, min, max) se calculan **solo** en el conjunto de entrenamiento y se aplican al conjunto de prueba.

#### 4.4.3 Tratamiento de la Variable Objetivo

**Transformación de labels:**

| Etiqueta Original (String) | Categoría | Índice Numérico |
|---------------------------|-----------|----------------|
| normal | normal | 0 |
| back, land, neptune, pod, smurf, teardrop, ... | DoS | 1 |
| ipsweep, nmap, portsweep, satan | Probe | 2 |
| ftp_write, guess_passwd, imap, ... | R2L | 3 |
| buffer_overflow, loadmodule, perl, rootkit | U2R | 4 |

**Nota:** El dataset original contiene 23 tipos específicos de ataque que se mapean a las 4 categorías principales.

#### 4.4.4 Creación de DataFrames Finales para Spark ML

**Estructura del DataFrame de entrenamiento:**

```
root
 |-- features: vector (contains [feature1, feature2, ..., featureN])
 |-- label: double (0.0, 1.0, 2.0, 3.0, 4.0)
 |-- [otros metadatos opcionales]
```

**Número de features en el vector:** [X] (después de one-hot encoding)

**Schema del DataFrame:**
- [Describir la estructura completa]

#### 4.4.5 Validación de Transformaciones

**Checks realizados:**
- ✓ No hay valores NaN en el vector de features
- ✓ Todas las labels están en el rango [0, 4]
- ✓ Dimensión del vector de features es consistente en todos los registros
- ✓ Distribución de clases preservada después de transformaciones

#### 4.4.6 Almacenamiento de Datos Transformados

**Formato de almacenamiento:** [Parquet / CSV / Otro]

**Ubicación:**
- Conjunto de entrenamiento: `datos/processed/train.parquet`
- Conjunto de prueba: `datos/processed/test.parquet`

**Ventajas del formato elegido:**
- [Razones de la elección]

**Tamaño de archivos:**
- Entrenamiento: [X] MB/GB
- Prueba: [X] MB/GB

---

## 5. Conclusiones

### 5.1 Resumen del Trabajo Realizado

En esta primera etapa del proyecto, hemos completado exitosamente:

1. **Resumen Ejecutivo:** Documentación completa del dataset KDD Cup 1999, incluyendo origen, propósito, tamaño y características.

2. **Exploración de Datos:** 
   - Análisis exhaustivo de los 41 atributos predictores
   - Identificación de la distribución de clases (fuertemente desbalanceada)
   - Análisis de correlaciones entre atributos
   - Detección de outliers y valores ausentes

3. **Preparación de Datos:**
   - Creación de conjuntos de entrenamiento (70%) y prueba (30%) con estratificación
   - Limpieza de datos: eliminación de registros inválidos y tratamiento de outliers
   - Selección de atributos: [X] atributos eliminados, [Y] atributos retenidos
   - Transformación: codificación de categóricos, normalización y creación de DataFrames para Spark ML

### 5.2 Principales Hallazgos

**Características Relevantes del Dataset:**
1. [Hallazgo 1 sobre la naturaleza de los datos]
2. [Hallazgo 2 sobre patrones interesantes]
3. [Hallazgo 3 sobre desafíos identificados]

**Desafíos Identificados:**
- **Desbalanceo extremo:** U2R representa solo el 0.01% de los datos
- [Otros desafíos específicos encontrados]

**Atributos Más Relevantes:**
- [Lista de atributos que muestran mayor poder predictivo]

### 5.3 Desafíos Encontrados

Durante el desarrollo de esta etapa, enfrentamos los siguientes desafíos:

1. **[Desafío técnico 1]:**
   - Descripción del problema
   - Solución implementada

2. **[Desafío de datos]:**
   - Descripción del problema
   - Solución implementada

3. **[Desafío computacional]:**
   - Descripción del problema
   - Solución implementada

### 5.4 Próximos Pasos

Para la siguiente etapa del proyecto (entregable individual), se procederá a:

1. **Construcción del Modelo Base:**
   - Entrenar un árbol de decisión utilizando Spark ML
   - Evaluar el rendimiento en el conjunto de prueba

2. **Análisis de Resultados:**
   - Calcular métricas: accuracy, precisión, recall, F1-score por clase
   - Analizar la matriz de confusión
   - Considerar las métricas NTAIE y NTOE en la evaluación

3. **Mejoras Potenciales:**
   - Técnicas para manejar el desbalanceo de clases (SMOTE, undersampling, class weights)
   - Ajuste de hiperparámetros del árbol de decisión
   - Evaluación de otros clasificadores (Random Forest, Gradient Boosting)

### 5.5 Lecciones Aprendidas

- [Lección 1 sobre el uso de Spark]
- [Lección 2 sobre preprocesamiento de datos]
- [Lección 3 sobre trabajo en equipo]

---

## 6. Distribución del Trabajo

### Participación de los Integrantes

| Integrante | Porcentaje | Horas Dedicadas | Tareas Realizadas |
|------------|------------|-----------------|-------------------|
| [Nombre Estudiante 1] | [33-34%] | [X] horas | [Lista de tareas específicas] |
| [Nombre Estudiante 2] | [33-34%] | [X] horas | [Lista de tareas específicas] |
| [Nombre Estudiante 3] | [33-34%] | [X] horas | [Lista de tareas específicas] |
| **TOTAL** | **100%** | **[X]** horas | |

### Detalle de Tareas por Integrante

**[Nombre Estudiante 1]:**
- Investigación y documentación del dataset
- Desarrollo del script de exploración inicial
- Análisis estadístico descriptivo
- Redacción de secciones 1, 2 y 3.1 del informe

**[Nombre Estudiante 2]:**
- Desarrollo del script de limpieza de datos
- Análisis de correlaciones y visualizaciones
- Selección de atributos
- Redacción de secciones 3.2, 3.3 y 4.2 del informe

**[Nombre Estudiante 3]:**
- Desarrollo del script de transformación de datos
- Creación de conjuntos de entrenamiento y prueba
- Integración de scripts y testing
- Redacción de secciones 4.1, 4.3, 4.4 y conclusiones

### Metodología de Trabajo

- **Reuniones:** [X] reuniones de [Y] horas cada una
- **Herramientas de colaboración:** [Git, Slack, Google Docs, etc.]
- **División de trabajo:** [Metodología utilizada]

---

## 7. Referencias Bibliográficas

[1] Stolfo, S. J., Fan, W., Lee, W., Prodromidis, A., & Chan, P. K. (2000). Cost-based modeling for fraud and intrusion detection: Results from the JAM project. Proceedings DARPA Information Survivability Conference and Exposition. DISCEX'00.

[2] KDD Cup 1999 Data. UCI Machine Learning Repository. Disponible en: https://archive.ics.uci.edu/ml/datasets/KDD+Cup+1999+Data

[3] Tavallaee, M., Bagheri, E., Lu, W., & Ghorbani, A. A. (2009). A detailed analysis of the KDD CUP 99 data set. IEEE Symposium on Computational Intelligence for Security and Defense Applications.

[4] Apache Spark MLlib Documentation. Disponible en: https://spark.apache.org/docs/latest/ml-guide.html

[5] Karau, H., Konwinski, A., Wendell, P., & Zaharia, M. (2015). Learning Spark: Lightning-Fast Big Data Analysis. O'Reilly Media.

[6] Dhanabal, L., & Shantharajah, S. P. (2015). A study on NSL-KDD dataset for intrusion detection system based on classification algorithms. International Journal of Advanced Research in Computer and Communication Engineering, 4(6), 446-452.

[7] Aggarwal, P., & Sharma, S. K. (2015). Analysis of KDD dataset attributes-class wise for intrusion detection. Procedia Computer Science, 57, 842-851.

[8] [Añadir otras referencias utilizadas durante la investigación]

---

## 8. Apéndices

### Apéndice A: Descripción Detallada de los 41 Atributos

Ver archivo separado: `atributos_kdd99.md`

### Apéndice B: Mapeo de Tipos de Ataque a Categorías

**Categoría DoS (Denial of Service):**
- back
- land
- neptune
- pod
- smurf
- teardrop

**Categoría Probe (Probing/Scanning):**
- ipsweep
- nmap
- portsweep
- satan

**Categoría R2L (Remote to Local):**
- ftp_write
- guess_passwd
- imap
- multihop
- phf
- spy
- warezclient
- warezmaster

**Categoría U2R (User to Root):**
- buffer_overflow
- loadmodule
- perl
- rootkit

### Apéndice C: Comandos para Descargar y Preparar los Datos

```bash
# Descargar el dataset completo
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data.gz

# Descomprimir
gunzip kddcup.data.gz

# Descargar el archivo con los nombres de las columnas
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.names

# Mover a la carpeta del proyecto
mv kddcup.data /home/usuario/Datos/proyecto-kdd-cup-1999/datos/raw/
```

### Apéndice D: Código de Ejemplo para Visualizaciones

Ver scripts en la carpeta `notebooks/` o código Python/R utilizado para generar gráficos.

---

**Fecha de entrega:** 19 de octubre de 2026, 09:00 h (CET)

**Nombre del archivo a entregar:** G[X]-E1PS.zip

**Contenido del archivo:**
- Esta memoria (convertida a PDF)
- Carpeta `scripts/` con todos los archivos .scala
- Archivo `atributos_kdd99.md`

---

*Documento preparado por el Grupo [X] para la asignatura "Técnicas Escalables de Análisis de Datos en Entornos Big Data: Clasificadores", Universidad [Nombre], 2026-2027.*
