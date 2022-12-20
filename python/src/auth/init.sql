CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';

CREATE DATABASE auth;

GRANT ALL PRIVILEGES ON auth.* TO 'admin'@'localhost';

USE auth;

CREATE TABLE user (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

INSERT INTO user (email, password) VALUES ('linhvu2695@gmail.com', 'password')
