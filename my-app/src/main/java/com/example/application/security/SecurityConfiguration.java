package com.example.application.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/* * CONFIGURACIÓN DE SEGURIDAD DE SPRING SECURITY
 * Establece las reglas de acceso a nivel de protocolo HTTP.
 * Define qué rutas son públicas y cuál es el mecanismo de login.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    // Define el algoritmo de encriptación de contraseñas (BCrypt es el estándar actual)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1. CONFIGURACIÓN DE ACCESOS PÚBLICOS
        // Permitimos que cualquier usuario acceda a la raíz (web principal) y recursos estáticos
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/line-awesome/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/icons/**")).permitAll()
        );

        // Llamamos al configurador base de Vaadin que gestiona la seguridad interna del framework
        super.configure(http);

        // 2. CONFIGURACIÓN DEL LOGIN
        // Establecemos que el formulario de acceso está en la ruta "/login"
        // Vaadin redirigirá automáticamente aquí cuando un usuario intente entrar en zonas protegidas
        setLoginView(http, "/login"); 
    }
}