package com.example.application.repository;

import com.example.application.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByNombre(String nombre); 
    
    Optional<Usuario> findByNombreAndContrasena(String nombre, String contrasena);
}