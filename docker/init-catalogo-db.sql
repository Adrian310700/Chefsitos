-- Crear la base de datos para el microservicio de catálogo
CREATE DATABASE IF NOT EXISTS uamishop_catalogo;
GRANT ALL PRIVILEGES ON uamishop_catalogo.* TO 'uamishop'@'%';
FLUSH PRIVILEGES;
