package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@PageTitle("Añadir Producto | TuFood")
@Route(value = "add-product", layout = MainLayout.class)
public class AddProductView extends VerticalLayout {

    private byte[] imagenBytes = null; // Para almacenar los bytes de la imagen

    public AddProductView(ProductoRepository productoRepository, AuthService authService) {
        // 1. Seguridad
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate("home")));
            return;
        }

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // 2. Contenedor del Formulario
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("600px");
        formContainer.addClassNames("perfil-card", "perfil-card-content");
        formContainer.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 title = new H2("Nuevo Producto");
        title.getStyle().set("color", "var(--lumo-primary-color)");

        // --- GESTIÓN DE SUBIDA (BUFFER A BYTES) ---
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);

        Image preview = new Image();
        preview.setWidth("150px");
        preview.setHeight("150px");
        preview.setVisible(false);
        preview.getStyle().set("object-fit", "cover").set("border-radius", "10px");

        upload.addSucceededListener(event -> {
            try {
                // Leemos los bytes directamente del buffer
                imagenBytes = buffer.getInputStream().readAllBytes();
                
                // Mostramos la vista previa usando StreamResource
                StreamResource resource = new StreamResource("preview", () -> new ByteArrayInputStream(imagenBytes));
                preview.setSrc(resource);
                preview.setVisible(true);
                
                Notification.show("Imagen cargada correctamente");
            } catch (IOException e) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRemovedListener(e -> {
            imagenBytes = null;
            preview.setVisible(false);
        });

        // 3. Formulario
        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre del plato");
        NumberField precioField = new NumberField("Precio (€)");
        TextField categoriaField = new TextField("Categoría (Ej: Hamburguesas)");

        form.add(nombreField, precioField, categoriaField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        // 4. Botones de Acción
        Button saveBtn = new Button("Guardar en Catálogo", VaadinIcon.DATABASE.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveBtn.setWidthFull();

        saveBtn.addClickListener(e -> {
            if (nombreField.isEmpty() || precioField.getValue() == null || categoriaField.isEmpty()) {
                Notification.show("Por favor, completa los campos obligatorios", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(nombreField.getValue());
            nuevoProducto.setPrecio(precioField.getValue());
            nuevoProducto.setCategoria(categoriaField.getValue());
            
            // Asignamos el array de bytes al campo binario
            if (imagenBytes != null) {
                nuevoProducto.setImagenBlob(imagenBytes);
            }

            productoRepository.save(nuevoProducto);

            Notification.show("Producto guardado correctamente", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Resetear todo
            nombreField.clear();
            precioField.clear();
            categoriaField.clear();
            imagenBytes = null;
            preview.setVisible(false);
            upload.getElement().executeJs("this.files=[]");
        });

        formContainer.add(title, new Span("Imagen del producto"), upload, preview, form, saveBtn);
        add(formContainer);
    }
}