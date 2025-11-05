package com.example.vaadinweb.views;

import com.example.vaadinweb.repository.UsuarioRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;


@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView(UsuarioRepository usuarioRepo) {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // 📝 DECLARACIÓN DE COMPONENTES AL INICIO
        H1 title = new H1("🔐 Iniciar Sesión"); // 👈 Declaración movida aquí
        TextField usuario = new TextField("Usuario"); // 👈 Declaración movida aquí
        PasswordField contrasena = new PasswordField("Contraseña"); // 👈 Declaración movida aquí

        Button entrar = new Button("Entrar", e -> {
            // Aquí 'usuario' y 'contrasena' son visibles (en scope)
            var encontrado = usuarioRepo.findByNombreAndContrasena(
                usuario.getValue(), contrasena.getValue()
            );

            if (encontrado.isPresent()) {
                var usuarioEncontrado = encontrado.get();
                
                // 1. Guardamos el nombre de usuario
                VaadinSession.getCurrent().setAttribute("userName", usuarioEncontrado.getNombre());
                
                // 2. 🔑 GUARDAMOS EL ROL EN LA SESIÓN (usa getRol() del modelo actualizado)
                VaadinSession.getCurrent().setAttribute("userRole", usuarioEncontrado.getRol());

                Notification.show("✅ Inicio de sesión correcto. Rol: " + usuarioEncontrado.getRol());
                getUI().ifPresent(ui -> ui.navigate("")); // volver a la vista principal
            } else {
                Notification.show("❌ Usuario o contraseña incorrectos");
            }
        });
        
        // 📝 Añadir todos los componentes al layout
        add(title, usuario, contrasena, entrar);
    }
}