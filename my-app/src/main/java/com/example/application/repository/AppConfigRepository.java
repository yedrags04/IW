package com.example.application.repository;

import com.example.application.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * REPOSITORIO PARA LA CONFIGURACIÓN DE LA APP
 * Gestiona la persistencia de los ajustes visuales y textos globales.
 * Al heredar de JpaRepository, obtenemos operaciones CRUD básicas sin escribir código.
 */
@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    // Implementación automática por Spring Data JPA para el manejo de AppConfig (ID: 1L)
}