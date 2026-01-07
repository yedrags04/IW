package com.example.application.views.dardealta;

import com.example.application.model.Usuario;
import com.example.application.repository.UsuarioRepository;
import com.example.application.views.MainLayout;
import com.example.application.views.main.HomeView;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@PageTitle("Alta de Usuarios | Tu Food")
@Route(value = "alta-usuarios", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminUserRegistrationView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);

    @Autowired
    public AdminUserRegistrationView(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

        addClassName("admin-registration-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- TARJETA CONTENEDORA ---
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content");
        card.setMaxWidth("550px"); // Reducido un poco para mayor estética
        card.setPadding(true);
        card.setSpacing(true);

        // Encabezado
        H2 title = new H2("Registrar Nuevo Usuario");
        Paragraph subtitle = new Paragraph("Complete los datos para dar de alta a un trabajador o cliente.");
        title.addClassName("perfil-title");

        // --- FORMULARIO ---
        FormLayout formLayout = new FormLayout();

        TextField nombre = new TextField("Nombre de Usuario");
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));

        TextField apellido = new TextField("Apellido");
        apellido.setRequiredIndicatorVisible(true);

        EmailField email = new EmailField("Correo Electrónico");
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));

        PasswordField password = new PasswordField("Contraseña");
        password.setPrefixComponent(new Icon(VaadinIcon.KEY));

        Select<String> rol = new Select<>();
        rol.setLabel("Rol del Usuario");
        rol.setItems("TRABAJADOR", "CLIENTE", "ADMIN");
        rol.setPlaceholder("Seleccione un rol");

        // --- CONFIGURACIÓN DE VALIDACIONES (BINDER) ---
        binder.forField(nombre)
            .asRequired("El nombre es obligatorio")
            .withValidator(n -> n.length() >= 3, "Mínimo 3 caracteres")
            .bind(Usuario::getNombre, Usuario::setNombre);

        binder.forField(email)
            .asRequired("El email es obligatorio")
            .withValidator(new com.vaadin.flow.data.validator.EmailValidator("Formato de email inválido"))
            .bind(Usuario::getEmail, Usuario::setEmail);

        binder.forField(password)
            .asRequired("La contraseña es obligatoria")
            .withValidator(p -> p.length() >= 6, "Mínimo 6 caracteres")
            .bind(u -> "", (u, p) -> u.setContrasena(passwordEncoder.encode(p)));

        binder.forField(rol)
            .asRequired("Debe seleccionar un rol")
            .bind(Usuario::getRol, Usuario::setRol);

        formLayout.add(nombre, apellido, email, password, rol);
        formLayout.setColspan(email, 2);
        formLayout.setColspan(password, 2);
        formLayout.setColspan(rol, 2);

        // --- BOTONES (REDISEÑADOS) ---
        Button btnAlta = new Button("Dar de Alta", new Icon(VaadinIcon.USER_CHECK));
        btnAlta.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAlta.setMinWidth("150px");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Menos llamativo
        btnCancelar.setMinWidth("120px");

        // Acción de Cancelar
        // Acción de Cancelar: Redirige explícitamente a la clase HomeView
        btnCancelar.addClickListener(e -> {
            UI.getCurrent().navigate(HomeView.class);
        });

        // Acción de Guardar
        btnAlta.addClickListener(e -> {
            Usuario nuevoUsuario = new Usuario();
            if (binder.writeBeanIfValid(nuevoUsuario) && !apellido.isEmpty()) {
                try {
                    usuarioRepository.save(nuevoUsuario);
                    Notification.show("Usuario " + nuevoUsuario.getNombre() + " creado con éxito")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
                    // Limpiar formulario
                    binder.setBean(new Usuario());
                    apellido.clear();
                    
                } catch (Exception ex) {
                    Notification.show("Error: El nombre o email ya existen")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                if(apellido.isEmpty()) apellido.setInvalid(true);
                Notification.show("Revise los campos obligatorios")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Layout de botones alineados a la derecha y dentro de la tarjeta
        HorizontalLayout actions = new HorizontalLayout(btnCancelar, btnAlta);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END); // Alineación a la derecha
        actions.setSpacing(true);
        actions.setPadding(false);

        card.add(title, subtitle, formLayout, actions);
        add(card);
    }
}