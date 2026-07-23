# PinkyPuff — Instalación

Aplicación de escritorio Java + PostgreSQL. Se distribuye como un único archivo
`PinkyPuff.jar` que ya contiene todas sus dependencias.

## Contenido

| Archivo | Para qué sirve |
|---|---|
| `PinkyPuff.jar` | La aplicación completa (36 MB). Incluye Hibernate, el driver de PostgreSQL, JavaFX, FlatLaf y OpenPDF. |
| `instalar_bd.sql` | Crea el esquema, los índices, vistas, funciones y triggers en una base nueva. |
| `conexion.properties.ejemplo` | Plantilla de configuración de la conexión. |
| `PinkyPuff.sh` / `PinkyPuff.bat` | Lanzadores para no tener que escribir el comando. |

## Requisitos en la máquina destino

- **Java 21 o superior.** Comprobar con `java -version`.
  - Fedora/RHEL: `sudo dnf install java-21-openjdk`
  - Debian/Ubuntu: `sudo apt install openjdk-21-jre`
  - Windows: [Adoptium Temurin 21](https://adoptium.net/)
- **PostgreSQL 14 o superior**, accesible desde la máquina (no hace falta que sea
  la misma).

> **El JAR incluye las librerías nativas de Linux.** Se construyó en Linux, así
> que JavaFX y FlatLaf llevan dentro sus `.so`. Para distribuirlo a Windows o
> macOS hay que reconstruirlo en esa plataforma (`mvn clean package`), porque las
> dependencias de JavaFX se resuelven con un clasificador por sistema operativo.

## Paso 1 — Preparar la base de datos

Crear la base y ejecutar el script. Ajuste servidor, puerto y usuario:

```bash
createdb -h localhost -p 5433 -U admin pinkypuff
psql -h localhost -p 5433 -U admin -d pinkypuff -f instalar_bd.sql
```

El script es **idempotente**: se puede volver a ejecutar sin romper nada, y sirve
también para actualizar una base que ya existía.

> Necesita permiso para `CREATE EXTENSION` (normalmente superusuario). Si el
> usuario de la aplicación no lo tiene, pida al administrador que ejecute antes:
> ```sql
> CREATE EXTENSION IF NOT EXISTS pg_trgm;
> CREATE EXTENSION IF NOT EXISTS pgcrypto;
> ```

## Paso 2 — Crear el primer administrador

No hay usuarios por defecto. Crear uno con la contraseña ya cifrada en SHA-256,
que es el formato que espera la aplicación:

```sql
INSERT INTO administrador (email, username, password_hash)
VALUES ('admin@suempresa.com', 'admin', encode(sha256('SU_CONTRASEÑA'::bytea), 'hex'));
```

## Paso 3 — Configurar la conexión

Hay dos formas; la primera no requiere tocar archivos.

**Opción A — desde la propia aplicación.** Arranque sin más: al no encontrar
configuración, muestra un diálogo donde introducir servidor, puerto, base,
usuario y contraseña, con un botón para comprobarlo antes de guardar.

**Opción B — archivo preparado de antemano.** Útil para instalar en varios
equipos. Copie `conexion.properties.ejemplo` a:

- Linux/macOS: `~/.pinkypuff/conexion.properties`
- Windows: `C:\Users\SU_USUARIO\.pinkypuff\conexion.properties`

```properties
db.host=localhost
db.puerto=5433
db.base=pinkypuff
db.usuario=admin
db.password=su_contraseña
```

> **La contraseña se guarda en claro.** En Linux y macOS el archivo se crea con
> permisos `rw-------` (solo el dueño puede leerlo). En Windows conviene
> restringirlo a mano si el equipo es compartido.

Para usar otra ruta:

```bash
java -Dpinkypuff.config=/ruta/a/conexion.properties -jar PinkyPuff.jar
```

## Paso 4 — Ejecutar

```bash
java -jar PinkyPuff.jar
```

O con los lanzadores incluidos: `./PinkyPuff.sh` en Linux/macOS,
`PinkyPuff.bat` en Windows (doble clic).

### Acceso desde el menú de aplicaciones (Linux)

Cree `~/.local/share/applications/pinkypuff.desktop`:

```ini
[Desktop Entry]
Type=Application
Name=PinkyPuff
Exec=/ruta/completa/a/PinkyPuff.sh
Icon=applications-office
Terminal=false
Categories=Office;
```

Después: `update-desktop-database ~/.local/share/applications`

## Mantenimiento

Ejecutar de vez en cuando (semanalmente, o al cerrar el día). Actualiza las
estadísticas del planificador, refresca las vistas materializadas y purga las
bitácoras antiguas:

```sql
CALL sp_mantenimiento_bd();
```

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Connection refused` en el diálogo | PostgreSQL no está arrancado o el puerto no es el correcto. Compruebe con `pg_isready -h SERVIDOR -p PUERTO`. |
| `password authentication failed` | Usuario o contraseña incorrectos, o falta la regla en `pg_hba.conf` del servidor. |
| `function fn_evolucion_mensual(...) does not exist` | No se ejecutó `instalar_bd.sql` sobre esa base. |
| `Cuenta bloqueada por 3 intentos fallidos` | Es el bloqueo de seguridad, dura 15 minutos. Para liberarlo antes: `DELETE FROM sesiones_admin WHERE usuario_ingresado = 'admin' AND exito = FALSE;` |
| No abre nada y no da error | Falta Java o es anterior a la 21. Compruebe con `java -version`. |
| Los meses de los gráficos salen en inglés | No afecta: las etiquetas se generan en español sin depender de la configuración regional del servidor. |

## Reconstruir el JAR

Desde la carpeta del proyecto:

```bash
mvn clean package
```

Genera `target/PinkyPuff.jar`. Necesita conexión a internet la primera vez, para
descargar las dependencias.
