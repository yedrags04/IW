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

@PageTitle("Configuración del Sistema | Tu Food")
@Route(value = "configuracion", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ConfiguracionView extends VerticalLayout {

    private final AppConfigRepository repository;
    private AppConfig config;

    public ConfiguracionView(AppConfigRepository repository) {
        this.repository = repository;
        this.config = repository.findById(1L).orElse(new AppConfig());

        addClassName("perfil-view");
        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("perfil-card");
        card.setMaxWidth("600px");

        H2 title = new H2("Personalizar Apariencia");

        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre de la Marca", config.getNombreApp(), "");
        TextField colorField = new TextField("Color Primario (Hexadecimal)", config.getColorPrimario(), "");
        TextField heroTitleField = new TextField("Título Principal (Hero)", config.getHeroTitulo(), "");
        TextField heroSubField = new TextField("Subtítulo Principal", config.getHeroSubtitulo(), "");

        Button btnGuardar = new Button("Guardar Cambios", e -> {
            config.setNombreApp(nombreField.getValue());
            config.setColorPrimario(colorField.getValue());
            config.setHeroTitulo(heroTitleField.getValue());
            config.setHeroSubtitulo(heroSubField.getValue());
            
            repository.save(config);
            Notification.show("Configuración actualizada. Recarga para aplicar cambios.");
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(nombreField, colorField, heroTitleField, heroSubField);
        card.add(title, form, btnGuardar);
        add(card);
    }
}