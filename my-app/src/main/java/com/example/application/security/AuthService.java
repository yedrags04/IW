package com.example.application.security;

import com.example.application.repository.UsuarioRepository;
import com.example.application.model.Usuario;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final Validator validator;

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
            
            // Guardamos datos individuales
            VaadinSession.getCurrent().setAttribute(ROL_SESSION_ATTRIBUTE, user.getRol());
            VaadinSession.getCurrent().setAttribute(USERNAME_SESSION_ATTRIBUTE, user.getNombre());
            
            // NUEVO: Guardamos el objeto completo para que ProductosView lo encuentre
            VaadinSession.getCurrent().setAttribute("usuario", user);
            
            return true;
        }
        return false;
    }

    @Transactional
    public String register(String name, String email, String password) {
        if (usuarioRepository.findByNombre(name).isPresent()) {
            return "El nombre de usuario ya está en uso.";
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return "El email ya está registrado.";
        }
        
        Usuario newUser = new Usuario(name, password, "USER", email); 
        
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

    public void logout() {
        if (VaadinSession.getCurrent().getSession() != null) {
            VaadinSession.getCurrent().getSession().invalidate();
        }
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