package com.example.application.security;

import com.example.application.model.Usuario;
import com.example.application.repository.UsuarioRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * SERVICIO DE AUTENTICACIÓN Y REGISTRO
 * Gestiona el acceso de usuarios, la persistencia de la sesión y el alta de nuevos clientes.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Atributo constante para identificar el nombre de usuario en la sesión de Vaadin
    public static final String USERNAME_SESSION_ATTRIBUTE = "userName";

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Proceso de Login manual.
     * Compara la contraseña encriptada y guarda el usuario en la sesión si es correcto.
     */
    public boolean authenticate(String nombre, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombre(nombre);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificamos si la contraseña coincide con el hash de la base de datos
            if (passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                // GUARDADO EN SESIÓN: Permite que el MainLayout y otras vistas sepan quién entró
                VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, usuario.getNombre());
                VaadinSession.getCurrent().setAttribute("userRole", usuario.getRol());
                return true;
            }
        }
        return false;
    }

    /**
     * PROCESO DE REGISTRO DE CLIENTES (CORREGIDO)
     * Método utilizado por LoginFlipView para dar de alta a nuevos usuarios desde la web.
     */
    public String register(String nombre, String email, String contrasena) {
        // Verificación de duplicados antes de intentar guardar
        if (usuarioRepository.findByNombre(nombre).isPresent()) {
            return "El nombre de usuario ya existe";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);
        
        // Encriptamos la contraseña por seguridad
        nuevoUsuario.setContrasena(passwordEncoder.encode(contrasena));

        // --- CORRECCIÓN DE ROL ---
        // Antes se guardaba como USER, ahora asignamos CLIENTE por defecto
        nuevoUsuario.setRol("CLIENTE"); 

        try {
            usuarioRepository.save(nuevoUsuario);
            return "SUCCESS";
        } catch (Exception e) {
            return "Error al guardar el usuario: " + e.getMessage();
        }
    }

    /**
     * Finaliza la sesión actual del usuario en Vaadin.
     */
    public void logout() {
        VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, null);
        VaadinSession.getCurrent().setAttribute("userRole", null);
        VaadinSession.getCurrent().close(); // Cierra la sesión de Vaadin
    }

    // --- MÉTODOS DE COMPROBACIÓN DE ROLES ---

    public boolean isUserLoggedIn() {
        return VaadinSession.getCurrent().getAttribute(USERNAME_SESSION_ATTRIBUTE) != null;
    }

    public boolean isAdmin() {
        String role = (String) VaadinSession.getCurrent().getAttribute("userRole");
        return "ADMIN".equals(role);
    }

    public boolean isWorker() {
        String role = (String) VaadinSession.getCurrent().getAttribute("userRole");
        return "TRABAJADOR".equals(role) || "ADMIN".equals(role);
    }
}