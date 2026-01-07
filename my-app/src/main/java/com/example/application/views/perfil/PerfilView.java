package com.example.application.views.perfil;

import com.example.application.model.Usuario;
import com.example.application.repository.UsuarioRepository;
import com.example.application.views.MainLayout;
import com.example.application.views.main.HomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@PageTitle("Mi Perfil | Tu Food")
@Route(value = "perfil", layout = MainLayout.class)
public class PerfilView extends VerticalLayout {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private Usuario usuarioActual;
    
    private TextField nombre = new TextField("Nombre de Usuario");
    private EmailField email = new EmailField("Correo Electrónico");
    private PasswordField password = new PasswordField("Nueva Contraseña (dejar en blanco para no cambiar)");
    
    private Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);

    public PerfilView(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        
        // 1. Obtener usuario de la sesión
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("userName");
        if (nombreUsuario == null) {
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        
        usuarioRepository.findByNombre(nombreUsuario).ifPresentOrElse(u -> {
            this.usuarioActual = u;
            construirVista();
            configurarBinder();
        }, () -> mostrarError("Usuario no encontrado"));
    }
    
    private void configurarBinder() {
        // Vinculamos los campos
        binder.forField(nombre)
            .asRequired("El nombre es obligatorio")
            .bind(Usuario::getNombre, Usuario::setNombre);

        binder.forField(email)
            .asRequired("El email es obligatorio")
            .withValidator(new com.vaadin.flow.data.validator.EmailValidator("Email no válido"))
            .bind(Usuario::getEmail, Usuario::setEmail);

        // La contraseña es especial: solo se encripta y guarda si el usuario escribe algo
        binder.forField(password)
            .withValidator(p -> p.isEmpty() || p.length() >= 6, "La contraseña debe tener al menos 6 caracteres")
            .bind(u -> "", (u, p) -> {
                if (!p.isEmpty()) {
                    u.setContrasena(passwordEncoder.encode(p));
                }
            });

        // Cargamos los datos actuales del objeto al formulario
        binder.readBean(usuarioActual);
    }
    
    private void construirVista() {
        addClassName("perfil-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content"); 
        card.setMaxWidth("600px");
        card.setPadding(true);

        // --- ENCABEZADO CON AVATAR ---
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        Avatar avatar = new Avatar(usuarioActual.getNombre());
        avatar.setWidth("60px");
        avatar.setHeight("60px");
        
        VerticalLayout titleLayout = new VerticalLayout(new H2("Mi Perfil"), new Paragraph("Rol: " + usuarioActual.getRol()));
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        header.add(avatar, titleLayout);

        // --- FORMULARIO ---
        FormLayout formLayout = new FormLayout();
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        password.setPrefixComponent(new Icon(VaadinIcon.KEY));

        formLayout.add(nombre, email, password);
        formLayout.setColspan(nombre, 1);
        formLayout.setColspan(email, 1);
        formLayout.setColspan(password, 2);

        // --- ACCIONES ---
        Button guardar = new Button("Actualizar Datos", new Icon(VaadinIcon.CHECK));
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClickListener(e -> guardarCambios());

        Button cancelar = new Button("Cancelar");
        cancelar.addClickListener(e -> UI.getCurrent().navigate(HomeView.class));

        HorizontalLayout actions = new HorizontalLayout(cancelar, guardar);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        card.add(header, formLayout, actions);
        add(card);
    }

    private void guardarCambios() {
        // El rol no se toca porque no está bindeado a ningún campo del formulario
        if (binder.writeBeanIfValid(usuarioActual)) {
            try {
                usuarioRepository.save(usuarioActual);
                
                // Actualizamos la sesión por si cambió el nombre
                VaadinSession.getCurrent().setAttribute("userName", usuarioActual.getNombre());
                
                Notification.show("Perfil actualizado con éxito")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Limpiar campo password después de guardar
                password.clear();
            } catch (Exception ex) {
                mostrarError("Error: El nombre o email ya están en uso.");
            }
        } else {
            mostrarError("Por favor, revise los errores en el formulario");
        }
    }
    
    private void mostrarError(String mensaje) {
        Notification.show(mensaje).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}