CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          rol ENUM('ADMIN','USER') NOT NULL
);

CREATE TABLE analisis_historial (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    usuario_id BIGINT NOT NULL,
                                    consumo_kwh INT NOT NULL,
                                    costo_estimado_mensual DECIMAL(12,2) NOT NULL,
                                    probabilidad DOUBLE NOT NULL,
                                    fecha DATETIME(6) NOT NULL,
                                    categoria VARCHAR(255) NOT NULL,
                                    CONSTRAINT fk_analisis_historial_usuario
                                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);