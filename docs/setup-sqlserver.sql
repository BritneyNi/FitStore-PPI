-- ============================================================
--  FitStore — SQL Server Setup
--  Ejecutar antes de iniciar Spring Boot (en SSMS o sqlcmd)
--
--  La contraseña NO se escribe en el repositorio. Se pasa como
--  variable de sqlcmd:
--
--    sqlcmd -S localhost -E -i docs/setup-sqlserver.sql -v DB_PASSWORD="TuClaveLocal"
--
--  En SSMS: activa el modo SQLCMD y define la variable antes de ejecutar:
--    :setvar DB_PASSWORD "TuClaveLocal"
-- ============================================================

-- 1. Crear base de datos
IF DB_ID('fitstore_db') IS NULL
    CREATE DATABASE fitstore_db;
GO

USE fitstore_db;
GO

-- 2. Login de aplicacion y acceso
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'fitstore_user')
    CREATE LOGIN fitstore_user WITH PASSWORD = '$(DB_PASSWORD)', CHECK_POLICY = OFF;
GO
IF EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'fitstore_user')
    DROP USER fitstore_user;
GO
CREATE USER fitstore_user FOR LOGIN fitstore_user;
ALTER ROLE db_owner ADD MEMBER fitstore_user;
GO

-- 3. Columna con la URL de la imagen del producto.
--    Hibernate tambien la crea con spring.jpa.hibernate.ddl-auto=update;
--    se incluye aqui para dejar el esquema explicito.
IF OBJECT_ID('productos', 'U') IS NOT NULL AND COL_LENGTH('productos', 'imagen_url') IS NULL
    ALTER TABLE productos ADD imagen_url NVARCHAR(500) NULL;
GO

-- 4. Las tablas las crea Spring Boot automaticamente
--    con spring.jpa.hibernate.ddl-auto=update

-- 5. (Opcional) Verificar que todo quedo bien:
-- SELECT name FROM sys.tables;
-- SELECT * FROM clientes;
-- SELECT * FROM productos;
