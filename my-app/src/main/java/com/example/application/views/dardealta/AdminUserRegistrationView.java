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

/* * VISTA DE ALTA DE USUARIOS (ADMIN)
 * Permite al administrador crear nuevas cuentas de trabajadores o clientes.
 * Utiliza Binder para enlazar los campos del formulario con el modelo Usuario.
 */
@PageTitle("Alta de Usuarios | Tu Food")
@Route(value = "alta-usuarios", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminUserRegistrationView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    // El Binder gestiona la sincronización de datos y las validaciones del Bean
    private final Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);

    @Autowired
    public AdminUserRegistrationView(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card");
        card.setMaxWidth("550px");

        H2 title = new H2("Registrar Nuevo Usuario");
        Paragraph subtitle = new Paragraph("Complete los datos para dar de alta a un trabajador o cliente.");

        // CONFIGURACIÓN DE LOS CAMPOS DEL FORMULARIO
        FormLayout formLayout = new FormLayout();
        TextField nombre = new TextField("Nombre de Usuario");
        TextField apellido = new TextField("Apellido"); // Campo adicional no persistido
        EmailField email = new EmailField("Correo Electrónico");
        PasswordField password = new PasswordField("Contraseña");
        
        Select<String> rol = new Select<>();
        rol.setLabel("Rol del Usuario");
        rol.setItems("TRABAJADOR", "CLIENTE", "ADMIN");

        // VINCULACIÓN DE CAMPOS Y VALIDACIÓN
        binder.forField(nombre)
            .asRequired("El nombre es obligatorio")
            .withValidator(n -> n.length() >= 3, "Mínimo 3 caracteres")
            .bind(Usuario::getNombre, Usuario::setNombre);

        binder.forField(email)
            .asRequired("El email es obligatorio")
            .withValidator(new com.vaadin.flow.data.validator.EmailValidator("Formato inválido"))
            .bind(Usuario::getEmail, Usuario::setEmail);

        // La contraseña se encripta antes de guardarse en el objeto
        binder.forField(password)
            .asRequired("Obligatorio")
            .bind(u -> "", (u, p) -> u.setContrasena(passwordEncoder.encode(p)));

        binder.forField(rol).asRequired().bind(Usuario::getRol, Usuario::setRol);

        formLayout.add(nombre, apellido, email, password, rol);

        // BOTONES DE ACCIÓN
        Button btnAlta = new Button("Dar de Alta", new Icon(VaadinIcon.USER_CHECK));
        btnAlta.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> UI.getCurrent().navigate(HomeView.class));

        // Lógica de guardado
        btnAlta.addClickListener(e -> {
            Usuario nuevoUsuario = new Usuario();
            // Comprobamos si el formulario cumple todas las reglas del Binder
            if (binder.writeBeanIfValid(nuevoUsuario)) {
                try {
                    usuarioRepository.save(nuevoUsuario);
                    Notification.show("Usuario creado").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    binder.setBean(new Usuario()); // Limpia el formulario
                } catch (Exception ex) {
                    Notification.show("Error: Nombre o Email duplicado").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });

        HorizontalLayout actions = new HorizontalLayout(btnCancelar, btnAlta);
        card.add(title, subtitle, formLayout, actions);
        add(card);
    }
}