-- =====================================================================
-- Sistema Informatico de Gestion de Ventas y Produccion - Yogurin Bustamante
-- Migracion v2: alinea la base de datos con el Capitulo 3 del informe
--
-- Ejecutar UNA sola vez sobre la BD existente:
--   mysql -u root yogurin_bustamantedb < migracion_v2.sql
-- =====================================================================

USE yogurin_bustamantedb;

-- ---------------------------------------------------------------------
-- 1. Tabla intermedia produccion_insumo (relacion N:M del modelo logico)
--    Registra que insumos consumio cada lote de produccion y en que cantidad.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `produccion_insumo` (
  `id`             int(11)       NOT NULL AUTO_INCREMENT,
  `produccion_id`  int(11)       NOT NULL,
  `insumo_id`      int(11)       NOT NULL,
  `cantidad_usada` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pi_produccion` (`produccion_id`),
  KEY `idx_pi_insumo` (`insumo_id`),
  CONSTRAINT `produccion_insumo_ibfk_1` FOREIGN KEY (`produccion_id`)
      REFERENCES `produccion` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `produccion_insumo_ibfk_2` FOREIGN KEY (`insumo_id`)
      REFERENCES `insumos` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- 2. Stock minimo en productos, para las alertas de reabastecimiento (RF-18)
-- ---------------------------------------------------------------------
ALTER TABLE `productos`
  ADD COLUMN `stock_minimo` int(11) NOT NULL DEFAULT 10 AFTER `stock`;

-- ---------------------------------------------------------------------
-- 3. DNI unico en clientes (declarado en el punto 3.2.4 del informe)
--    Primero los DNI vacios pasan a NULL: MySQL permite varios NULL en un UNIQUE,
--    pero no permitiria dos cadenas vacias iguales.
-- ---------------------------------------------------------------------
UPDATE `clientes` SET `dni` = NULL WHERE `dni` = '';

ALTER TABLE `clientes`
  ADD CONSTRAINT `uk_clientes_dni` UNIQUE (`dni`);

-- ---------------------------------------------------------------------
-- 4. Migracion de contrasenas a SHA-256
--    Las claves estaban guardadas en texto plano. Se reemplazan por su hash.
--    Las credenciales de acceso NO cambian, solo su forma de almacenamiento.
-- ---------------------------------------------------------------------

-- Elimina el usuario basura con usuario y clave vacios
DELETE FROM `usuarios` WHERE `usuario` = '';

-- admin / 12345
UPDATE `usuarios`
   SET `clave` = '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5'
 WHERE `usuario` = 'admin';

-- diego / 1234
UPDATE `usuarios`
   SET `clave` = '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'
 WHERE `usuario` = 'diego';

-- HagiBF / 1234
UPDATE `usuarios`
   SET `clave` = '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'
 WHERE `usuario` = 'HagiBF';

-- Jack / 12345
UPDATE `usuarios`
   SET `clave` = '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5'
 WHERE `usuario` = 'Jack';

-- ---------------------------------------------------------------------
-- 5. Datos de prueba de insumos (RF-07), la tabla estaba vacia
-- ---------------------------------------------------------------------
INSERT INTO `insumos` (`nombre`, `unidad`, `stock_actual`, `stock_minimo`) VALUES
  ('Leche fresca',    'Litro',  120.00, 30.00),
  ('Azucar rubia',    'Kg',      45.00, 15.00),
  ('Cultivo lactico', 'Sobre',   20.00,  8.00),
  ('Envase 1L',       'Unidad', 200.00, 50.00),
  ('Envase 500ml',    'Unidad', 150.00, 50.00),
  ('Pulpa de fresa',  'Kg',      18.00, 10.00);

-- ---------------------------------------------------------------------
-- 6. Verificacion
-- ---------------------------------------------------------------------
-- El largo del hash debe ser 64 en todas las filas
SELECT `usuario`, LENGTH(`clave`) AS largo_hash, `rol` FROM `usuarios`;

-- Debe listar 11 tablas
SELECT COUNT(*) AS total_tablas
  FROM information_schema.tables
 WHERE table_schema = 'yogurin_bustamantedb';
