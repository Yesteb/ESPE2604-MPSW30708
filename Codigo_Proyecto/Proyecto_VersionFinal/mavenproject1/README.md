# PinkyPuff — Sistema de gestión de pedidos

Aplicación de escritorio Java Swing para registrar y gestionar pedidos de ropa y accesorios. Permite crear pedidos, clasificar productos por tipo/estilo/talla, generar reportes en PDF y administrar clientes.

---

## Requisitos de desarrollo

| Herramienta         | Versión requerida  | Notas                                        |
|---------------------|--------------------|----------------------------------------------|
| **JDK**             | 21 (LTS)           | `maven.compiler.release = 21`                |
| **Apache Maven**    | 3.9+               | Incluido en NetBeans; también válido standalone |
| **NetBeans IDE**    | 21 o superior      | Recomendado para el diseñador Swing (GUI)    |
| **PostgreSQL**      | 16 o superior      | Driver JDBC `42.7.7` en el `pom.xml`         |
| **Docker**          | 24 o superior      | Para levantar la base de datos localmente    |
| **Docker Compose**  | 2.x (plugin)       | Incluido en Docker Desktop / Docker Engine   |

---

## Dependencias Maven (gestionadas automáticamente)

```xml
hibernate-core   7.0.0.Final    <!-- ORM / JPA 3.2 -->
postgresql       42.7.7         <!-- Driver JDBC     -->
lombok           1.18.42        <!-- Getters/Setters  -->
openpdf          1.3.43         <!-- Generación PDF   -->
```

---

## Configuración de la base de datos

La aplicación espera PostgreSQL en **`localhost:5433`** (puerto no estándar para evitar conflictos con instalaciones locales).

### Opción A — Docker Compose (recomendado)

Crea el archivo `docker-compose.yml` en la raíz del proyecto:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: pinkypuff-db
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: contra
      POSTGRES_DB: postgres
    ports:
      - "5433:5432"
    volumes:
      - pinkypuff_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  pinkypuff_data:
```

Luego levanta el contenedor:

```bash
docker compose up -d
```

### Opción B — Docker sin Compose

```bash
docker run -d \
  --name pinkypuff-db \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=contra \
  -e POSTGRES_DB=postgres \
  -p 5433:5432 \
  postgres:16
```

### Verificar conexión

```bash
docker exec -it pinkypuff-db psql -U admin -d postgres
```

---

## Parámetros de conexión (`persistence.xml`)

```
URL:      jdbc:postgresql://localhost:5433/postgres
Usuario:  admin
Password: contra
```

El esquema se genera automáticamente al iniciar la aplicación (`schema-generation = update`).

---

## Configuración del proyecto en NetBeans

1. **Clonar / abrir** el proyecto como _Maven Project_ en NetBeans.
2. Verificar que NetBeans usa **JDK 21**: `Tools → Java Platforms`.
3. Asegurarse de que el plugin **Lombok** esté habilitado:
   - `Tools → Options → Editor → Hints` → activar procesamiento de anotaciones.
   - O instalar el plugin desde: `Tools → Plugins → Lombok`.
4. Hacer clic en **Clean and Build** para descargar dependencias Maven.

---

## Ejecutar la aplicación

### Desde NetBeans

Clase principal: `view.FrmLogin`

Botón **Run Project** o `F6`.

### Desde línea de comandos

```bash
mvn compile exec:java -Dexec.mainClass=view.FrmLogin
```

---

## Usuario de prueba

El archivo `utils/CrearUsuarioPrueba.java` crea un usuario administrador inicial.
Ejecutarlo una sola vez como clase principal:

```
Email:    admin@correo.es
Usuario:  admin
Password: contraxd
```

---

## Detener la base de datos

```bash
docker compose down          # detiene el contenedor
docker compose down -v       # detiene y elimina los datos
```

---

## Estructura del proyecto

```
src/main/java/
├── controller/     Lógica de presentación y controladores
├── model/          Entidades JPA (Pedido, Cliente, Producto, Usuario…)
├── persistencia/   Controladores JPA (CRUD por entidad)
├── service/        Servicios de negocio (PedidoService)
├── utils/          Utilidades (creación de usuario de prueba)
└── view/           Formularios Swing (FrmLogin, FrmDashboard…)

src/main/resources/
└── META-INF/
    └── persistence.xml   Configuración JPA / Hibernate
```
