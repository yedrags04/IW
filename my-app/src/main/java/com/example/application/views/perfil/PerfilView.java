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

/**
 * VISTA DE PERFIL DE USUARIO
 * Permite al usuario autenticado visualizar y editar sus datos personales,
 * incluyendo el cambio de contraseña con encriptación segura.
 */
@PageTitle("Mi Perfil | Tu Food")
@Route(value = "perfil", layout = MainLayout.class)
public class PerfilView extends VerticalLayout {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private Usuario usuarioActual; // Almacena el objeto de usuario que estamos editando
    
    // Componentes del formulario
    private TextField nombre = new TextField("Nombre de Usuario");
    private EmailField email = new EmailField("Correo Electrónico");
    private PasswordField password = new PasswordField("Nueva Contraseña (dejar en blanco para no cambiar)");
    
    // El Binder vincula automáticamente los campos de la UI con los atributos del modelo Usuario
    private Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);

    public PerfilView(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        
        // 1. CONTROL DE ACCESO: Obtenemos el nombre del usuario desde la sesión de Vaadin
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("userName");
        
        // Si no hay sesión iniciada, redirigimos al inicio inmediatamente
        if (nombreUsuario == null) {
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        
        // Buscamos los datos completos del usuario en la base de datos
        usuarioRepository.findByNombre(nombreUsuario).ifPresentOrElse(u -> {
            this.usuarioActual = u;
            construirVista(); // Genera la interfaz gráfica
            configurarBinder(); // Configura la lógica de validación y vinculación
        }, () -> mostrarError("Usuario no encontrado"));
    }
    
    /**
     * Configura la lógica de validación y vinculación de datos entre el formulario y el modelo.
     */
    private void configurarBinder() {
        // Vinculación del campo Nombre
        binder.forField(nombre)
            .asRequired("El nombre es obligatorio")
            .bind(Usuario::getNombre, Usuario::setNombre);

        // Vinculación del campo Email con validador de formato
        binder.forField(email)
            .asRequired("El email es obligatorio")
            .withValidator(new com.vaadin.flow.data.validator.EmailValidator("Email no válido"))
            .bind(Usuario::getEmail, Usuario::setEmail);

        /**
         * Lógica especial para la CONTRASEÑA:
         * Solo se encripta y se guarda si el campo no está vacío. Esto permite al usuario
         * actualizar otros datos sin necesidad de cambiar su contraseña actual.
         */
        binder.forField(password)
            .withValidator(p -> p.isEmpty() || p.length() >= 6, "La contraseña debe tener al menos 6 caracteres")
            .bind(u -> "", (u, p) -> {
                if (!p.isEmpty()) {
                    u.setContrasena(passwordEncoder.encode(p)); // Encriptación BCrypt
                }
            });

        // Carga los datos actuales del objeto usuarioActual en los campos de texto
        binder.readBean(usuarioActual);
    }
    
    /**
     * Crea y organiza los componentes visuales de la vista.
     */
    private void construirVista() {
        addClassName("perfil-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Tarjeta central que contiene el perfil
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content"); 
        card.setMaxWidth("600px");
        card.setPadding(true);

        // --- ENCABEZADO: Avatar y Título ---
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        
        // Avatar circular con la inicial del usuario
        Avatar avatar = new Avatar(usuarioActual.getNombre());
        avatar.setWidth("60px");
        avatar.setHeight("60px");
        
        VerticalLayout titleLayout = new VerticalLayout(new H2("Mi Perfil"), new Paragraph("Rol: " + usuarioActual.getRol()));
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        header.add(avatar, titleLayout);

        // --- FORMULARIO: Layout de campos ---
        FormLayout formLayout = new FormLayout();
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        password.setPrefixComponent(new Icon(VaadinIcon.KEY));

        formLayout.add(nombre, email, password);
        // Configuración de columnas para que sea responsivo
        formLayout.setColspan(nombre, 1);
        formLayout.setColspan(email, 1);
        formLayout.setColspan(password, 2); // La contraseña ocupa toda la fila

        // --- ACCIONES: Botones de Guardar y Cancelar ---
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

    /**
     * Procesa la actualización de los datos en la base de datos.
     */
    private void guardarCambios() {
        // Valida los datos y los escribe en el objeto usuarioActual
        if (binder.writeBeanIfValid(usuarioActual)) {
            try {
                // Persistencia en base de datos
                usuarioRepository.save(usuarioActual);
                
                // IMPORTANTE: Actualizamos la sesión por si el usuario cambió su nombre,
                // para que el MainLayout se actualice correctamente.
                VaadinSession.getCurrent().setAttribute("userName", usuarioActual.getNombre());
                
                Notification.show("Perfil actualizado con éxito")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Limpiamos el campo de contraseña por seguridad después del éxito
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