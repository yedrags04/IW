package com.example.application.repository;

import com.example.application.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    // Al extender de JpaRepository, Spring ya nos da métodos como
    // findById(id), save(entidad) y findAll() automáticamente.
}