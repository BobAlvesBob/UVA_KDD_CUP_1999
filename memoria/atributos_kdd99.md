# Descripción Detallada de los Atributos del Dataset KDD Cup 1999

## Resumen General

El dataset KDD Cup 1999 contiene **41 atributos predictores** más **1 atributo objetivo** (label), para un total de 42 columnas. Los atributos se clasifican en 4 categorías funcionales basadas en su propósito en la detección de intrusiones.

---

## Categorías de Atributos

### 1. Atributos Básicos de la Conexión (Basic Features)
### 2. Atributos de Contenido (Content Features)
### 3. Atributos de Tráfico Basados en Tiempo (Time-based Traffic Features)
### 4. Atributos de Tráfico Basados en Host (Host-based Traffic Features)

---

## 1. ATRIBUTOS BÁSICOS DE LA CONEXIÓN (9 atributos)

Estos atributos capturan información fundamental sobre cada conexión TCP individual.

### 1.1 duration
- **Tipo:** Numérico continuo (entero)
- **Unidad:** Segundos
- **Descripción:** Duración de la conexión en segundos. Valor 0 indica que la conexión no se completó.
- **Rango:** 0 a 58,329 segundos (aproximadamente 16 horas)
- **Relevancia:** Conexiones anormalmente largas o cortas pueden indicar ataques.

### 1.2 protocol_type
- **Tipo:** Categórico
- **Valores posibles:** `tcp`, `udp`, `icmp`
- **Descripción:** Protocolo de red utilizado en la conexión.
  - **tcp:** Transmission Control Protocol (orientado a conexión)
  - **udp:** User Datagram Protocol (sin conexión)
  - **icmp:** Internet Control Message Protocol (mensajes de control)
- **Relevancia:** Diferentes protocolos tienen patrones de ataque característicos.

### 1.3 service
- **Tipo:** Categórico
- **Valores posibles:** 70 servicios de red diferentes
- **Descripción:** Servicio de red en el destino (puerto de destino decodificado).
- **Servicios comunes:**
  - `http`, `ftp`, `smtp`, `telnet`, `ssh`, `domain` (DNS), `pop_3`, `imap4`
  - `private` (servicios en puertos no estándar)
  - Muchos otros servicios específicos
- **Relevancia:** Ciertos ataques se dirigen a servicios específicos.

### 1.4 flag
- **Tipo:** Categórico
- **Valores posibles:** 11 estados de conexión TCP
- **Descripción:** Estado de la conexión (normal o error).
- **Valores principales:**
  - `SF`: Normal establishment and termination (SYN-FIN)
  - `S0`: Connection attempt seen, no reply
  - `REJ`: Connection attempt rejected
  - `RSTR`: Connection reset
  - `RSTO`: Connection reset by originator
  - `S1`, `S2`, `S3`: Otros estados de error
  - `SH`: Connection SYN-ACK seen, but no data
  - `OTH`: Other states
- **Relevancia:** Flags anormales frecuentemente indican escaneos o ataques DoS.

### 1.5 src_bytes
- **Tipo:** Numérico continuo (entero)
- **Unidad:** Bytes
- **Descripción:** Número de bytes de datos transferidos desde el origen al destino.
- **Rango:** 0 a 1,379,963,888 bytes
- **Relevancia:** Volúmenes inusuales pueden indicar ataques de transferencia de datos o DoS.

### 1.6 dst_bytes
- **Tipo:** Numérico continuo (entero)
- **Unidad:** Bytes
- **Descripción:** Número de bytes de datos transferidos desde el destino al origen.
- **Rango:** 0 a 1,309,937,401 bytes
- **Relevancia:** Asimetría en transferencia puede indicar ciertos tipos de ataque.

### 1.7 land
- **Tipo:** Categórico binario (0/1)
- **Descripción:** 1 si la conexión es desde/hacia la misma dirección IP y puerto; 0 en caso contrario.
- **Relevancia:** Ataque "land" conocido que causa DoS en sistemas vulnerables.

### 1.8 wrong_fragment
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de fragmentos IP "incorrectos" en la conexión.
- **Rango:** Típicamente 0, valores altos indican anomalías
- **Relevancia:** Fragmentación anormal puede indicar ataques de evasión de IDS.

### 1.9 urgent
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de paquetes "urgentes" (con flag URG activo).
- **Rango:** Típicamente 0, raramente > 0
- **Relevancia:** Paquetes urgentes inusuales pueden indicar intentos de exploits.

---

## 2. ATRIBUTOS DE CONTENIDO (13 atributos)

Estos atributos analizan el contenido de la conexión y requieren conocimiento del dominio de aplicación.

### 2.1 hot
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de "indicadores calientes" (hot indicators) - acceso a archivos/directorios del sistema críticos.
- **Ejemplos:** Acceso a `/etc/passwd`, `/bin`, archivos de configuración
- **Relevancia:** Accesos a recursos sensibles sugieren intentos de compromiso.

### 2.2 num_failed_logins
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de intentos de login fallidos.
- **Rango:** 0 a 5 (típicamente)
- **Relevancia:** Múltiples fallos sugieren ataques de fuerza bruta o adivinación de contraseñas.

### 2.3 logged_in
- **Tipo:** Categórico binario (0/1)
- **Descripción:** 1 si el login fue exitoso; 0 en caso contrario.
- **Relevancia:** Distingue entre intentos de ataque y accesos legítimos autenticados.

### 2.4 num_compromised
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de "condiciones comprometidas" detectadas.
- **Ejemplos:** Variables de entorno sospechosas, comandos maliciosos
- **Relevancia:** Indicador directo de posible compromiso del sistema.

### 2.5 root_shell
- **Tipo:** Categórico binario (0/1)
- **Descripción:** 1 si se obtuvo acceso a shell con privilegios de root; 0 en caso contrario.
- **Relevancia:** Indicador crítico de ataques U2R (User to Root) exitosos.

### 2.6 su_attempted
- **Tipo:** Numérico discreto (0, 1, 2)
- **Descripción:** Número de comandos "su" (switch user) ejecutados.
- **Valores:** 0 = ninguno, 1 = uno, 2 = más de uno
- **Relevancia:** Intentos de escalada de privilegios.

### 2.7 num_root
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de accesos con privilegios de root.
- **Relevancia:** Accesos root frecuentes pueden indicar compromiso o abuso de privilegios.

### 2.8 num_file_creations
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de operaciones de creación de archivos.
- **Relevancia:** Creación inusual de archivos puede indicar instalación de backdoors o malware.

### 2.9 num_shells
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de shells (command prompts) invocados.
- **Relevancia:** Múltiples shells pueden indicar intentos de acceso interactivo no autorizado.

### 2.10 num_access_files
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de operaciones en archivos de control de acceso (e.g., `/etc/passwd`, `/etc/shadow`).
- **Relevancia:** Accesos a archivos de autenticación sugieren intentos de compromiso.

### 2.11 num_outbound_cmds
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de comandos FTP outbound en una sesión FTP.
- **Nota:** Este atributo es siempre 0 en el dataset KDD99 (no se capturó información).
- **Relevancia:** Teóricamente útil para detectar uso indebido de FTP.

### 2.12 is_host_login
- **Tipo:** Categórico binario (0/1)
- **Descripción:** 1 si el login es para un servicio "host" (e.g., telnet, rlogin); 0 en caso contrario.
- **Relevancia:** Distingue tipos de acceso que tienen diferentes perfiles de riesgo.

### 2.13 is_guest_login
- **Tipo:** Categórico binario (0/1)
- **Descripción:** 1 si el login es de tipo "guest" (usuario invitado); 0 en caso contrario.
- **Relevancia:** Cuentas guest pueden ser vectores de ataque si están mal configuradas.

---

## 3. ATRIBUTOS DE TRÁFICO BASADOS EN TIEMPO (9 atributos)

Estos atributos se calculan usando una **ventana de tiempo de 2 segundos** y capturan estadísticas sobre conexiones recientes al mismo destino.

### 3.1 count
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de conexiones al mismo host destino en los últimos 2 segundos.
- **Rango:** 1 a 511
- **Relevancia:** Volumen alto puede indicar escaneos o ataques DoS.

### 3.2 srv_count
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de conexiones al mismo servicio en los últimos 2 segundos.
- **Rango:** 1 a 511
- **Relevancia:** Múltiples conexiones al mismo servicio pueden indicar ataques dirigidos.

### 3.3 serror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "SYN" entre las últimas conexiones al mismo host (basado en `count`).
- **Rango:** 0.0 a 1.0
- **Cálculo:** (conexiones con flag S0, S1, S2, S3) / count
- **Relevancia:** Tasa alta indica escaneos SYN o ataques DoS.

### 3.4 srv_serror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "SYN" entre las últimas conexiones al mismo servicio (basado en `srv_count`).
- **Rango:** 0.0 a 1.0
- **Relevancia:** Similar a `serror_rate`, pero específico del servicio.

### 3.5 rerror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "REJ" entre las últimas conexiones al mismo host.
- **Rango:** 0.0 a 1.0
- **Cálculo:** (conexiones con flag REJ) / count
- **Relevancia:** Tasa alta indica conexiones rechazadas, posiblemente escaneos.

### 3.6 srv_rerror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "REJ" entre las últimas conexiones al mismo servicio.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Similar a `rerror_rate`, pero específico del servicio.

### 3.7 same_srv_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones al mismo servicio entre las últimas conexiones al mismo host.
- **Rango:** 0.0 a 1.0
- **Cálculo:** (conexiones al mismo servicio) / count
- **Relevancia:** Conexiones normales tienden a usar el mismo servicio; variación puede indicar exploración.

### 3.8 diff_srv_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones a servicios diferentes entre las últimas conexiones al mismo host.
- **Rango:** 0.0 a 1.0
- **Cálculo:** (conexiones a servicios diferentes) / count
- **Relevancia:** Tasa alta sugiere escaneo de múltiples servicios (probing).

### 3.9 srv_diff_host_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones a diferentes hosts entre las últimas conexiones al mismo servicio.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Puede indicar propagación de ataques a múltiples hosts.

---

## 4. ATRIBUTOS DE TRÁFICO BASADOS EN HOST (10 atributos)

Estos atributos se calculan usando una **ventana de las últimas 100 conexiones** al mismo host destino.

### 4.1 dst_host_count
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de conexiones que han tenido el mismo host destino en las últimas 100 conexiones.
- **Rango:** 0 a 255
- **Relevancia:** Patrón de acceso al host destino.

### 4.2 dst_host_srv_count
- **Tipo:** Numérico discreto (entero)
- **Descripción:** Número de conexiones que han tenido el mismo servicio en el host destino en las últimas 100 conexiones.
- **Rango:** 0 a 255
- **Relevancia:** Frecuencia de acceso a un servicio específico.

### 4.3 dst_host_same_srv_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones al mismo servicio entre las conexiones al mismo host destino.
- **Rango:** 0.0 a 1.0
- **Cálculo:** dst_host_srv_count / dst_host_count
- **Relevancia:** Consistencia en el uso de servicios.

### 4.4 dst_host_diff_srv_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones a diferentes servicios entre las conexiones al mismo host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Diversidad de servicios accedidos (exploración vs. uso normal).

### 4.5 dst_host_same_src_port_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con el mismo puerto origen entre las conexiones al mismo host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Uso repetido del mismo puerto origen puede ser inusual.

### 4.6 dst_host_srv_diff_host_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones a diferentes hosts origen para el mismo servicio en el host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Múltiples orígenes accediendo al mismo servicio pueden indicar ataque distribuido.

### 4.7 dst_host_serror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "SYN" entre las conexiones al mismo host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Tasa de errores SYN dirigidos a un host específico.

### 4.8 dst_host_srv_serror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "SYN" para el mismo servicio en el host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Errores SYN específicos de un servicio en el host.

### 4.9 dst_host_rerror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "REJ" entre las conexiones al mismo host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Tasa de rechazos dirigidos a un host específico.

### 4.10 dst_host_srv_rerror_rate
- **Tipo:** Numérico continuo (proporción)
- **Descripción:** Porcentaje de conexiones con errores "REJ" para el mismo servicio en el host destino.
- **Rango:** 0.0 a 1.0
- **Relevancia:** Rechazos específicos de un servicio en el host.

---

## 5. ATRIBUTO OBJETIVO (1 atributo)

### 5.1 attack_type (label)
- **Tipo:** Categórico
- **Descripción:** Tipo de ataque o "normal" para tráfico legítimo.
- **Valores en el dataset original:** 23 tipos específicos de ataque + "normal"

**Categorías principales (5 clases):**

1. **normal** - Tráfico de red normal y legítimo

2. **DoS (Denial of Service)** - Ataques de denegación de servicio
   - `back`: SYN flooding ataque
   - `land`: Land ataque (mismo IP origen/destino)
   - `neptune`: SYN flood
   - `pod`: Ping of death
   - `smurf`: Smurf DDoS ataque
   - `teardrop`: Teardrop ataque (fragmentos IP malformados)

3. **Probe (Probing/Scanning)** - Exploración de la red
   - `ipsweep`: Escaneo de IPs
   - `nmap`: Escaneo con herramienta nmap
   - `portsweep`: Escaneo de puertos
   - `satan`: Escaneo con herramienta SATAN

4. **R2L (Remote to Local)** - Acceso no autorizado desde remoto
   - `ftp_write`: Escritura no autorizada vía FTP
   - `guess_passwd`: Adivinación de contraseña
   - `imap`: Ataque a servicio IMAP
   - `multihop`: Acceso a través de múltiples saltos
   - `phf`: Ataque PHF
   - `spy`: Spyware
   - `warezclient`: Cliente warez ilegal
   - `warezmaster`: Servidor warez ilegal

5. **U2R (User to Root)** - Escalada de privilegios
   - `buffer_overflow`: Desbordamiento de buffer
   - `loadmodule`: Carga de módulo no autorizado
   - `perl`: Explotación vía perl
   - `rootkit`: Instalación de rootkit

---

## Resumen Estadístico por Tipo de Atributo

| Categoría | Cantidad | Tipos |
|-----------|----------|-------|
| **Categóricos** | 4 | protocol_type (3 valores), service (70 valores), flag (11 valores), + atributos binarios |
| **Binarios (0/1)** | 5 | land, logged_in, root_shell, is_host_login, is_guest_login |
| **Numéricos Continuos** | 9 | duration, src_bytes, dst_bytes, wrong_fragment, urgent + proporciones/rates |
| **Numéricos Discretos (conteos)** | 23 | hot, num_failed_logins, count, srv_count, dst_host_count, etc. |
| **Proporciones/Tasas (0.0-1.0)** | 13 | Todos los atributos terminados en "_rate" |
| **Total Predictores** | **41** | |
| **Label** | **1** | attack_type (5 categorías principales, 23 subtipos + normal) |

---

## Notas Importantes

1. **Atributo num_outbound_cmds:** Siempre tiene valor 0 en el dataset. Puede considerarse para eliminación durante la selección de atributos.

2. **Valores proporcionales:** Todos los atributos que terminan en "_rate" son proporciones en el rango [0.0, 1.0].

3. **Ventanas temporales:**
   - Atributos con prefijo "count" o que contienen "srv_": ventana de 2 segundos
   - Atributos con prefijo "dst_host_": ventana de últimas 100 conexiones

4. **Interdependencias:** Algunos atributos están matemáticamente relacionados:
   - `same_srv_rate` + `diff_srv_rate` no necesariamente = 1.0 (pueden existir otras categorías)
   - Los atributos "_rate" se calculan basándose en los atributos "count" correspondientes

5. **Atributos sensibles al orden temporal:** Los atributos de las categorías 3 y 4 dependen del orden en que aparecen las conexiones en el dataset. Si se reordena el dataset, estos valores cambiarían.

---

## Referencias

- KDD Cup 1999 Task Description: http://kdd.ics.uci.edu/databases/kddcup99/task.html
- Atribute Information: http://kdd.ics.uci.edu/databases/kddcup99/kddcup.names

---

*Este documento sirve como referencia completa para entender cada atributo del dataset KDD Cup 1999 y su relevancia para la detección de intrusiones en redes.*
