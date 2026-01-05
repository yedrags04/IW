package com.example.application.views.dardealta;

import com.example.application.views.MainLayout;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Alta de Usuarios | Tu Food")
@Route(value = "alta-usuarios", layout = MainLayout.class)
@RolesAllowed("ADMIN") // Solo accesible por administradores
public class AdminUserRegistrationView extends VerticalLayout {

    public AdminUserRegistrationView() {
        addClassName("admin-registration-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- TARJETA CONTENEDORA ---
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content");
        card.setMaxWidth("600px");
        card.setPadding(true);

        // Encabezado
        H2 title = new H2("Registrar Nuevo Usuario");
        Paragraph subtitle = new Paragraph("Complete los datos para dar de alta a un trabajador o cliente.");
        title.addClassName("perfil-title");

        // --- FORMULARIO ---
        FormLayout formLayout = new FormLayout();

        TextField nombre = new TextField("Nombre");
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));
        nombre.setRequired(true);

        TextField apellido = new TextField("Apellido");
        apellido.setRequired(true);

        EmailField email = new EmailField("Correo Electrónico");
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        email.setRequired(true);

        PasswordField password = new PasswordField("Contraseña");
        password.setPrefixComponent(new Icon(VaadinIcon.KEY));
        password.setRequired(true);

        Select<String> rol = new Select<>();
        rol.setLabel("Rol del Usuario");
        rol.setItems("Trabajador", "Cliente");
        rol.setPlaceholder("Seleccione un rol");
        rol.setRequiredIndicatorVisible(true);

        formLayout.add(nombre, apellido, email, password, rol);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), 
                                     new FormLayout.ResponsiveStep("400px", 2));
        
        // El email y password ocupan toda la fila para mejor lectura
        formLayout.setColspan(email, 2);
        formLayout.setColspan(password, 2);
        formLayout.setColspan(rol, 2);

        // --- BOTONES ---
        Button btnAlta = new Button("Dar de Alta", new Icon(VaadinIcon.USER_CHECK));
        btnAlta.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAlta.addClassName("btn-primary");
        btnAlta.setWidthFull();

        btnAlta.addClickListener(e -> {
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rol.isEmpty()) {
                Notification.show("Por favor, rellene todos los campos obligatorios")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                // Aquí iría tu lógica para guardar en la base de datos
                Notification.show("Usuario " + nombre.getValue() + " registrado como " + rol.getValue())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                limpiarCampos(nombre, apellido, email, password, rol);
            }
        });

        card.add(title, subtitle, formLayout, btnAlta);
        add(card);
    }

    private void limpiarCampos(TextField n, TextField a, EmailField e, PasswordField p, Select<String> r) {
        n.clear();
        a.clear();
        e.clear();
        p.clear();
        r.clear();
    }
}