    CREATE TABLE IF NOT EXISTS producto (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        nombre VARCHAR(255),
        precio DOUBLE,
        categoria VARCHAR(255)
    );

    INSERT INTO producto (nombre, precio, categoria) VALUES
    ('Hamburguesa', 5.99, 'Comida'),
    ('Patatas', 2.49, 'Comida'),
    ('Coca-Cola', 1.99, 'Bebida');


    CREATE TABLE IF NOT EXISTS usuarios (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        nombre VARCHAR(255) NOT NULL UNIQUE,
        contrasena VARCHAR(255) NOT NULL,
        rol VARCHAR(50) NOT NULL 
    );

    INSERT INTO usuarios (nombre, contrasena, rol) VALUES
    ('admin', '1234', 'ADMIN'),
    ('miguel', 'lolalolita', 'USER');