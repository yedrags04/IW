package com.example.application.repository;

import com.example.application.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Necesario para el login y para verificar si el usuario ya existe en el registro
    Optional<Usuario> findByNombre(String nombre); 
    
    // Opcional: Puede ser útil para validar si el email ya existe
    Optional<Usuario> findByEmail(String email); 
}   