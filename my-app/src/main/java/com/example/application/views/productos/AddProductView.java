package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.model.Usuario;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Añadir Producto")
@Route(value = "add-product", layout = MainLayout.class)
public class AddProductView extends VerticalLayout {

    public AddProductView(ProductoRepository productoRepository, AuthService authService) {
        // 1. Seguridad: Si no es ADMIN, redirigir fuera
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate("image-gallery")));
            return;
        }

        // 2. Configuración del layout principal (Ocupa toda la pantalla y centra contenido)
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "20px");

        // 3. Crear un contenedor para el formulario con ancho máximo
        // Esto evita que se pegue a la izquierda cuando el menú lateral se mueve
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("600px");
        formContainer.setPadding(true);
        formContainer.setSpacing(true);
        formContainer.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 title = new H2("Añadir Nuevo Producto");
        title.getStyle().set("margin-top", "0");

        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre");
        NumberField precioField = new NumberField("Precio (€)");
        TextField categoriaField = new TextField("Categoría");

        // Hacer que el formulario sea responsivo (1 columna en móvil, 2 en desktop)
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        form.add(nombreField, precioField, categoriaField);

        // Botones
        Button saveBtn = new Button("Guardar Producto");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setWidthFull();

        Button cancelBtn = new Button("Cancelar", e -> getUI().ifPresent(ui -> ui.navigate("image-gallery")));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setWidthFull();

        saveBtn.addClickListener(e -> {
            if (nombreField.isEmpty() || precioField.isEmpty() || categoriaField.isEmpty()) {
                Notification.show("Todos los campos son obligatorios", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Producto producto = new Producto();
            producto.setNombre(nombreField.getValue());
            producto.setPrecio(precioField.getValue());
            producto.setCategoria(categoriaField.getValue());

            productoRepository.save(producto);

            Notification.show("Producto añadido con éxito", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nombreField.clear();
            precioField.clear();
            categoriaField.clear();
        });

        // 4. Montar la estructura
        formContainer.add(title, form, saveBtn, cancelBtn);
        add(formContainer);
    }
}