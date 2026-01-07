package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Clase principal de Spring Boot.
 * Escanea los componentes y lanza el servidor embebido (Tomcat).
 */

@SpringBootApplication (exclude = { UserDetailsServiceAutoConfiguration.class })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}