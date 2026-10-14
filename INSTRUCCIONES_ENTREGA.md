# Instrucciones para la Entrega - Primera Etapa

## Fecha Límite
**19 de octubre de 2026 a las 09:00 h (CET)**

## Formato de Entrega

### Nombre del Archivo
`G[número]-E1PS.zip`

Donde `[número]` es el identificador de vuestro grupo según la Wiki.

**Ejemplo:** Si sois el grupo 3, el archivo debe llamarse `G3-E1PS.zip`

## Contenido del Archivo ZIP

El archivo debe contener:

```
G[X]-E1PS/
├── memoria.pdf                       # Informe convertido a PDF
├── scripts/
│   ├── procesamiento.scala           # Script principal
│   ├── exploracion.scala             # Script de exploración
│   ├── limpieza.scala                # Módulo de limpieza
│   └── transformacion.scala          # Módulo de transformación
└── [opcional] atributos_kdd99.md     # Si se referencia como apéndice
```

## Pasos para Preparar la Entrega

### 1. Completar el Informe

Editar `memoria/informe.md` y rellenar las secciones marcadas con `[X]` o `[completar]`:

- [ ] Nombres de los integrantes del grupo
- [ ] Número de grupo
- [ ] Resultados de la exploración (estadísticas, gráficos)
- [ ] Resultados de la limpieza (métricas NTAIE, NTOE)
- [ ] Distribución del trabajo (porcentaje, horas, tareas)
- [ ] Cualquier otro campo pendiente

### 2. Ejecutar los Scripts

**Importante:** Antes de entregar, ejecutar los scripts para:
- Verificar que funcionan correctamente
- Completar las secciones del informe con los resultados reales
- Generar visualizaciones para la carpeta `imagenes/`

```bash
# Descargar datos
cd datos/raw/
wget http://kdd.ics.uci.edu/databases/kddcup99/kddcup.data.gz
gunzip kddcup.data.gz
cd ../..

# Ejecutar exploración (para obtener estadísticas)
spark-shell --driver-memory 6g
:load scripts/exploracion.scala

# Ejecutar procesamiento completo
:load scripts/procesamiento.scala
```

### 3. Generar Visualizaciones

Crear gráficos y guardarlos en `memoria/imagenes/`:
- Matriz de correlación
- Distribución de clases
- Boxplots de outliers
- Histogramas de atributos principales

Pueden usar Python/R/Scala para generar estos gráficos.

### 4. Convertir el Informe a PDF

**Opción A: Usando Pandoc (Recomendado)**

```bash
cd memoria/

# Instalación de pandoc (si no está instalado)
# Ubuntu/Debian: sudo apt-get install pandoc
# macOS: brew install pandoc

# Conversión a PDF
pandoc informe.md -o memoria.pdf \
  --from markdown \
  --pdf-engine=xelatex \
  --toc \
  --number-sections \
  -V geometry:margin=2.5cm \
  -V fontsize=11pt \
  -V lang=es
```

**Opción B: Usando Markdown Preview en Editor**

- Abrir `informe.md` en VSCode/Typora/otro editor con soporte Markdown
- Exportar/Imprimir como PDF

**Opción C: Copiar a Word y Exportar**

- Copiar el contenido de `informe.md`
- Pegar en Word/LibreOffice
- Formatear títulos y secciones
- Exportar como PDF

### 5. Verificar el Código

**Checklist de verificación:**

- [ ] La variable `PATH` en `procesamiento.scala` está comentada/documentada para que los profesores la modifiquen
- [ ] Los scripts se pueden cargar con `:load` sin errores de sintaxis
- [ ] No hay rutas absolutas hardcodeadas (excepto PATH)
- [ ] Los comentarios están en español
- [ ] El código está bien indentado y legible

**Prueba final:**

```scala
// Probar que se puede cargar sin errores
spark-shell
:load scripts/procesamiento.scala
// Si carga sin errores de sintaxis, está OK
```

### 6. Crear el Archivo ZIP

**Opción A: Desde línea de comandos**

```bash
cd /home/usuario/Datos/

# Crear estructura temporal
mkdir G[X]-E1PS
cp proyecto-kdd-cup-1999/memoria/memoria.pdf G[X]-E1PS/
cp -r proyecto-kdd-cup-1999/scripts G[X]-E1PS/

# Opcional: incluir descripción de atributos
cp proyecto-kdd-cup-1999/memoria/atributos_kdd99.md G[X]-E1PS/

# Crear ZIP
zip -r G[X]-E1PS.zip G[X]-E1PS/

# Limpiar
rm -rf G[X]-E1PS/
```

**Opción B: Desde interfaz gráfica**

1. Crear carpeta `G[X]-E1PS`
2. Copiar `memoria.pdf` y carpeta `scripts/`
3. Hacer clic derecho → "Comprimir" o "Create archive"
4. Nombrar como `G[X]-E1PS.zip`

### 7. Verificar el ZIP

```bash
# Listar contenido
unzip -l G[X]-E1PS.zip

# Debe mostrar:
# G[X]-E1PS/memoria.pdf
# G[X]-E1PS/scripts/procesamiento.scala
# G[X]-E1PS/scripts/exploracion.scala
# G[X]-E1PS/scripts/limpieza.scala
# G[X]-E1PS/scripts/transformacion.scala

# Verificar tamaño (no debe ser excesivo)
ls -lh G[X]-E1PS.zip
```

### 8. Subir al Aula Virtual

1. Acceder al aula virtual de la asignatura
2. Buscar la tarea "Primera Etapa - Proyecto de Clasificación"
3. Subir el archivo `G[X]-E1PS.zip`
4. Verificar que la subida fue exitosa
5. **Importante:** Solo un miembro del grupo debe subir el archivo

## Checklist Final

Antes de entregar, verificar:

### Memoria (PDF)
- [ ] Portada con nombres de todos los integrantes
- [ ] Tabla de contenidos
- [ ] Resumen ejecutivo (máx. 500 palabras)
- [ ] Exploración de datos completa con resultados
- [ ] Preparación de datos documentada
- [ ] Conclusiones
- [ ] Distribución del trabajo (100% entre todos)
- [ ] Referencias bibliográficas
- [ ] Apéndices (si aplica)
- [ ] Formato profesional, sin errores ortográficos

### Scripts
- [ ] `procesamiento.scala` es el script principal
- [ ] Variable `PATH` claramente indicada para modificación
- [ ] Scripts se cargan con `:load` sin errores
- [ ] Comentarios en español
- [ ] Código bien organizado y legible
- [ ] Funciones documentadas

### General
- [ ] Nombre del archivo: `G[X]-E1PS.zip`
- [ ] Tamaño razonable (< 50 MB, idealmente < 10 MB)
- [ ] No incluir datos (solo código y memoria)
- [ ] Verificado que el ZIP se descomprime correctamente

## Criterios de Evaluación

Según el enunciado, se evaluará:

1. **Calidad de la memoria (50%)**
   - Completitud de las secciones
   - Claridad de la redacción
   - Profundidad del análisis
   - Visualizaciones informativas

2. **Calidad del software (50%)**
   - Código funcional y ejecutable
   - Buenas prácticas de programación
   - Comentarios y documentación
   - Cumplimiento de requisitos técnicos

## Consejos

### Para la Memoria
- **Sé conciso:** El resumen ejecutivo tiene un máximo de 500 palabras
- **Sé específico:** Incluye números, porcentajes, estadísticas concretas
- **Visualiza:** Una imagen vale más que mil palabras
- **Justifica:** Explica por qué tomaste cada decisión (limpieza, outliers, etc.)

### Para el Código
- **Testea:** Ejecuta los scripts antes de entregar
- **Comenta:** Explica las partes no obvias
- **Modulariza:** Usa funciones reutilizables
- **Sé consistente:** Estilo de código uniforme

### Gestión del Tiempo
- **No dejes para el último día:** La conversión a PDF y creación del ZIP pueden tener problemas
- **Prueba en limpio:** Ejecuta los scripts en una sesión nueva de Spark
- **Revisa en grupo:** Que todos lean la memoria antes de entregar

## Problemas Comunes

### "Mi memoria.pdf es muy grande"
- Comprime las imágenes antes de insertarlas
- Usa formato JPEG en lugar de PNG para fotos
- Limita la resolución a 150-300 DPI

### "Los scripts no cargan"
- Verifica errores de sintaxis con `:load`
- Asegúrate de que las rutas a otros scripts sean relativas
- Prueba en una instalación limpia de Spark

### "No puedo convertir a PDF"
- Usa un servicio online como markdown2pdf.com
- Copia a Google Docs y exporta como PDF
- Pide ayuda a un compañero con las herramientas instaladas

## Contacto

Si tienes problemas con la entrega:
1. Revisa primero esta guía
2. Consulta los README.md de cada carpeta
3. Pregunta en el foro del aula virtual
4. Contacta a los profesores con tiempo suficiente

## Después de la Entrega

- Conservar una copia del proyecto completo
- Los datos procesados (`datos/processed/`) serán necesarios para el entregable individual
- El código puede ser reutilizado y mejorado en entregas posteriores

---

**¡Buena suerte con la entrega!**

Fecha límite: **19 de octubre de 2026, 09:00 h (CET)**
