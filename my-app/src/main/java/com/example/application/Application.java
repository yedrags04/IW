package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLASE DE ARRANQUE (Entry Point)
 * La anotación @SpringBootApplication activa tres características:
 * 1. @EnableAutoConfiguration: Configura automáticamente Spring según las dependencias del pom.xml.
 * 2. @ComponentScan: Busca componentes (@Service, @Repository, @Component) en este paquete y subpaquetes.
 * 3. @Configuration: Permite registrar beans adicionales en el contexto.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Lanza la aplicación Spring Boot y arranca el servidor embebido (Tomcat por defecto)
        SpringApplication.run(Application.class, args);
    }
}