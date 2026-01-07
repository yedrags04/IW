package com.example.application.security;

import com.example.application.repository.UsuarioRepository;
import com.example.application.model.Usuario;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

/* * SERVICIO DE AUTENTICACIÓN Y GESTIÓN DE USUARIOS
 * Esta clase centraliza la lógica de login, registro y control de sesiones.
 * Utiliza VaadinSession para mantener los datos del usuario activo en el navegador.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final Validator validator; // Validador de Bean Validation (JSR 303)
    private final PasswordEncoder passwordEncoder; // Encriptador de contraseñas (BCrypt)

    // Claves constantes para evitar errores de escritura al acceder a los atributos de sesión
    public static final String ROL_SESSION_ATTRIBUTE = "userRole";
    public static final String USERNAME_SESSION_ATTRIBUTE = "userName";
    public static final String USUARIO_COMPLETO_ATTRIBUTE = "usuario";

    public AuthService(UsuarioRepository usuarioRepository, Validator validator, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.validator = validator; 
        this.passwordEncoder = passwordEncoder;
    }

    // Recupera el objeto Usuario completo guardado en la sesión actual
    public Usuario getAuthenticatedUser() {
        return (Usuario) VaadinSession.getCurrent().getAttribute(USUARIO_COMPLETO_ATTRIBUTE);
    }

    // Procesa el inicio de sesión verificando credenciales en la base de datos
    public boolean authenticate(String username, String password) {
        Optional<Usuario> userOpt = usuarioRepository.findByNombre(username);
        
        // Verificamos si el usuario existe y si la contraseña coincide (usando el encoder)
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getContrasena())) {
            Usuario user = userOpt.get();
            
            // Si es correcto, inyectamos los datos en la sesión de Vaadin
            VaadinSession.getCurrent().setAttribute(ROL_SESSION_ATTRIBUTE, user.getRol());
            VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, user.getNombre());
            VaadinSession.getCurrent().setAttribute(USUARIO_COMPLETO_ATTRIBUTE, user);
            
            return true;
        }
        return false;
    }

    // Registra un nuevo usuario aplicando encriptación y validación de datos
    @Transactional
    public String register(String name, String email, String password) {
        // Validaciones previas de existencia
        if (usuarioRepository.findByNombre(name).isPresent()) {
            return "El nombre de usuario ya está en uso.";
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return "El email ya está registrado.";
        }
        
        // Encriptación de la contraseña antes de guardarla (Seguridad)
        String encryptedPassword = passwordEncoder.encode(password);
        Usuario newUser = new Usuario(name, encryptedPassword, "USER", email); 
        
        // Validación manual de las anotaciones de la clase Usuario (@Email, @Size, etc.)
        Set<ConstraintViolation<Usuario>> violations = validator.validate(newUser);
        if (!violations.isEmpty()) {
            return violations.iterator().next().getMessage();
        }

        try {
            usuarioRepository.save(newUser);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno del sistema al intentar registrar.";
        }
    }

    // Finaliza la sesión actual y limpia los datos del navegador
    public void logout() {
        if (VaadinSession.getCurrent().getSession() != null) {
            VaadinSession.getCurrent().getSession().invalidate();
        }
        VaadinSession.getCurrent().close();
    }

    // MÉTODOS DE COMPROBACIÓN DE ROLES
    // Utilizados en las vistas para ocultar o mostrar elementos según el permiso del usuario
    
    public boolean isAdmin() {
        String role = (String) VaadinSession.getCurrent().getAttribute(ROL_SESSION_ATTRIBUTE);
        return "ADMIN".equals(role);
    }
    
    public boolean isUserLoggedIn() {
        return VaadinSession.getCurrent().getAttribute(USERNAME_SESSION_ATTRIBUTE) != null;
    }

    public boolean isWorker() {
        String role = (String) VaadinSession.getCurrent().getAttribute(ROL_SESSION_ATTRIBUTE);
        return "TRABAJADOR".equals(role);
    }
}