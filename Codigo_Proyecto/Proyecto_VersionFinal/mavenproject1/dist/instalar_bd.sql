-- ============================================================
--  PinkyPuff - preparacion completa de la base de datos
-- ============================================================
--  Uso:
--     psql -h SERVIDOR -p PUERTO -U USUARIO -d BASE -f instalar_bd.sql
--
--  Requisitos:
--    * PostgreSQL 14 o superior.
--    * El usuario necesita permiso para CREATE EXTENSION (normalmente
--      superusuario). Si no lo tiene, pida al administrador que ejecute antes:
--         CREATE EXTENSION IF NOT EXISTS pg_trgm;
--         CREATE EXTENSION IF NOT EXISTS pgcrypto;
--
--  Es idempotente: puede ejecutarse varias veces sin efectos adversos.
-- ============================================================

-- ===== 1. Esquema base =====

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


-- ===== 2. Indices, vistas, funciones y triggers =====

BEGIN;

-- ============================================================
-- 2.1 Extensiones
-- ============================================================

-- Búsqueda por similitud de texto (autocompletado de clientes)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Estadísticas de consultas: imprescindible para medir el antes/después
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Hash de contraseñas dentro de la BD (opcional, ver sección 9.1)
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- 2.2 Correcciones al esquema base
-- ============================================================

-- IMPORTANTE: la base real está gestionada por Hibernate (hbm2ddl.auto=update),
-- así que NO coincide exactamente con schema.sql. Hibernate ya creó por su cuenta
-- la PK de producto_atributos y bautizó las claves foráneas con nombres generados
-- (fk546r6okax6dnf19davvh7rd6h, fkg90gpvucv02t90l89vuoqyupn…). Por eso este
-- bloque consulta el catálogo en vez de dar los nombres por supuestos: así es
-- idempotente y sirve tanto sobre una base creada con schema.sql como sobre la
-- que mantiene Hibernate.

-- ---- Clave primaria de producto_atributos ------------------------------
-- Sin ella se permiten atributos duplicados y no hay UPSERT posible.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'producto_atributos'::regclass AND contype = 'p'
    ) THEN
        -- Deduplicar antes: la PK fallaría con filas repetidas
        DELETE FROM producto_atributos a
        USING producto_atributos b
        WHERE a.ctid < b.ctid
          AND a.producto_id = b.producto_id
          AND a.categoria   = b.categoria;

        ALTER TABLE producto_atributos
            ADD CONSTRAINT pk_producto_atributos PRIMARY KEY (producto_id, categoria);
        RAISE NOTICE 'PK creada en producto_atributos';
    ELSE
        RAISE NOTICE 'producto_atributos ya tiene PK: se omite';
    END IF;
END $$;

-- ---- Borrado en cascada de las tablas hijas de productos ---------------
-- Localiza la FK existente por su DEFINICIÓN (no por su nombre), la elimina y
-- la recrea con ON DELETE CASCADE. Sin esto, borrar un producto falla porque
-- quedan atributos y etiquetas huérfanos apuntándolo.
DO $$
DECLARE
    r      RECORD;
    tabla  TEXT;
BEGIN
    FOREACH tabla IN ARRAY ARRAY['producto_atributos', 'producto_etiquetas'] LOOP
        FOR r IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = tabla::regclass
              AND contype  = 'f'
              AND confrelid = 'productos'::regclass
              AND confdeltype <> 'c'          -- 'c' = ya tiene ON DELETE CASCADE
        LOOP
            EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', tabla, r.conname);
            EXECUTE format(
                'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (producto_id) '
                'REFERENCES productos(id) ON DELETE CASCADE',
                tabla, 'fk_' || tabla || '_producto');
            RAISE NOTICE 'FK % de % recreada con ON DELETE CASCADE', r.conname, tabla;
        END LOOP;
    END LOOP;
END $$;

-- ---- Restricciones de dominio -----------------------------------------
-- PostgreSQL no admite ADD CONSTRAINT IF NOT EXISTS, de ahí el DO.
--
-- Los valores permitidos NO son inventados: salen de leer el código Java.
--   pedidos  → PedidoService.marcarCobrado()   'COBRADO'
--              PedidoService.marcarCancelado() 'CANCELADO'
--              PedidoService.reactivarPedido() 'PENDIENTE'
--   clientes → Cliente.estado por defecto      'ACTIVO'
--              ControladorDashboard filtros    'INACTIVO', 'BLOQUEADO'
-- Añadir un estado a la aplicación obliga a actualizar también este CHECK.
DO $$
DECLARE
    c RECORD;
BEGIN
    FOR c IN
        SELECT * FROM (VALUES
            ('pedidos',   'ck_pedidos_estado',    $q$estado IN ('PENDIENTE','COBRADO','CANCELADO')$q$),
            ('clientes',  'ck_clientes_estado',   $q$estado IN ('ACTIVO','INACTIVO','BLOQUEADO')$q$),
            ('productos', 'ck_productos_cantidad',$q$cantidad > 0$q$),
            ('productos', 'ck_productos_precio',  $q$precio >= 0$q$)
        ) AS t(tabla, nombre, expresion)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = c.nombre) THEN
            EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK (%s)',
                           c.tabla, c.nombre, c.expresion);
            RAISE NOTICE 'CHECK % creado', c.nombre;
        END IF;
    END LOOP;
END $$;

-- Una etiqueta no debe repetirse dentro de su categoría
CREATE UNIQUE INDEX IF NOT EXISTS uq_etiquetas_categoria_valor
    ON etiquetas_config (categoria, valor);


-- ============================================================
-- 3.1 Sesiones e intentos de login
-- ============================================================
-- Hoy ControladorFrmLogin cuenta los intentos fallidos en una variable de
-- instancia: se reinicia al cerrar la ventana y no sirve si hay varios equipos.
-- Persistirlos permite bloqueo real y auditoría de accesos.

CREATE TABLE IF NOT EXISTS sesiones_admin (
    id                  BIGSERIAL       PRIMARY KEY,
    email               VARCHAR(255),               -- NULL si el usuario no existe
    usuario_ingresado   VARCHAR(255)    NOT NULL,
    exito               BOOLEAN         NOT NULL,
    momento             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    origen              VARCHAR(120),               -- host / equipo
    cierre              TIMESTAMPTZ,
    CONSTRAINT fk_sesiones_admin FOREIGN KEY (email)
        REFERENCES administrador(email) ON DELETE SET NULL
);

COMMENT ON TABLE sesiones_admin IS
    'Bitácora de intentos de inicio de sesión y sesiones abiertas.';

-- ============================================================
-- 3.2 Resumen precalculado de pedidos
-- ============================================================
-- Reemplaza a Pedido.getTotal(), que hoy suma en Java recorriendo la colección
-- EAGER de productos. Esta tabla la mantienen triggers: siempre está al día.

CREATE TABLE IF NOT EXISTS pedido_resumen (
    pedido_id       VARCHAR(36)     PRIMARY KEY,
    total           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    unidades        INTEGER         NOT NULL DEFAULT 0,
    items           INTEGER         NOT NULL DEFAULT 0,
    actualizado     TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT fk_pedido_resumen FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id) ON DELETE CASCADE
);

COMMENT ON TABLE pedido_resumen IS
    'Totales denormalizados por pedido, mantenidos por trigger.';

-- ============================================================
-- 3.3 Auditoría de cambios de estado en pedidos
-- ============================================================

CREATE TABLE IF NOT EXISTS auditoria_pedidos (
    id              BIGSERIAL       PRIMARY KEY,
    pedido_id       VARCHAR(36)     NOT NULL,
    operacion       CHAR(1)         NOT NULL,   -- I = insert, U = update, D = delete
    estado_anterior VARCHAR(255),
    estado_nuevo    VARCHAR(255),
    total_momento   NUMERIC(12,2),
    usuario_bd      NAME            NOT NULL DEFAULT current_user,
    momento         TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE auditoria_pedidos IS
    'Historial inmutable de cambios de estado de pedidos.';


-- ============================================================
-- Antes de crear el índice UNIQUE, verificar que no haya duplicados:
--   SELECT username, COUNT(*) FROM administrador GROUP BY username HAVING COUNT(*) > 1;
-- ============================================================

-- Índice de cobertura (covering index) para el login.
--   * UNIQUE  → garantiza que no existan dos administradores con el mismo usuario
--   * INCLUDE → guarda email y hash EN el índice, así PostgreSQL resuelve el
--               login con un Index Only Scan: no toca la tabla en absoluto.
CREATE UNIQUE INDEX IF NOT EXISTS uq_administrador_username
    ON administrador (username)
    INCLUDE (email, password_hash);


-- Login insensible a mayúsculas (OPCIONAL).
-- Cambia la semántica: 'Admin' y 'admin' pasarían a ser el mismo usuario.
-- Solo si también se ajusta la consulta en Java a LOWER(u.nombre) = LOWER(:n).
-- CREATE UNIQUE INDEX uq_administrador_username_lower
--     ON administrador (LOWER(username));


-- buscarTodos(): ORDER BY fecha_registro DESC
-- NULLS LAST porque fecha_registro es nullable y así el índice sirve para el
-- ORDER BY tal cual lo emite Hibernate.
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha_registro
    ON pedidos (fecha_registro DESC NULLS LAST);

-- buscarPorEstado(): WHERE estado = ? ORDER BY fecha_registro DESC
-- Índice compuesto: filtra y ordena con un solo recorrido, sin paso de Sort.
CREATE INDEX IF NOT EXISTS idx_pedidos_estado_fecha
    ON pedidos (estado, fecha_registro DESC NULLS LAST);

-- buscarPendientePorCliente(): WHERE cliente_id = ? AND estado = 'PENDIENTE'
-- ÍNDICE PARCIAL: solo indexa las filas pendientes. Como los pedidos cobrados
-- se acumulan para siempre y los pendientes son pocos, este índice se mantiene
-- diminuto sin importar cuánto crezca la tabla.
CREATE INDEX IF NOT EXISTS idx_pedidos_pendientes_cliente
    ON pedidos (cliente_id, fecha_registro DESC)
    WHERE estado = 'PENDIENTE';

-- buscarCobradosHoy(): WHERE estado='COBRADO' AND fecha_cobro >= ? AND < ?
CREATE INDEX IF NOT EXISTS idx_pedidos_cobrados_fecha
    ON pedidos (fecha_cobro DESC)
    WHERE estado = 'COBRADO';

-- Toda FK usada en JOIN necesita índice: PostgreSQL NO lo crea solo.
CREATE INDEX IF NOT EXISTS idx_pedidos_cliente
    ON pedidos (cliente_id);


-- LEFT JOIN FETCH p.productos → WHERE pedido_id IN (...)
-- INCLUDE evita volver a la tabla para calcular totales.
CREATE INDEX IF NOT EXISTS idx_productos_pedido
    ON productos (pedido_id)
    INCLUDE (precio, cantidad);

-- Productos aún no asignados a un pedido (inventario libre)
CREATE INDEX IF NOT EXISTS idx_productos_sin_pedido
    ON productos (fecha_registro DESC)
    WHERE pedido_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_productos_administrador
    ON productos (administrador_email);

-- Agregaciones por etiqueta (EstadisticasService.conteoPorEtiqueta).
-- Índices parciales: no indexan los NULL, que son mayoría en categorías opcionales.
CREATE INDEX IF NOT EXISTS idx_productos_tipo   ON productos (tipo)   WHERE tipo   IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_productos_estilo ON productos (estilo) WHERE estilo IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_productos_talla  ON productos (talla)  WHERE talla  IS NOT NULL;


-- buscarTodos(): ORDER BY nombre
CREATE INDEX IF NOT EXISTS idx_clientes_nombre
    ON clientes (nombre);

-- buscarPorEstado(): WHERE estado = ? ORDER BY nombre
CREATE INDEX IF NOT EXISTS idx_clientes_estado_nombre
    ON clientes (estado, nombre);

-- buscarPorNombre(): WHERE LOWER(nombre) = LOWER(?)
-- ÍNDICE FUNCIONAL: sin él, LOWER() sobre la columna deshabilita cualquier
-- índice normal y obliga a un Seq Scan.
CREATE INDEX IF NOT EXISTS idx_clientes_nombre_lower
    ON clientes (LOWER(nombre));

-- BuscadorCliente: hoy filtra en memoria sobre la lista completa.
-- Un índice GIN de trigramas permite mover ese filtro a la BD:
--   SELECT * FROM clientes WHERE nombre ILIKE '%ana%' ORDER BY similarity(nombre,'ana') DESC;
CREATE INDEX IF NOT EXISTS idx_clientes_nombre_trgm
    ON clientes USING gin (nombre gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_clientes_fecha_registro
    ON clientes (fecha_registro DESC NULLS LAST);


-- buscarPorCategoria(): WHERE categoria = ? ORDER BY orden, id
-- Se invoca una vez por cada grupo de chips del dashboard. Índice compuesto que
-- cubre filtro + orden completo.
CREATE INDEX IF NOT EXISTS idx_etiquetas_categoria_orden
    ON etiquetas_config (categoria, orden, id)
    INCLUDE (valor, valor_numerico);

-- Lado inverso de la relación N:M (el PK ya cubre producto_id)
CREATE INDEX IF NOT EXISTS idx_producto_etiquetas_etiqueta
    ON producto_etiquetas (etiqueta_id);

-- Agregaciones por categoría personalizada
CREATE INDEX IF NOT EXISTS idx_producto_atributos_categoria
    ON producto_atributos (categoria, valor);


-- Conteo de intentos fallidos recientes por usuario
CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_momento
    ON sesiones_admin (usuario_ingresado, momento DESC)
    WHERE exito = FALSE;

CREATE INDEX IF NOT EXISTS idx_sesiones_email_momento
    ON sesiones_admin (email, momento DESC);

CREATE INDEX IF NOT EXISTS idx_auditoria_pedido
    ON auditoria_pedidos (pedido_id, momento DESC);


-- ============================================================
-- 5.1 Pedido con su total ya calculado
-- ============================================================
-- Elimina la necesidad de traer los productos solo para sumar (Pedido.getTotal()).

CREATE OR REPLACE VIEW v_pedidos_detalle AS
SELECT
    p.id,
    p.estado,
    p.fecha_registro,
    p.fecha_cobro,
    c.id            AS cliente_id,
    c.nombre        AS cliente_nombre,
    c.telefono      AS cliente_telefono,
    c.estado        AS cliente_estado,
    COALESCE(r.total, 0)    AS total,
    COALESCE(r.unidades, 0) AS unidades,
    COALESCE(r.items, 0)    AS items,
    -- Días transcurridos desde el registro: útil para resaltar pedidos viejos
    EXTRACT(DAY FROM (now() - p.fecha_registro))::INT AS dias_antiguedad
FROM pedidos p
JOIN clientes c        ON c.id = p.cliente_id
LEFT JOIN pedido_resumen r ON r.pedido_id = p.id;

COMMENT ON VIEW v_pedidos_detalle IS
    'Pedido + cliente + totales precalculados. Reemplaza el LEFT JOIN FETCH.';

-- ============================================================
-- 5.2 Producto con todas sus etiquetas resueltas
-- ============================================================
-- Los atributos dinámicos vienen como JSON en una sola columna, en lugar de
-- una fila por atributo (que es lo que provoca el @ElementCollection EAGER).

CREATE OR REPLACE VIEW v_productos_completo AS
SELECT
    pr.id,
    pr.tipo,
    pr.estilo,
    pr.talla,
    pr.descripcion,
    pr.precio,
    pr.cantidad,
    (pr.precio * pr.cantidad) AS subtotal,
    pr.fecha_registro,
    pr.pedido_id,
    pr.administrador_email,
    COALESCE(
        (SELECT jsonb_object_agg(pa.categoria, pa.valor)
         FROM producto_atributos pa
         WHERE pa.producto_id = pr.id),
        '{}'::jsonb
    ) AS atributos
FROM productos pr;

-- ============================================================
-- 5.3 Sesiones abiertas
-- ============================================================

CREATE OR REPLACE VIEW v_sesiones_activas AS
SELECT
    s.email,
    a.username,
    s.momento       AS inicio,
    s.origen,
    now() - s.momento AS duracion
FROM sesiones_admin s
JOIN administrador a ON a.email = s.email
WHERE s.exito = TRUE
  AND s.cierre IS NULL
  AND s.momento > now() - INTERVAL '12 hours'
ORDER BY s.momento DESC;

-- ============================================================
-- 5.4 Inventario libre (productos sin pedido asignado)
-- ============================================================

CREATE OR REPLACE VIEW v_inventario_libre AS
SELECT
    tipo,
    estilo,
    talla,
    COUNT(*)          AS referencias,
    SUM(cantidad)     AS unidades,
    SUM(precio * cantidad) AS valor_inventario,
    MIN(precio)       AS precio_min,
    MAX(precio)       AS precio_max
FROM productos
WHERE pedido_id IS NULL
GROUP BY tipo, estilo, talla;


-- ============================================================
-- 6.1 Panel de inicio: todos los KPIs en UNA fila
-- ============================================================
-- Hoy el dashboard hace, al arrancar: contarPorEstado('PENDIENTE'),
-- buscarCobradosHoy(), contarTotal() de clientes, buscarTodos() de pedidos…
-- Son 4+ viajes a la base de datos y varios recorridos completos de tabla.
-- Esto lo resuelve con un solo SELECT de una fila.

DROP MATERIALIZED VIEW IF EXISTS mv_dashboard_inicio CASCADE;

CREATE MATERIALIZED VIEW mv_dashboard_inicio AS
SELECT
    1::INT AS id,   -- fila única: necesaria para el índice UNIQUE del REFRESH CONCURRENTLY

    (SELECT COUNT(*) FROM pedidos WHERE estado = 'PENDIENTE')        AS pedidos_pendientes,
    (SELECT COUNT(*) FROM pedidos WHERE estado = 'COBRADO')          AS pedidos_cobrados,
    (SELECT COUNT(*) FROM pedidos
      WHERE estado = 'COBRADO'
        AND fecha_cobro >= date_trunc('day', now()))                 AS cobrados_hoy,

    (SELECT COALESCE(SUM(r.total), 0)
       FROM pedidos p JOIN pedido_resumen r ON r.pedido_id = p.id
      WHERE p.estado = 'COBRADO'
        AND p.fecha_cobro >= date_trunc('day', now()))               AS monto_hoy,

    (SELECT COALESCE(SUM(r.total), 0)
       FROM pedidos p JOIN pedido_resumen r ON r.pedido_id = p.id
      WHERE p.estado = 'COBRADO'
        AND p.fecha_cobro >= date_trunc('month', now()))             AS monto_mes,

    (SELECT COALESCE(SUM(r.total), 0)
       FROM pedidos p JOIN pedido_resumen r ON r.pedido_id = p.id
      WHERE p.estado = 'PENDIENTE')                                  AS monto_por_cobrar,

    -- Facturación histórica: la tarjeta "Total cobrado" del reporte
    (SELECT COALESCE(SUM(r.total), 0)
       FROM pedidos p JOIN pedido_resumen r ON r.pedido_id = p.id
      WHERE p.estado = 'COBRADO')                                    AS monto_cobrado_total,

    (SELECT COUNT(*) FROM clientes)                                  AS clientes_total,
    (SELECT COUNT(*) FROM clientes WHERE estado = 'ACTIVO')          AS clientes_activos,
    (SELECT COUNT(*) FROM productos)                                 AS productos_total,
    (SELECT COALESCE(SUM(cantidad), 0)
       FROM productos WHERE pedido_id IS NULL)                       AS unidades_libres,

    now() AS calculado_en
WITH DATA;

-- El índice UNIQUE es OBLIGATORIO para poder usar REFRESH ... CONCURRENTLY
CREATE UNIQUE INDEX uq_mv_dashboard_inicio ON mv_dashboard_inicio (id);

COMMENT ON MATERIALIZED VIEW mv_dashboard_inicio IS
    'Instantánea de KPIs que se lee al iniciar sesión. Refrescar con sp_refrescar_dashboard().';


-- ============================================================
-- 6.2 Ranking de productos del mes
-- ============================================================
-- Alimenta los gráficos del dashboard sin recalcular sobre toda la historia.

DROP MATERIALIZED VIEW IF EXISTS mv_top_productos_mes CASCADE;

CREATE MATERIALIZED VIEW mv_top_productos_mes AS
SELECT
    date_trunc('month', p.fecha_registro)::DATE AS mes,
    -- Los COALESCE van AQUÍ, no en el índice: REFRESH CONCURRENTLY exige un
    -- índice UNIQUE sobre columnas simples, no sobre expresiones.
    COALESCE(pr.tipo,   '(sin tipo)')   AS tipo,
    COALESCE(pr.estilo, '(sin estilo)') AS estilo,
    COALESCE(pr.talla,  '(sin talla)')  AS talla,
    SUM(pr.cantidad)              AS unidades,
    SUM(pr.precio * pr.cantidad)  AS monto,
    COUNT(DISTINCT p.id)          AS pedidos
FROM productos pr
JOIN pedidos p ON p.id = pr.pedido_id
WHERE p.fecha_registro >= date_trunc('month', now()) - INTERVAL '11 months'
GROUP BY 1, 2, 3, 4
WITH DATA;

CREATE UNIQUE INDEX uq_mv_top_productos_mes
    ON mv_top_productos_mes (mes, tipo, estilo, talla);

CREATE INDEX idx_mv_top_productos_unidades
    ON mv_top_productos_mes (mes, unidades DESC);


-- ============================================================
-- 6.3 Refresco
-- ============================================================
-- CONCURRENTLY: no bloquea a quien esté leyendo la vista. Es más lento, pero
-- la aplicación nunca se queda esperando.

-- Se elimina primero porque cambia el número de parámetros: con CREATE OR REPLACE
-- quedarían dos sobrecargas conviviendo.
DROP PROCEDURE IF EXISTS sp_refrescar_dashboard(BOOLEAN);

CREATE OR REPLACE PROCEDURE sp_refrescar_dashboard(
    p_concurrente   BOOLEAN DEFAULT TRUE,
    p_solo_si_sucio BOOLEAN DEFAULT FALSE   -- TRUE = no hacer nada si no hubo cambios
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sucio BOOLEAN;
BEGIN
    SELECT sucio INTO v_sucio FROM dashboard_estado WHERE id = 1;

    IF p_solo_si_sucio AND NOT COALESCE(v_sucio, TRUE) THEN
        RETURN;   -- nada cambió desde el último refresco
    END IF;

    IF p_concurrente THEN
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_dashboard_inicio;
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_top_productos_mes;
    ELSE
        REFRESH MATERIALIZED VIEW mv_dashboard_inicio;
        REFRESH MATERIALIZED VIEW mv_top_productos_mes;
    END IF;

    -- Cierra el ciclo que abren los triggers de la sección 8.5: sin este UPDATE
    -- la marca "sucio" nunca se limpiaría y el mecanismo no serviría de nada.
    UPDATE dashboard_estado SET sucio = FALSE, marcado_en = now() WHERE id = 1;
END;
$$;

-- Uso:
--   CALL sp_refrescar_dashboard();               -- refresca siempre
--   CALL sp_refrescar_dashboard(TRUE, TRUE);     -- solo si hubo cambios


-- Reemplaza EstadisticasService.conteoMensual() y montoMensual().
-- Devuelve TODOS los meses del rango, incluso los que no tienen pedidos
-- (generate_series), cosa que un GROUP BY normal no hace.

CREATE OR REPLACE FUNCTION fn_evolucion_mensual(
    p_meses  INT     DEFAULT 6,
    p_estado VARCHAR DEFAULT NULL      -- NULL = todos los estados
)
RETURNS TABLE (
    mes                 DATE,
    etiqueta            TEXT,
    pedidos             BIGINT,
    monto               NUMERIC,
    monto_mes_anterior  NUMERIC,
    variacion_pct       NUMERIC,
    monto_acumulado     NUMERIC,
    promedio_movil_3m   NUMERIC
)
LANGUAGE sql
STABLE
AS $$
WITH serie AS (
    -- Esqueleto de meses: garantiza que no falte ninguno en el gráfico
    SELECT generate_series(
        date_trunc('month', CURRENT_DATE) - make_interval(months => p_meses - 1),
        date_trunc('month', CURRENT_DATE),
        INTERVAL '1 month'
    )::DATE AS mes
),
datos AS (
    SELECT
        date_trunc('month', p.fecha_registro)::DATE AS mes,
        COUNT(*)                        AS pedidos,
        COALESCE(SUM(r.total), 0)       AS monto
    FROM pedidos p
    LEFT JOIN pedido_resumen r ON r.pedido_id = p.id
    WHERE p.fecha_registro >= date_trunc('month', CURRENT_DATE)
                              - make_interval(months => p_meses - 1)
      AND (p_estado IS NULL OR p.estado = p_estado)
    GROUP BY 1
),
completo AS (
    SELECT s.mes,
           COALESCE(d.pedidos, 0) AS pedidos,
           COALESCE(d.monto, 0)   AS monto
    FROM serie s
    LEFT JOIN datos d ON d.mes = s.mes
)
SELECT
    mes,
    -- Etiqueta "ene 26", idéntica a la que produce hoy EstadisticasService con
    -- DateTimeFormatter("MMM yy", es_ES). Se usa un array en vez de
    -- to_char(..., 'TMmon') para NO depender de la locale del servidor:
    -- la imagen Debian de PostgreSQL no trae es_ES generada y devolvería
    -- los meses en inglés.
    ((ARRAY['ene','feb','mar','abr','may','jun',
            'jul','ago','sep','oct','nov','dic'])[EXTRACT(MONTH FROM mes)]
     || ' ' || to_char(mes, 'YY'))::TEXT                    AS etiqueta,
    pedidos,
    monto,
    -- LAG: valor de la fila anterior en el orden de la ventana
    LAG(monto) OVER w                                       AS monto_mes_anterior,
    -- Variación porcentual mes contra mes
    CASE WHEN LAG(monto) OVER w > 0
         THEN ROUND((monto - LAG(monto) OVER w) * 100.0 / LAG(monto) OVER w, 2)
    END                                                     AS variacion_pct,
    -- Acumulado del periodo
    SUM(monto) OVER (w ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
                                                            AS monto_acumulado,
    -- Promedio móvil de 3 meses: suaviza el ruido de la serie
    ROUND(AVG(monto) OVER (w ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 2)
                                                            AS promedio_movil_3m
FROM completo
WINDOW w AS (ORDER BY mes)
ORDER BY mes;
$$;

-- Uso:
--   SELECT * FROM fn_evolucion_mensual(6, 'COBRADO');


-- Reemplaza EstadisticasService.conteoPorEtiqueta().
-- Funciona con las categorías fijas (TIPO/ESTILO/TALLA) y con las
-- personalizadas guardadas en producto_atributos.

CREATE OR REPLACE FUNCTION fn_top_etiquetas(
    p_categoria TEXT,
    p_estado    TEXT DEFAULT NULL,
    p_limite    INT  DEFAULT 10
)
RETURNS TABLE (
    valor           TEXT,
    unidades        BIGINT,
    posicion        BIGINT,
    porcentaje      NUMERIC,
    pareto_pct      NUMERIC
)
LANGUAGE sql
STABLE
AS $$
WITH base AS (
    SELECT
        CASE upper(p_categoria)
            WHEN 'TIPO'   THEN pr.tipo
            WHEN 'ESTILO' THEN pr.estilo
            WHEN 'TALLA'  THEN pr.talla
            ELSE (SELECT pa.valor
                    FROM producto_atributos pa
                   WHERE pa.producto_id = pr.id
                     AND pa.categoria   = p_categoria)
        END AS valor,
        pr.cantidad
    FROM productos pr
    JOIN pedidos p ON p.id = pr.pedido_id
    WHERE p_estado IS NULL OR p.estado = p_estado
),
agregado AS (
    SELECT valor::TEXT AS valor, SUM(cantidad)::BIGINT AS unidades
    FROM base
    WHERE valor IS NOT NULL AND valor <> ''
    GROUP BY valor
)
SELECT
    valor,
    unidades,
    -- RANK: posición en el ranking (empates comparten posición)
    RANK() OVER (ORDER BY unidades DESC)                            AS posicion,
    -- SUM(...) OVER () sin ORDER BY = total de TODO el conjunto
    ROUND(unidades * 100.0 / NULLIF(SUM(unidades) OVER (), 0), 2)   AS porcentaje,
    -- Acumulado: dónde se corta el 80% del volumen (análisis de Pareto)
    ROUND(
        SUM(unidades) OVER (ORDER BY unidades DESC
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
        * 100.0 / NULLIF(SUM(unidades) OVER (), 0), 2)              AS pareto_pct
FROM agregado
ORDER BY unidades DESC
LIMIT p_limite;
$$;

-- Uso:
--   SELECT * FROM fn_top_etiquetas('TIPO');
--   SELECT * FROM fn_top_etiquetas('ESTILO', 'COBRADO', 5);
--   SELECT * FROM fn_top_etiquetas('COLOR');   -- categoría personalizada


-- NTILE(4) divide a los clientes en cuartiles por facturación.
-- Sirve para decidir a quién atender primero sin inventar umbrales a mano.

CREATE OR REPLACE FUNCTION fn_ranking_clientes(p_limite INT DEFAULT 20)
RETURNS TABLE (
    cliente_id      VARCHAR,
    nombre          TEXT,
    pedidos         BIGINT,
    monto_total     NUMERIC,
    ticket_promedio NUMERIC,
    posicion        BIGINT,
    cuartil         INT,
    ultimo_pedido   TIMESTAMP,
    dias_inactivo   INT
)
LANGUAGE sql
STABLE
AS $$
WITH agregado AS (
    SELECT
        c.id                                AS cliente_id,
        c.nombre::TEXT                      AS nombre,
        COUNT(p.id)                         AS pedidos,
        COALESCE(SUM(r.total), 0)           AS monto_total,
        MAX(p.fecha_registro)               AS ultimo_pedido
    FROM clientes c
    LEFT JOIN pedidos p        ON p.cliente_id = c.id
    LEFT JOIN pedido_resumen r ON r.pedido_id  = p.id
    GROUP BY c.id, c.nombre
)
SELECT
    cliente_id,
    nombre,
    pedidos,
    monto_total,
    ROUND(monto_total / NULLIF(pedidos, 0), 2)          AS ticket_promedio,
    ROW_NUMBER() OVER (ORDER BY monto_total DESC)       AS posicion,
    NTILE(4)     OVER (ORDER BY monto_total DESC)::INT  AS cuartil,
    ultimo_pedido,
    EXTRACT(DAY FROM (now() - ultimo_pedido))::INT      AS dias_inactivo
FROM agregado
WHERE pedidos > 0
ORDER BY monto_total DESC
LIMIT p_limite;
$$;


-- FIRST_VALUE / LAST_VALUE / LAG en acción sobre la línea de tiempo de un cliente.

CREATE OR REPLACE FUNCTION fn_historial_cliente(p_cliente_id VARCHAR)
RETURNS TABLE (
    pedido_id           VARCHAR,
    fecha_registro      TIMESTAMP,
    estado              VARCHAR,
    total               NUMERIC,
    numero_pedido       BIGINT,
    total_acumulado     NUMERIC,
    dias_desde_anterior INT,
    primer_pedido       TIMESTAMP
)
LANGUAGE sql
STABLE
AS $$
SELECT
    p.id,
    p.fecha_registro,
    p.estado,
    COALESCE(r.total, 0),
    ROW_NUMBER() OVER w,
    SUM(COALESCE(r.total, 0)) OVER (w ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW),
    EXTRACT(DAY FROM (p.fecha_registro - LAG(p.fecha_registro) OVER w))::INT,
    FIRST_VALUE(p.fecha_registro) OVER w
FROM pedidos p
LEFT JOIN pedido_resumen r ON r.pedido_id = p.id
WHERE p.cliente_id = p_cliente_id
WINDOW w AS (ORDER BY p.fecha_registro)
ORDER BY p.fecha_registro DESC;
$$;


-- fecha_registro es nullable en el esquema y la aplicación puede olvidarla.
-- Este trigger la rellena cuando llega NULL.

CREATE OR REPLACE FUNCTION fn_trg_fecha_registro()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.fecha_registro IS NULL THEN
        NEW.fecha_registro := LOCALTIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_clientes_fecha  ON clientes;
CREATE TRIGGER trg_clientes_fecha
    BEFORE INSERT ON clientes
    FOR EACH ROW EXECUTE FUNCTION fn_trg_fecha_registro();

DROP TRIGGER IF EXISTS trg_pedidos_fecha   ON pedidos;
CREATE TRIGGER trg_pedidos_fecha
    BEFORE INSERT ON pedidos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_fecha_registro();

DROP TRIGGER IF EXISTS trg_productos_fecha ON productos;
CREATE TRIGGER trg_productos_fecha
    BEFORE INSERT ON productos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_fecha_registro();


-- Regla de negocio: fecha_cobro y estado no pueden contradecirse.
-- Hoy eso depende de que el código Java lo haga bien en cada sitio.
--
-- CUIDADO al endurecer este trigger: reabrir un pedido cobrado NO es un error,
-- es una operación deliberada de la aplicación (PedidoService.reactivarPedido()).
-- Una versión anterior de este documento lanzaba una excepción en la transición
-- COBRADO → PENDIENTE y dejaba ese botón inservible. Antes de prohibir una
-- transición, comprobar que ningún método del servicio la realiza.

CREATE OR REPLACE FUNCTION fn_trg_pedido_estado()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    -- Al pasar a COBRADO se sella la fecha de cobro
    IF NEW.estado = 'COBRADO' AND COALESCE(OLD.estado, '') <> 'COBRADO' THEN
        NEW.fecha_cobro := COALESCE(NEW.fecha_cobro, LOCALTIMESTAMP);
    END IF;

    -- Un pedido que no está cobrado no puede arrastrar fecha de cobro.
    -- Coincide con lo que ya hace PedidoService.reactivarPedido(), que pone
    -- setFechaCobro(null) a mano: el trigger lo garantiza aunque se olvide.
    IF NEW.estado IN ('PENDIENTE', 'CANCELADO') THEN
        NEW.fecha_cobro := NULL;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_pedido_estado ON pedidos;
CREATE TRIGGER trg_pedido_estado
    BEFORE INSERT OR UPDATE OF estado, fecha_cobro ON pedidos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_pedido_estado();


-- Este es el trigger que más rendimiento aporta: elimina la necesidad de traer
-- todos los productos de un pedido solo para sumar su total.

CREATE OR REPLACE FUNCTION fn_recalcular_resumen(p_pedido_id VARCHAR)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_pedido_id IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO pedido_resumen (pedido_id, total, unidades, items, actualizado)
    SELECT
        p.id,
        COALESCE(SUM(pr.precio * pr.cantidad), 0),
        COALESCE(SUM(pr.cantidad), 0),
        COUNT(pr.id),
        now()
    FROM pedidos p
    LEFT JOIN productos pr ON pr.pedido_id = p.id
    WHERE p.id = p_pedido_id
    GROUP BY p.id
    ON CONFLICT (pedido_id) DO UPDATE SET
        total       = EXCLUDED.total,
        unidades    = EXCLUDED.unidades,
        items       = EXCLUDED.items,
        actualizado = EXCLUDED.actualizado;
END;
$$;

-- Cambios en productos: hay que recalcular el pedido que pierde el producto
-- Y el que lo recibe (por eso se tratan OLD y NEW por separado).
CREATE OR REPLACE FUNCTION fn_trg_resumen_producto()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP IN ('UPDATE', 'DELETE') THEN
        PERFORM fn_recalcular_resumen(OLD.pedido_id);
    END IF;

    IF TG_OP IN ('INSERT', 'UPDATE') THEN
        -- Si el pedido no cambió, no recalcular dos veces
        IF TG_OP = 'INSERT' OR NEW.pedido_id IS DISTINCT FROM OLD.pedido_id THEN
            PERFORM fn_recalcular_resumen(NEW.pedido_id);
        ELSIF NEW.precio IS DISTINCT FROM OLD.precio
           OR NEW.cantidad IS DISTINCT FROM OLD.cantidad THEN
            PERFORM fn_recalcular_resumen(NEW.pedido_id);
        END IF;
    END IF;

    RETURN NULL;   -- AFTER trigger: el valor de retorno se ignora
END;
$$;

DROP TRIGGER IF EXISTS trg_resumen_producto ON productos;
CREATE TRIGGER trg_resumen_producto
    AFTER INSERT OR UPDATE OF precio, cantidad, pedido_id OR DELETE ON productos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_resumen_producto();

-- Todo pedido nuevo nace con su fila de resumen en cero
CREATE OR REPLACE FUNCTION fn_trg_resumen_pedido()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO pedido_resumen (pedido_id) VALUES (NEW.id)
    ON CONFLICT (pedido_id) DO NOTHING;
    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS trg_resumen_pedido ON pedidos;
CREATE TRIGGER trg_resumen_pedido
    AFTER INSERT ON pedidos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_resumen_pedido();

-- Carga inicial para los pedidos que ya existen
INSERT INTO pedido_resumen (pedido_id, total, unidades, items, actualizado)
SELECT
    p.id,
    COALESCE(SUM(pr.precio * pr.cantidad), 0),
    COALESCE(SUM(pr.cantidad), 0),
    COUNT(pr.id),
    now()
FROM pedidos p
LEFT JOIN productos pr ON pr.pedido_id = p.id
GROUP BY p.id
ON CONFLICT (pedido_id) DO UPDATE SET
    total    = EXCLUDED.total,
    unidades = EXCLUDED.unidades,
    items    = EXCLUDED.items;


CREATE OR REPLACE FUNCTION fn_trg_auditoria_pedidos()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_total NUMERIC(12,2);
BEGIN
    IF TG_OP = 'DELETE' THEN
        SELECT total INTO v_total FROM pedido_resumen WHERE pedido_id = OLD.id;
        INSERT INTO auditoria_pedidos (pedido_id, operacion, estado_anterior, total_momento)
        VALUES (OLD.id, 'D', OLD.estado, COALESCE(v_total, 0));
        RETURN OLD;
    END IF;

    -- En un INSERT la fila de pedido_resumen puede no existir todavía: PostgreSQL
    -- dispara los triggers AFTER en orden ALFABÉTICO y trg_auditoria_pedidos va
    -- antes que trg_resumen_pedido. Por eso el COALESCE (un pedido recién creado
    -- vale 0 de todos modos).
    SELECT total INTO v_total FROM pedido_resumen WHERE pedido_id = NEW.id;
    v_total := COALESCE(v_total, 0);

    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_pedidos (pedido_id, operacion, estado_nuevo, total_momento)
        VALUES (NEW.id, 'I', NEW.estado, v_total);
    ELSIF OLD.estado IS DISTINCT FROM NEW.estado THEN
        -- Solo se audita si el estado realmente cambió: evita ruido
        INSERT INTO auditoria_pedidos (pedido_id, operacion, estado_anterior, estado_nuevo, total_momento)
        VALUES (NEW.id, 'U', OLD.estado, NEW.estado, v_total);
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_auditoria_pedidos ON pedidos;
CREATE TRIGGER trg_auditoria_pedidos
    AFTER INSERT OR UPDATE OR DELETE ON pedidos
    FOR EACH ROW EXECUTE FUNCTION fn_trg_auditoria_pedidos();


-- No refresca la vista materializada dentro de la transacción (sería lentísimo):
-- solo deja una marca y emite un NOTIFY. La aplicación decide cuándo refrescar.

CREATE TABLE IF NOT EXISTS dashboard_estado (
    id          INT         PRIMARY KEY DEFAULT 1,
    sucio       BOOLEAN     NOT NULL DEFAULT TRUE,
    marcado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_dashboard_estado_fila_unica CHECK (id = 1)
);
INSERT INTO dashboard_estado (id) VALUES (1) ON CONFLICT DO NOTHING;

CREATE OR REPLACE FUNCTION fn_trg_marcar_dashboard()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE dashboard_estado
       SET sucio = TRUE, marcado_en = now()
     WHERE id = 1 AND sucio = FALSE;   -- evita escrituras innecesarias

    -- Canal de notificación: un cliente JDBC puede escucharlo con LISTEN
    PERFORM pg_notify('dashboard', TG_TABLE_NAME || ':' || TG_OP);
    RETURN NULL;
END;
$$;

-- FOR EACH STATEMENT: se dispara una vez por sentencia, no por fila.
-- Un INSERT de 500 productos genera 1 aviso, no 500.
DROP TRIGGER IF EXISTS trg_dashboard_pedidos ON pedidos;
CREATE TRIGGER trg_dashboard_pedidos
    AFTER INSERT OR UPDATE OR DELETE ON pedidos
    FOR EACH STATEMENT EXECUTE FUNCTION fn_trg_marcar_dashboard();

DROP TRIGGER IF EXISTS trg_dashboard_productos ON productos;
CREATE TRIGGER trg_dashboard_productos
    AFTER INSERT OR UPDATE OR DELETE ON productos
    FOR EACH STATEMENT EXECUTE FUNCTION fn_trg_marcar_dashboard();

DROP TRIGGER IF EXISTS trg_dashboard_clientes ON clientes;
CREATE TRIGGER trg_dashboard_clientes
    AFTER INSERT OR UPDATE OR DELETE ON clientes
    FOR EACH STATEMENT EXECUTE FUNCTION fn_trg_marcar_dashboard();


CREATE OR REPLACE FUNCTION fn_trg_normalizar_cliente()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.nombre := btrim(regexp_replace(NEW.nombre, '\s+', ' ', 'g'));

    IF NEW.nombre = '' THEN
        RAISE EXCEPTION 'El nombre del cliente no puede estar vacío'
            USING ERRCODE = 'check_violation';
    END IF;

    -- Teléfono: solo dígitos y el signo +
    IF NEW.telefono IS NOT NULL THEN
        NEW.telefono := regexp_replace(NEW.telefono, '[^0-9+]', '', 'g');
        IF NEW.telefono = '' THEN
            NEW.telefono := NULL;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_normalizar_cliente ON clientes;
CREATE TRIGGER trg_normalizar_cliente
    BEFORE INSERT OR UPDATE OF nombre, telefono ON clientes
    FOR EACH ROW EXECUTE FUNCTION fn_trg_normalizar_cliente();


-- Hoy el login hace: (1) SELECT del administrador, (2) comparación del hash en
-- Java, (3) el conteo de intentos vive en memoria y se pierde al cerrar.
-- Esto lo resuelve en UNA llamada, con bloqueo persistente por intentos fallidos.

CREATE OR REPLACE FUNCTION fn_autenticar_admin(
    p_usuario VARCHAR,
    p_hash    VARCHAR,                  -- SHA-256 hex, tal como lo calcula HashContrasena
    p_origen  VARCHAR DEFAULT NULL,
    p_max_intentos INT DEFAULT 3,
    p_ventana_min  INT DEFAULT 15
)
RETURNS TABLE (
    r_email       VARCHAR,
    r_username    VARCHAR,
    r_autenticado BOOLEAN,
    r_intentos    INT,
    r_bloqueado   BOOLEAN,
    r_mensaje     TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_admin     administrador%ROWTYPE;
    v_fallidos  INT     := 0;
    v_ok        BOOLEAN := FALSE;
    v_bloqueado BOOLEAN := FALSE;
BEGIN
    -- Intentos fallidos recientes: se apoya en idx_sesiones_usuario_momento
    SELECT COUNT(*)
      INTO v_fallidos
      FROM sesiones_admin
     WHERE usuario_ingresado = p_usuario
       AND exito = FALSE
       AND momento > now() - make_interval(mins => p_ventana_min);

    v_bloqueado := v_fallidos >= p_max_intentos;

    IF v_bloqueado THEN
        RETURN QUERY SELECT
            NULL::VARCHAR, NULL::VARCHAR, FALSE, v_fallidos, TRUE,
            format('Cuenta bloqueada por %s intentos fallidos. Reintente en %s minutos.',
                   v_fallidos, p_ventana_min)::TEXT;
        RETURN;
    END IF;

    -- Index Only Scan gracias a uq_administrador_username … INCLUDE (...)
    SELECT * INTO v_admin
      FROM administrador a
     WHERE a.username = p_usuario
        OR a.email    = p_usuario
     LIMIT 1;

    v_ok := FOUND AND v_admin.password_hash = p_hash;

    -- Toda tentativa queda registrada, exitosa o no
    INSERT INTO sesiones_admin (email, usuario_ingresado, exito, origen)
    VALUES (CASE WHEN FOUND THEN v_admin.email END, p_usuario, v_ok, p_origen);

    IF v_ok THEN
        RETURN QUERY SELECT
            v_admin.email, v_admin.username, TRUE, 0, FALSE, 'Acceso concedido'::TEXT;
    ELSE
        v_fallidos := v_fallidos + 1;
        RETURN QUERY SELECT
            NULL::VARCHAR, NULL::VARCHAR, FALSE, v_fallidos,
            v_fallidos >= p_max_intentos,
            format('Usuario o contraseña incorrectos. Le quedan %s intento(s).',
                   GREATEST(p_max_intentos - v_fallidos, 0))::TEXT;
    END IF;
END;
$$;

-- Uso:
--   SELECT * FROM fn_autenticar_admin('admin', '8c6976e5b5410415…', 'PC-CAJA-01');


-- Devuelve TODO lo que necesita la pantalla inicial en un único JSON.
-- Sustituye los 4-6 round-trips que hoy hace ControladorDashboard al arrancar.

CREATE OR REPLACE FUNCTION fn_dashboard_inicial(p_email VARCHAR DEFAULT NULL)
RETURNS JSONB
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT jsonb_build_object(
        'kpis', (
            SELECT to_jsonb(m) FROM mv_dashboard_inicio m WHERE m.id = 1
        ),
        'pedidos_pendientes', (
            SELECT COALESCE(jsonb_agg(t ORDER BY t.fecha_registro DESC), '[]'::jsonb)
            FROM (
                SELECT v.id, v.cliente_nombre, v.total, v.unidades,
                       v.fecha_registro, v.dias_antiguedad
                FROM v_pedidos_detalle v
                WHERE v.estado = 'PENDIENTE'
                ORDER BY v.fecha_registro DESC
                LIMIT 50
            ) t
        ),
        'cobrados_hoy', (
            SELECT COALESCE(jsonb_agg(t ORDER BY t.fecha_cobro DESC), '[]'::jsonb)
            FROM (
                SELECT v.id, v.cliente_nombre, v.total, v.fecha_cobro
                FROM v_pedidos_detalle v
                WHERE v.estado = 'COBRADO'
                  AND v.fecha_cobro >= date_trunc('day', now())
                ORDER BY v.fecha_cobro DESC
            ) t
        ),
        'etiquetas', (
            -- Todos los chips del dashboard en una sola pasada, en vez de
            -- una consulta por categoría
            SELECT COALESCE(jsonb_object_agg(categoria, valores), '{}'::jsonb)
            FROM (
                SELECT e.categoria,
                       jsonb_agg(jsonb_build_object(
                           'id', e.id, 'valor', e.valor,
                           'numerico', e.valor_numerico, 'orden', e.orden
                       ) ORDER BY e.orden, e.id) AS valores
                FROM etiquetas_config e
                GROUP BY e.categoria
            ) x
        ),
        'top_tipos',    (SELECT COALESCE(jsonb_agg(to_jsonb(t)), '[]'::jsonb)
                           FROM fn_top_etiquetas('TIPO', NULL, 5) t),
        'evolucion',    (SELECT COALESCE(jsonb_agg(to_jsonb(e)), '[]'::jsonb)
                           FROM fn_evolucion_mensual(6, NULL) e),
        'administrador', (
            SELECT jsonb_build_object('email', a.email, 'username', a.username)
            FROM administrador a WHERE a.email = p_email
        ),
        'dashboard_sucio', (SELECT sucio FROM dashboard_estado WHERE id = 1),
        'generado_en', now()
    ) INTO v_resultado;

    RETURN v_resultado;
END;
$$;

-- Uso:
--   SELECT jsonb_pretty(fn_dashboard_inicial('admin@pinkypuff.com'));


CREATE OR REPLACE PROCEDURE sp_cobrar_pedido(p_pedido_id VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
    v_items  INT;
BEGIN
    -- FOR UPDATE: bloquea la fila para que dos cajas no cobren el mismo pedido
    SELECT estado INTO v_estado
      FROM pedidos
     WHERE id = p_pedido_id
       FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El pedido % no existe', p_pedido_id
            USING ERRCODE = 'no_data_found';
    END IF;

    IF v_estado = 'COBRADO' THEN
        RAISE EXCEPTION 'El pedido % ya fue cobrado', p_pedido_id
            USING ERRCODE = 'check_violation';
    END IF;

    SELECT items INTO v_items FROM pedido_resumen WHERE pedido_id = p_pedido_id;
    IF COALESCE(v_items, 0) = 0 THEN
        RAISE EXCEPTION 'No se puede cobrar el pedido %: no tiene productos', p_pedido_id
            USING ERRCODE = 'check_violation';
    END IF;

    -- fecha_cobro la coloca trg_pedido_estado; la auditoría, trg_auditoria_pedidos
    UPDATE pedidos SET estado = 'COBRADO' WHERE id = p_pedido_id;
END;
$$;

-- Uso:  CALL sp_cobrar_pedido('a1b2c3d4-…');


-- Encapsula la lógica de PedidoService: buscar el pedido pendiente del cliente,
-- crearlo si no existe, insertar el producto y sus atributos. Todo atómico.

CREATE OR REPLACE FUNCTION fn_agregar_producto(
    p_cliente_id    VARCHAR,
    p_tipo          VARCHAR,
    p_estilo        VARCHAR,
    p_talla         VARCHAR,
    p_descripcion   VARCHAR,
    p_precio        NUMERIC,
    p_cantidad      INT,
    p_admin_email   VARCHAR,
    p_atributos     JSONB DEFAULT '{}'::jsonb
)
RETURNS TABLE (r_pedido_id VARCHAR, r_producto_id VARCHAR, r_total NUMERIC)
LANGUAGE plpgsql
AS $$
DECLARE
    v_pedido_id   VARCHAR(36);
    v_producto_id VARCHAR(36);
    v_total       NUMERIC(12,2);
BEGIN
    IF p_cantidad <= 0 THEN
        RAISE EXCEPTION 'La cantidad debe ser mayor que cero'
            USING ERRCODE = 'check_violation';
    END IF;

    -- ¿Ya hay un pedido pendiente? Usa idx_pedidos_pendientes_cliente
    SELECT id INTO v_pedido_id
      FROM pedidos
     WHERE cliente_id = p_cliente_id
       AND estado = 'PENDIENTE'
     ORDER BY fecha_registro DESC
     LIMIT 1
       FOR UPDATE;

    IF NOT FOUND THEN
        v_pedido_id := gen_random_uuid()::VARCHAR;
        INSERT INTO pedidos (id, cliente_id, estado, fecha_registro)
        VALUES (v_pedido_id, p_cliente_id, 'PENDIENTE', LOCALTIMESTAMP);
    END IF;

    v_producto_id := gen_random_uuid()::VARCHAR;
    INSERT INTO productos (id, tipo, estilo, talla, descripcion, precio,
                           cantidad, fecha_registro, administrador_email, pedido_id)
    VALUES (v_producto_id, p_tipo, p_estilo, p_talla, p_descripcion, p_precio,
            p_cantidad, LOCALTIMESTAMP, p_admin_email, v_pedido_id);

    -- Atributos personalizados desde el JSON, en una sola sentencia
    IF p_atributos IS NOT NULL AND p_atributos <> '{}'::jsonb THEN
        INSERT INTO producto_atributos (producto_id, categoria, valor)
        SELECT v_producto_id, clave, valor
          FROM jsonb_each_text(p_atributos) AS j(clave, valor)
         WHERE valor IS NOT NULL AND valor <> ''
        ON CONFLICT (producto_id, categoria) DO UPDATE
            SET valor = EXCLUDED.valor;
    END IF;

    -- trg_resumen_producto ya actualizó el total
    SELECT total INTO v_total FROM pedido_resumen WHERE pedido_id = v_pedido_id;

    RETURN QUERY SELECT v_pedido_id, v_producto_id, v_total;
END;
$$;

-- Uso:
--   SELECT * FROM fn_agregar_producto(
--       'cliente-uuid', 'Peluche', 'Clásico', 'M', 'Osito rosa',
--       12.50, 2, 'admin@pinkypuff.com', '{"COLOR":"Rosa"}'::jsonb);


-- Reemplaza el filtrado en memoria de BuscadorCliente.
-- Aprovecha idx_clientes_nombre_trgm (GIN de trigramas).

CREATE OR REPLACE FUNCTION fn_buscar_clientes(
    p_texto  TEXT,
    p_limite INT DEFAULT 20
)
RETURNS TABLE (
    id              VARCHAR,
    nombre          TEXT,
    telefono        VARCHAR,
    estado          VARCHAR,
    pedidos_pend    BIGINT,
    similitud       REAL
)
LANGUAGE sql
STABLE
AS $$
SELECT
    c.id,
    c.nombre::TEXT,
    c.telefono,
    c.estado,
    (SELECT COUNT(*) FROM pedidos p
      WHERE p.cliente_id = c.id AND p.estado = 'PENDIENTE'),
    similarity(c.nombre, p_texto) AS similitud
FROM clientes c
-- El filtro DEBE ser ILIKE: el índice GIN de trigramas lo resuelve con un
-- Bitmap Index Scan. Poner aquí similarity(...) > 0.25 sería un error: es una
-- llamada a función, no un operador indexable, y obliga a recorrer la tabla
-- entera calculando la similitud de cada fila (ver medición abajo).
WHERE c.nombre ILIKE '%' || p_texto || '%'
ORDER BY
    -- similarity() aquí sí es barato: solo se evalúa sobre las filas ya filtradas.
    -- Prioriza coincidencias por prefijo, luego por similitud.
    (c.nombre ILIKE p_texto || '%') DESC,
    similarity(c.nombre, p_texto) DESC,
    c.nombre
LIMIT p_limite;
$$;


CREATE OR REPLACE PROCEDURE sp_cerrar_sesion(p_email VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE sesiones_admin
       SET cierre = now()
     WHERE email = p_email
       AND exito = TRUE
       AND cierre IS NULL;
END;
$$;

-- Purga de bitácoras viejas: sin esto, sesiones_admin y auditoria_pedidos
-- crecen sin límite y empiezan a pesar en los backups.
CREATE OR REPLACE PROCEDURE sp_purgar_bitacoras(p_dias INT DEFAULT 90)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sesiones  INT;
    v_auditoria INT;
BEGIN
    DELETE FROM sesiones_admin
     WHERE momento < now() - make_interval(days => p_dias);
    GET DIAGNOSTICS v_sesiones = ROW_COUNT;

    DELETE FROM auditoria_pedidos
     WHERE momento < now() - make_interval(days => p_dias * 4);
    GET DIAGNOSTICS v_auditoria = ROW_COUNT;

    RAISE NOTICE 'Purgadas % sesiones y % filas de auditoría', v_sesiones, v_auditoria;
END;
$$;


CREATE OR REPLACE PROCEDURE sp_mantenimiento_bd()
LANGUAGE plpgsql
AS $$
BEGIN
    -- Estadísticas frescas: sin esto el planificador elige mal los índices
    ANALYZE administrador;
    ANALYZE clientes;
    ANALYZE pedidos;
    ANALYZE productos;
    ANALYZE etiquetas_config;
    ANALYZE producto_atributos;
    ANALYZE pedido_resumen;

    CALL sp_refrescar_dashboard(TRUE);
    CALL sp_purgar_bitacoras(90);

    RAISE NOTICE 'Mantenimiento completado: %', clock_timestamp();
END;
$$;

-- Ejecutar semanalmente, o al cerrar el día:
--   CALL sp_mantenimiento_bd();


SELECT r.pedido_id, r.total AS total_guardado, COALESCE(s.real, 0) AS total_real
FROM pedido_resumen r
LEFT JOIN (
    SELECT pedido_id, SUM(precio * cantidad) AS real
    FROM productos WHERE pedido_id IS NOT NULL
    GROUP BY pedido_id
) s ON s.pedido_id = r.pedido_id
WHERE r.total <> COALESCE(s.real, 0);

-- Reparación:
--   SELECT fn_recalcular_resumen(pedido_id) FROM pedidos;


ALTER TABLE pedidos   SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE productos SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE sesiones_admin SET (autovacuum_vacuum_scale_factor = 0.10);


COMMIT;
