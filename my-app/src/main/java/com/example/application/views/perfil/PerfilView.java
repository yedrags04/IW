package com.example.application.views.perfil; // Asegúrate de que coincida con tu estructura de carpetas

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

@PageTitle("Mi Perfil | Tu Food")
@Route(value = "perfil", layout = MainLayout.class)
public class PerfilView extends VerticalLayout {

    public PerfilView() {
        // 1. Configuración del contenedor principal de la vista
        addClassName("perfil-view");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // --- TARJETA DE PERFIL (CARD) ---
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content"); 
        card.setWidthFull();
        card.setMaxWidth("800px"); 
        card.setPadding(false); // El padding se maneja vía CSS (.perfil-card-content)
        card.setSpacing(false);

        // --- ENCABEZADO (Avatar y Título) ---
        HorizontalLayout header = createHeader();
        header.addClassName("perfil-header");

        // --- FORMULARIO ---
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("perfil-form");
        formLayout.setWidthFull();

        TextField nombre = new TextField("Nombre");
        nombre.setPrefixComponent(new Icon(VaadinIcon.USER));
        nombre.setValue("Juan");

        TextField apellidos = new TextField("Apellidos");
        apellidos.setValue("Pérez");

        EmailField email = new EmailField("Correo Electrónico");
        email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        email.setValue("juan.perez@tufood.com");

        TextField restaurante = new TextField("Restaurante");
        restaurante.setPrefixComponent(new Icon(VaadinIcon.SHOP));
        restaurante.setValue("La Parri-Burger");

        // Campo Admin (Solo lectura)
        TextField rol = new TextField("Rol de Usuario");
        rol.setPrefixComponent(new Icon(VaadinIcon.SHIELD));
        rol.setValue("Administrador del Sistema");
        rol.setReadOnly(true);
        rol.addClassName("readonly-field");

        // Organización del Layout (Responsivo)
        formLayout.add(nombre, apellidos, email, restaurante, rol);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Configuración de columnas (Email, Restaurante y Rol ocupan toda la fila)
        formLayout.setColspan(email, 2);
        formLayout.setColspan(restaurante, 2);
        formLayout.setColspan(rol, 2);

        // --- BOTONES DE ACCIÓN ---
        Button guardar = new Button("Guardar Cambios", new Icon(VaadinIcon.CHECK));
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClassName("btn-primary");
        guardar.addClickListener(e -> {
            Notification n = Notification.show("¡Perfil actualizado!");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.addClassName("btn-tertiary");

        HorizontalLayout actions = new HorizontalLayout(guardar, cancelar);
        actions.addClassName("perfil-actions");
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setSpacing(true);

        // Montaje de la tarjeta
        card.add(header, formLayout, actions);
        
        // Agregar la tarjeta al centro de la vista
        add(card);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        Avatar avatar = new Avatar("Juan Pérez");
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        avatar.getStyle().set("border", "2px solid var(--lumo-primary-color-10pct)");

        VerticalLayout textContainer = new VerticalLayout();
        textContainer.setPadding(false);
        textContainer.setSpacing(false);

        H2 title = new H2("Configuración de Perfil");
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "1.5rem");
        
        Paragraph subtitle = new Paragraph("Gestiona tu información personal y del restaurante");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin", "0");

        textContainer.add(title, subtitle);
        header.add(avatar, textContainer);

        return header;
    }
}