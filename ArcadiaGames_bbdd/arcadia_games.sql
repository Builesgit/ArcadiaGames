
-- BASE DE DATOS: arcadia_games_db

CREATE DATABASE IF NOT EXISTS arcadia_games_db;
USE arcadia_games_db;

-- TABLA: usuarios
CREATE TABLE usuarios (
  id INT(11) NOT NULL AUTO_INCREMENT,
  usuario VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(10) DEFAULT 'user',
  PRIMARY KEY (id)
);

-- DATOS DE LA TABLA: usuarios
INSERT INTO usuarios (usuario, password, rol) VALUES ('admin', '123', 'admin');