-- ============================================================
--  FitStore — SQL Server Setup
--  Ejecutar antes de iniciar Spring Boot (en SSMS o sqlcmd)
-- ============================================================

-- 1. Crear base de datos
IF DB_ID('fitstore_db') IS NULL
    CREATE DATABASE fitstore_db;
GO

USE fitstore_db;
GO

-- 2. Login de aplicacion y acceso
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'fitstore_user')
    CREATE LOGIN fitstore_user WITH PASSWORD = 'Fitstore2026!', CHECK_POLICY = OFF;
GO
IF EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'fitstore_user')
    DROP USER fitstore_user;
GO
CREATE USER fitstore_user FOR LOGIN fitstore_user;
ALTER ROLE db_owner ADD MEMBER fitstore_user;
GO

-- 3. Las tablas las crea Spring Boot automaticamente
--    con spring.jpa.hibernate.ddl-auto=update

-- 4. (Opcional) Verificar que todo quedo bien:
-- SELECT name FROM sys.tables;
-- SELECT * FROM clientes;
-- SELECT * FROM productos;