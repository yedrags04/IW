package com.example.application.views.perfil;

import com.example.application.model.Usuario;
import com.example.application.repository.UsuarioRepository;
import com.example.application.views.MainLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Mi Perfil | Tu Food")
@Route(value = "perfil", layout = MainLayout.class)
public class PerfilView extends VerticalLayout {
    
    private final UsuarioRepository usuarioRepository;
    private Usuario usuarioActual;
    private TextField nombre;
    private TextField apellidos;
    private EmailField email;

    public PerfilView(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        
        // Obtener el usuario logueado de la sesión
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("userName");
        if (nombreUsuario == null) {
            mostrarError("Debes estar logueado para acceder al perfil");
            return;
        }
        
        // Cargar usuario de la base de datos
        var usuarioOpt = usuarioRepository.findByNombre(nombreUsuario);
        if (usuarioOpt.isEmpty()) {
            mostrarError("Usuario no encontrado en la base de datos");
            return;
        }
        
        this.usuarioActual = usuarioOpt.get();
        construirVista();
    }
    
    private void construirVista() {
        addClassName("perfil-view");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // --- TARJETA DE PERFIL (CARD) ---
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content"); 
        card.setWidthFull();
        card.setMaxWidth("800px"); 
        card.setPadding(false);
        card.setSpacing(false);

        // --- ENCABEZADO ---
        HorizontalLayout header = createHeader();
        header.addClassName("perfil-header");

        // --- FORMULARIO ---
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("perfil-form");
        formLayout.setWidthFull();

        nombre = new TextField("Nombre");
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));
        nombre.setValue(usuarioActual.getNombre());

        apellidos = new TextField("Apellidos");
        apellidos.setPlaceholder("Añade tus apellidos");
        apellidos.setValue(""); 

        email = new EmailField("Correo Electrónico");
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        email.setValue(usuarioActual.getEmail());

        // Organización del Layout
        formLayout.add(nombre, apellidos, email);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Configuración de columnas (El email ocupa toda la fila)
        formLayout.setColspan(email, 2);

        // --- BOTONES DE ACCIÓN ---
        Button guardar = new Button("Guardar Cambios", new Icon(VaadinIcon.CHECK));
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClickListener(e -> guardarCambios());

        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.addClickListener(e -> recargarDatos());

        HorizontalLayout actions = new HorizontalLayout(guardar, cancelar);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setSpacing(true);

        card.add(header, formLayout, actions);
        add(card);
    }

    private void guardarCambios() {
        try {
            if (nombre.getValue().trim().isEmpty()) {
                mostrarError("El nombre no puede estar vacío");
                return;
            }
            if (!email.getValue().contains("@")) {
                mostrarError("Email inválido");
                return;
            }
            
            usuarioActual.setNombre(nombre.getValue().trim());
            usuarioActual.setEmail(email.getValue().trim());
            
            usuarioRepository.save(usuarioActual);
            VaadinSession.getCurrent().setAttribute("userName", usuarioActual.getNombre());
            
            mostrarExito("Perfil actualizado correctamente");
        } catch (Exception ex) {
            mostrarError("Error al guardar: " + ex.getMessage());
        }
    }
    
    private void recargarDatos() {
        nombre.setValue(usuarioActual.getNombre());
        email.setValue(usuarioActual.getEmail());
        apellidos.clear();
        mostrarInfo("Cambios cancelados");
    }
    
    private void mostrarExito(String mensaje) {
        Notification n = Notification.show(mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void mostrarError(String mensaje) {
        Notification n = Notification.show("❌ " + mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    private void mostrarInfo(String mensaje) {
        Notification.show("ℹ️ " + mensaje);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        Avatar avatar = new Avatar(usuarioActual.getNombre());
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        avatar.getStyle().set("border", "2px solid var(--lumo-primary-color-10pct)");

        VerticalLayout textContainer = new VerticalLayout();
        textContainer.setPadding(false);
        textContainer.setSpacing(false);

        H2 title = new H2("Configuración de Perfil");
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "1.5rem");
        
        Paragraph subtitle = new Paragraph("Sesión activa: " + usuarioActual.getNombre());
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin", "0");

        textContainer.add(title, subtitle);
        header.add(avatar, textContainer);

        return header;
    }
}