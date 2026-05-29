-- ============================================================
--  FitStore — MySQL Setup
--  Ejecutar antes de iniciar Spring Boot
-- ============================================================

-- 1. Crear base de datos
CREATE DATABASE IF NOT EXISTS fitstore_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE fitstore_db;

-- 2. Las tablas las crea Spring Boot automáticamente
--    con spring.jpa.hibernate.ddl-auto=update

-- 3. (Opcional) Verificar que todo quedó bien:
-- SHOW TABLES;
-- SELECT * FROM clientes;
-- SELECT * FROM productos;
