-- =========================================================
-- Script mejorado - Sistema Informático Yogurín
-- Motor objetivo: MySQL 8+
-- =========================================================

DROP DATABASE IF EXISTS yogurin_bustamantedb;
CREATE DATABASE yogurin_bustamantedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yogurin_bustamantedb;

-- =========================================================
-- TABLA: usuarios
-- Seguridad: contraseña en hash SHA-256 + salt
-- =========================================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL,
    clave CHAR(64) NOT NULL,
    salt CHAR(32) NOT NULL,
    rol ENUM('Administrador', 'Vendedor') NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_usuario UNIQUE (usuario)
);

-- =========================================================
-- TABLA: productos
-- =========================================================
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_productos_precio CHECK (precio >= 0),
    CONSTRAINT chk_productos_stock CHECK (stock >= 0)
);

-- =========================================================
-- TABLA: insumos
-- =========================================================
CREATE TABLE insumos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_insumos_stock_actual CHECK (stock_actual >= 0),
    CONSTRAINT chk_insumos_stock_minimo CHECK (stock_minimo >= 0)
);

-- =========================================================
-- TABLA: clientes
-- =========================================================
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(8),
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(150),
    correo VARCHAR(100),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_clientes_dni UNIQUE (dni),
    CONSTRAINT chk_clientes_dni CHECK (dni IS NULL OR dni REGEXP '^[0-9]{8}$')
);

-- =========================================================
-- TABLA: produccion
-- =========================================================
CREATE TABLE produccion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    lote VARCHAR(50) NOT NULL,
    sabor VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,
    observacion VARCHAR(200),
    CONSTRAINT chk_produccion_cantidad CHECK (cantidad > 0)
);

-- =========================================================
-- TABLA: ventas
-- =========================================================
CREATE TABLE ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cliente_id INT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_pagar DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_ventas_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_ventas_total CHECK (total >= 0),
    CONSTRAINT chk_ventas_descuento CHECK (descuento >= 0),
    CONSTRAINT chk_ventas_total_pagar CHECK (total_pagar >= 0)
);

-- =========================================================
-- TABLA: detalle_venta
-- =========================================================
CREATE TABLE detalle_venta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta_venta
        FOREIGN KEY (venta_id) REFERENCES ventas(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_producto
        FOREIGN KEY (producto_id) REFERENCES productos(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal CHECK (subtotal >= 0)
);

-- =========================================================
-- TABLA: movimientos_inventario
-- =========================================================
CREATE TABLE movimientos_inventario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    producto_id INT NULL,
    cantidad INT NOT NULL,
    observacion VARCHAR(200),
    CONSTRAINT fk_movimientos_producto
        FOREIGN KEY (producto_id) REFERENCES productos(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_movimientos_cantidad CHECK (cantidad > 0)
);

-- =========================================================
-- TABLA: cierre_caja
-- =========================================================
CREATE TABLE cierre_caja (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    total_ventas DECIMAL(10,2) NOT NULL DEFAULT 0,
    observacion VARCHAR(200),
    CONSTRAINT uq_cierre_fecha UNIQUE (fecha),
    CONSTRAINT chk_cierre_total_ventas CHECK (total_ventas >= 0)
);

-- =========================================================
-- TABLA: auditoria
-- =========================================================
CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tabla_afectada VARCHAR(50) NOT NULL,
    accion ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    registro_id BIGINT,
    descripcion VARCHAR(300) NOT NULL,
    usuario_bd VARCHAR(100) NOT NULL DEFAULT CURRENT_USER(),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- ÍNDICES DE RENDIMIENTO
-- =========================================================
CREATE INDEX idx_productos_nombre_estado ON productos(nombre, estado);
CREATE INDEX idx_clientes_nombre ON clientes(nombre);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
CREATE INDEX idx_ventas_cliente_fecha ON ventas(cliente_id, fecha);
CREATE INDEX idx_detalle_venta_venta_producto ON detalle_venta(venta_id, producto_id);
CREATE INDEX idx_movimientos_producto_fecha ON movimientos_inventario(producto_id, fecha);

-- =========================================================
-- TRIGGERS DE AUDITORÍA
-- =========================================================
DELIMITER $$

CREATE TRIGGER trg_productos_after_update
AFTER UPDATE ON productos
FOR EACH ROW
BEGIN
    INSERT INTO auditoria(tabla_afectada, accion, registro_id, descripcion)
    VALUES ('productos', 'UPDATE', NEW.id, CONCAT('Stock/Precio actualizado para producto: ', NEW.nombre));
END$$

CREATE TRIGGER trg_ventas_after_insert
AFTER INSERT ON ventas
FOR EACH ROW
BEGIN
    INSERT INTO auditoria(tabla_afectada, accion, registro_id, descripcion)
    VALUES ('ventas', 'INSERT', NEW.id, CONCAT('Nueva venta registrada. Total pagar: ', NEW.total_pagar));
END$$

CREATE TRIGGER trg_usuarios_after_update
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
    INSERT INTO auditoria(tabla_afectada, accion, registro_id, descripcion)
    VALUES ('usuarios', 'UPDATE', NEW.id, CONCAT('Actualización de usuario: ', NEW.usuario));
END$$

DELIMITER ;

-- =========================================================
-- DATOS DE PRUEBA
-- Usuario admin: clave original = 1234
-- hash = SHA2(CONCAT(salt,'1234'),256)
-- =========================================================
INSERT INTO usuarios(usuario, clave, salt, rol, estado)
VALUES ('admin', SHA2(CONCAT('a1b2c3d4e5f6a7b8', '1234'), 256), 'a1b2c3d4e5f6a7b8', 'Administrador', TRUE);

INSERT INTO clientes(dni, nombre, telefono, direccion, correo) VALUES
('12345678', 'Cliente General', '987654321', 'Lima', 'cliente.general@yogurin.com'),
('87654321', 'María Pérez', '999888777', 'San Miguel', 'maria.perez@yogurin.com');

INSERT INTO productos(nombre, descripcion, precio, stock, estado) VALUES
('Yogurt Fresa', 'Yogurt sabor fresa 1L', 8.50, 120, TRUE),
('Yogurt Vainilla', 'Yogurt sabor vainilla 1L', 8.00, 100, TRUE),
('Yogurt Durazno', 'Yogurt sabor durazno 1L', 9.00, 80, TRUE);

INSERT INTO insumos(nombre, unidad, stock_actual, stock_minimo) VALUES
('Leche Entera', 'L', 200.00, 80.00),
('Fresa Pulpa', 'kg', 35.00, 10.00),
('Azúcar', 'kg', 50.00, 20.00);
