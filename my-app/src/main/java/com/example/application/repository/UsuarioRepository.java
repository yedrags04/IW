package com.example.application.repository;

import com.example.application.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/*
 * REPOSITORIO PARA USUARIOS
 * Gestiona el acceso de los empleados y el registro de nuevos usuarios.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Método fundamental para el proceso de Login (AuthService).
    // Busca un usuario por su nombre de cuenta. Retorna un Optional para gestionar nulos de forma segura.
    Optional<Usuario> findByNombre(String nombre); 
    
    // Se utiliza durante el registro para evitar que dos usuarios tengan el mismo correo electrónico.
    Optional<Usuario> findByEmail(String email); 
}