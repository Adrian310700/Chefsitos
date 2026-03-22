-- Crear la base de datos para el microservicio de catálogo
CREATE DATABASE IF NOT EXISTS uamishop_catalogo;
GRANT ALL PRIVILEGES ON uamishop_catalogo.* TO 'uamishop'@'%';
FLUSH PRIVILEGES;

CREATE DATABASE IF NOT EXISTS uamishop_ventas;
GRANT ALL PRIVILEGES ON uamishop_ventas.* TO 'uamishop'@'%';
FLUSH PRIVILEGES;

CREATE DATABASE IF NOT EXISTS uamishop_ordenes;
GRANT ALL PRIVILEGES ON uamishop_ordenes.* TO 'uamishop'@'%';
FLUSH PRIVILEGES;
