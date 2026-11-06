-- =====================================================================
-- Sistema de Pedidos - base de datos y usuario de laboratorio (MySQL 8+)
-- Ejecutar con una cuenta administradora:
--   mysql -u root -p < database/Script_BD_usuario_MySql.sql
-- La tabla "pedido" la crea Hibernate (ddl-auto=update) al iniciar la app.
-- La contraseña es la de laboratorio usada por defecto en application.properties;
-- en otros entornos se reemplaza con las variables DB_URL, DB_USERNAME y DB_PASSWORD.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS pedidosdb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'spedidos_app'@'localhost' IDENTIFIED BY 'userapp_2026!';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON pedidosdb.* TO 'spedidos_app'@'localhost';

FLUSH PRIVILEGES;
