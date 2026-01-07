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
 * VISTA PARA AÑADIR PRODUCTOS (Panel de Administración)
 * Permite al administrador subir nuevos platos al catálogo, incluyendo
 * la gestión de imágenes que se almacenan como BLOB en la base de datos.
 */
@PageTitle("Añadir Producto | TuFood")
@Route(value = "add-product", layout = MainLayout.class)
public class AddProductView extends VerticalLayout {

    // Variable temporal para guardar los bytes de la imagen subida antes de persistir el objeto
    private byte[] imagenBytes = null; 

    public AddProductView(ProductoRepository productoRepository, AuthService authService) {
        
        // 1. SEGURIDAD: Solo el rol ADMIN puede acceder a esta vista.
        // Si no es admin, redirigimos al Home al intentar adjuntar la vista.
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate("home")));
            return;
        }

        // Configuración estética del contenedor principal (centrado y fondo grisáceo)
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // 2. CONTENEDOR DEL FORMULARIO (Tarjeta visual)
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("600px");
        formContainer.addClassNames("perfil-card", "perfil-card-content");
        formContainer.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 title = new H2("Nuevo Producto");
        title.getStyle().set("color", "var(--lumo-primary-color)");

        // --- GESTIÓN DE SUBIDA DE IMÁGENES ---
        // MemoryBuffer guarda temporalmente el archivo en la memoria RAM del servidor
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png"); // Restricción de formatos
        upload.setMaxFiles(1);

        // Componente de vista previa para que el admin vea la foto antes de guardar
        Image preview = new Image();
        preview.setWidth("150px");
        preview.setHeight("150px");
        preview.setVisible(false); // Oculta hasta que haya una carga exitosa
        preview.getStyle().set("object-fit", "cover").set("border-radius", "10px");

        // Evento que se dispara cuando el archivo se ha subido al servidor correctamente
        upload.addSucceededListener(event -> {
            try {
                // Leemos los bytes directamente del flujo de entrada del buffer
                imagenBytes = buffer.getInputStream().readAllBytes();
                
                // Creamos un StreamResource para poder mostrar el array de bytes en el componente Image
                StreamResource resource = new StreamResource("preview", () -> new ByteArrayInputStream(imagenBytes));
                preview.setSrc(resource);
                preview.setVisible(true);
                
                Notification.show("Imagen cargada correctamente");
            } catch (IOException e) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Limpieza si el usuario decide borrar el archivo seleccionado
        upload.addFileRemovedListener(e -> {
            imagenBytes = null;
            preview.setVisible(false);
        });

        // 3. FORMULARIO DE DATOS
        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre del plato");
        NumberField precioField = new NumberField("Precio (€)");
        TextField categoriaField = new TextField("Categoría (Ej: Hamburguesas)");

        form.add(nombreField, precioField, categoriaField);
        // Configuración responsiva: 1 columna en móviles, 2 en pantallas más grandes
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        // 4. BOTÓN DE GUARDADO
        Button saveBtn = new Button("Guardar en Catálogo", VaadinIcon.DATABASE.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveBtn.setWidthFull();

        saveBtn.addClickListener(e -> {
            // Validación básica de campos obligatorios
            if (nombreField.isEmpty() || precioField.getValue() == null || categoriaField.isEmpty()) {
                Notification.show("Por favor, completa los campos obligatorios", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Mapeo de datos al modelo Producto
            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(nombreField.getValue());
            nuevoProducto.setPrecio(precioField.getValue());
            nuevoProducto.setCategoria(categoriaField.getValue());
            
            // Si hay una imagen cargada en bytes, la asignamos al campo binario del modelo
            if (imagenBytes != null) {
                nuevoProducto.setImagenBlob(imagenBytes);
            }

            // Persistencia en la base de datos mediante el repositorio JPA
            productoRepository.save(nuevoProducto);

            Notification.show("Producto guardado correctamente", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // --- RESETEO DEL FORMULARIO TRAS EL ÉXITO ---
            nombreField.clear();
            precioField.clear();
            categoriaField.clear();
            imagenBytes = null;
            preview.setVisible(false);
            // Ejecutamos JS para limpiar visualmente la lista de archivos del componente Upload
            upload.getElement().executeJs("this.files=[]");
        });

        // Organización final de los componentes dentro de la tarjeta
        formContainer.add(title, new Span("Imagen del producto"), upload, preview, form, saveBtn);
        add(formContainer);
    }
}