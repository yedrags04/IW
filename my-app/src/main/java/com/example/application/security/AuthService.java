package com.example.application.security;

import com.example.application.repository.UsuarioRepository;
import com.example.application.model.Usuario;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

// --- Imports para Bean Validation ---
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final Validator validator; // Inyectar el Validador de Bean Validation

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, Validator validator) {
        this.usuarioRepository = usuarioRepository;
        this.validator = validator; 
    }

    public static final String ROL_SESSION_ATTRIBUTE = "userRole";
    public static final String USERNAME_SESSION_ATTRIBUTE = "userName";

    public boolean authenticate(String username, String password) {
        Optional<Usuario> userOpt = usuarioRepository.findByNombre(username);
        
        if (userOpt.isPresent() && userOpt.get().getContrasena().equals(password)) {
            Usuario user = userOpt.get();
            
            VaadinSession.getCurrent().setAttribute(ROL_SESSION_ATTRIBUTE, user.getRol());
            VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, user.getNombre());
            
            return true;
        }
        return false;
    }

    // Método de registro con todas las comprobaciones (Unicidad y Bean Validation)
    @Transactional
    public String register(String name, String email, String password) {
        
        // 1. Comprobación de unicidad (si no se hace en el front-end)
        if (usuarioRepository.findByNombre(name).isPresent()) {
            return "El nombre de usuario ya está en uso.";
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return "El email ya está registrado.";
        }
        
        Usuario newUser = new Usuario(name, password, "USER", email); 
        
        // 2. Ejecutar Bean Validation (comprueba @Size, @NotEmpty, @Email)
        Set<ConstraintViolation<Usuario>> violations = validator.validate(newUser);
        if (!violations.isEmpty()) {
            // Devuelve el primer error de validación de la entidad
            return violations.iterator().next().getMessage();
        }

        // 3. Guardar en TiDB Cloud
        try {
            // ⚠️ ADVERTENCIA: La contraseña debe ser hasheada (BCrypt) en un entorno real.
            usuarioRepository.save(newUser);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno del sistema al intentar registrar.";
        }
    }

    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close();
    }

    public boolean isAdmin() {
        String role = (String) VaadinSession.getCurrent().getAttribute(ROL_SESSION_ATTRIBUTE);
        return "ADMIN".equals(role);
    }
    
    public boolean isUserLoggedIn() {
        return VaadinSession.getCurrent().getAttribute(USERNAME_SESSION_ATTRIBUTE) != null;
    }
}