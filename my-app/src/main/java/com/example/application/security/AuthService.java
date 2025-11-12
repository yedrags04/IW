// Archivo: src/main/java/com/example/vaadinweb/security/AuthService.java
package com.example.application.security;

import com.example.application.repository.ProductoRepository;
import com.example.application.repository.UsuarioRepository;
import com.example.application.model.Producto;
import com.example.application.model.Usuario;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Clave para guardar el rol en la sesión
    public static final String ROL_SESSION_ATTRIBUTE = "userRole";
    public static final String USERNAME_SESSION_ATTRIBUTE = "userName";

    public boolean authenticate(String username, String password) {
        Optional<Usuario> userOpt = usuarioRepository.findByNombre(username);
        
        if (userOpt.isPresent() && userOpt.get().getContrasena().equals(password)) {
            Usuario user = userOpt.get();
            
            // 1. Guarda el rol en la sesión
            VaadinSession.getCurrent().setAttribute(ROL_SESSION_ATTRIBUTE, user.getRol());
            // 2. Guarda el nombre de usuario (para saber si está logueado)
            VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, user.getNombre());
            
            return true;
        }
        return false;
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