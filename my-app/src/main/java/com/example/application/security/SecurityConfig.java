package com.example.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * CONFIGURACIÓN DE SEGURIDAD BÁSICA
 * Solo definimos el PasswordEncoder para que el AuthService pueda 
 * encriptar y comparar contraseñas. Al no configurar HttpSecurity,
 * Spring NO activará su formulario de login por defecto.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}