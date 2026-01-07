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

/**
 * Permite a los administradores añadir productos a la base de datos.
 */
@PageTitle("Añadir Producto | TuFood")
@Route(value = "add-product", layout = MainLayout.class)
public class AddProductView extends VerticalLayout {

    private byte[] imagenBytes = null; // Almacén temporal de la imagen subida

    public AddProductView(ProductoRepository productoRepository, AuthService authService) {
        // Validación de seguridad manual
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate("home")));
            return;
        }

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("600px");
        formContainer.addClassNames("perfil-card", "perfil-card-content");

        H2 title = new H2("Nuevo Producto");
        title.getStyle().set("color", "var(--lumo-primary-color)");

        // Lógica de subida de archivos (imágenes)
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);

        Image preview = new Image();
        preview.setWidth("150px");
        preview.setHeight("150px");
        preview.setVisible(false);
        preview.getStyle().set("object-fit", "cover").set("border-radius", "10px");

        // Al terminar la subida, convertimos el stream a bytes para la BD
        upload.addSucceededListener(event -> {
            try {
                imagenBytes = buffer.getInputStream().readAllBytes();
                StreamResource resource = new StreamResource("preview", () -> new ByteArrayInputStream(imagenBytes));
                preview.setSrc(resource);
                preview.setVisible(true);
            } catch (IOException e) {
                Notification.show("Error al procesar la imagen").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre del plato");
        NumberField precioField = new NumberField("Precio (€)");
        TextField categoriaField = new TextField("Categoría (Ej: Hamburguesas)");

        form.add(nombreField, precioField, categoriaField);

        Button saveBtn = new Button("Guardar en Catálogo", VaadinIcon.DATABASE.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveBtn.setWidthFull();

        saveBtn.addClickListener(e -> {
            if (nombreField.isEmpty() || precioField.getValue() == null || categoriaField.isEmpty()) {
                Notification.show("Campos incompletos").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(nombreField.getValue());
            nuevoProducto.setPrecio(precioField.getValue());
            nuevoProducto.setCategoria(categoriaField.getValue());
            nuevoProducto.setImagenBlob(imagenBytes); // Se guarda como BLOB en la BD

            productoRepository.save(nuevoProducto);
            Notification.show("Producto guardado correctamente").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Reset del formulario
            nombreField.clear(); precioField.clear(); categoriaField.clear();
            imagenBytes = null; preview.setVisible(false);
            upload.getElement().executeJs("this.files=[]");
        });

        formContainer.add(title, new Span("Imagen del producto"), upload, preview, form, saveBtn);
        add(formContainer);
    }
}