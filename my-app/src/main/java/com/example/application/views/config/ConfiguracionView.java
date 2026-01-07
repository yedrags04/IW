package com.example.application.views.config;

import com.example.application.model.AppConfig;
import com.example.application.repository.AppConfigRepository;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Vista de configuración global.
 * Permite al administrador cambiar la identidad visual de la web.
 */
@PageTitle("Configuración | Tu Food")
@Route(value = "configuracion", layout = MainLayout.class)
@RolesAllowed("ADMIN") // Solo accesible por administradores
public class ConfiguracionView extends VerticalLayout {

    private final AppConfigRepository repository;
    private AppConfig config;

    public ConfiguracionView(AppConfigRepository repository) {
        this.repository = repository;
        // Buscamos la configuración con ID 1 (única fila de config)
        this.config = repository.findById(1L).orElse(new AppConfig());

        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("perfil-card");
        card.setMaxWidth("600px");

        H2 title = new H2("Personalizar Apariencia");

        // Formulario de edición
        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre de la Marca", config.getNombreApp());
        TextField colorField = new TextField("Color Primario (Hex)", config.getColorPrimario());
        TextField heroTitleField = new TextField("Título Hero", config.getHeroTitulo());
        TextField heroSubField = new TextField("Subtítulo Hero", config.getHeroSubtitulo());

        Button btnGuardar = new Button("Guardar Cambios", e -> {
            // Actualizamos el objeto con los valores de los campos
            config.setNombreApp(nombreField.getValue());
            config.setColorPrimario(colorField.getValue());
            config.setHeroTitulo(heroTitleField.getValue());
            config.setHeroSubtitulo(heroSubField.getValue());
            
            repository.save(config); // Persistencia en BD
            Notification.show("Configuración guardada. Recarga la página para ver los cambios.");
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(nombreField, colorField, heroTitleField, heroSubField);
        card.add(title, form, btnGuardar);
        add(card);
    }
}