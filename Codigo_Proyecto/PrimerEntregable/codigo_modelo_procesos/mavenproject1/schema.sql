-- ============================================================
-- Base de datos: postgres
-- Motor: PostgreSQL
-- Puerto: 5433
-- Usuario: admin
-- ============================================================

-- Administrador
CREATE TABLE IF NOT EXISTS administrador (
    email           VARCHAR(255)    NOT NULL,
    username        VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    PRIMARY KEY (email)
);

-- Clientes
CREATE TABLE IF NOT EXISTS clientes (
    id              VARCHAR(36)     NOT NULL,
    nombre          VARCHAR(255)    NOT NULL,
    telefono        VARCHAR(255),
    descripcion     VARCHAR(500),
    fecha_registro  TIMESTAMP,
    estado          VARCHAR(255)    NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id)
);

-- Pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id              VARCHAR(36)     NOT NULL,
    cliente_id      VARCHAR(36)     NOT NULL,
    estado          VARCHAR(255)    NOT NULL,
    fecha_registro  TIMESTAMP,
    fecha_cobro     TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- Productos
CREATE TABLE IF NOT EXISTS productos (
    id                  VARCHAR(36)     NOT NULL,
    tipo                VARCHAR(255),
    estilo              VARCHAR(255),
    talla               VARCHAR(255),
    descripcion         VARCHAR(500),
    precio              NUMERIC(10,2)   NOT NULL,
    cantidad            INTEGER         NOT NULL,
    fecha_registro      TIMESTAMP,
    administrador_email VARCHAR(255),
    pedido_id           VARCHAR(36),
    PRIMARY KEY (id),
    FOREIGN KEY (administrador_email) REFERENCES administrador(email),
    FOREIGN KEY (pedido_id)           REFERENCES pedidos(id)
);

-- Etiquetas de configuracion (tipos, estilos, tallas, precios rapidos)
CREATE TABLE IF NOT EXISTS etiquetas_config (
    id              BIGSERIAL       NOT NULL,
    categoria       VARCHAR(255)    NOT NULL,
    valor           VARCHAR(255)    NOT NULL,
    valor_numerico  NUMERIC(10,2),
    orden           INTEGER         NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- Atributos personalizados de productos (categorias custom)
CREATE TABLE IF NOT EXISTS producto_atributos (
    producto_id     VARCHAR(36)     NOT NULL,
    categoria       VARCHAR(255)    NOT NULL,
    valor           VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Relacion muchos a muchos entre productos y etiquetas
CREATE TABLE IF NOT EXISTS producto_etiquetas (
    producto_id     VARCHAR(36)     NOT NULL,
    etiqueta_id     BIGINT          NOT NULL,
    PRIMARY KEY (producto_id, etiqueta_id),
    FOREIGN KEY (producto_id)   REFERENCES productos(id),
    FOREIGN KEY (etiqueta_id)   REFERENCES etiquetas_config(id)
);
