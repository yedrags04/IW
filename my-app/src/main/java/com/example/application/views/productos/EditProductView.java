package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@PageTitle("Editar Producto | Tu Food")
@Route(value = "edit-product", layout = MainLayout.class)
public class EditProductView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductoRepository repository;
    private Producto producto;

    private final TextField nombreField = new TextField("Nombre del Producto");
    private final NumberField precioField = new NumberField("Precio (€)");
    private final TextField categoriaField = new TextField("Categoría");

    // Componentes para la gestión de imagen
    private final Image previewImage = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final Button btnRemovePhoto = new Button("Eliminar Foto Actual");
    private byte[] nuevaImagenBytes = null;
    private boolean eliminarImagenActual = false;

    public EditProductView(ProductoRepository repository, AuthService authService) {
        this.repository = repository;

        // 1. Configuración del Layout Principal
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // Seguridad Admin
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductosView.class)));
            return;
        }

        // 2. Tarjeta del Formulario
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setMaxWidth("500px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER); // Centrar contenido de la tarjeta
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.MEDIUM,
            LumoUtility.Padding.LARGE
        );

        H2 title = new H2("Editar Producto");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.XXLARGE);

        // 3. Configuración de Imagen y Upload
        previewImage.setWidth("200px");
        previewImage.setHeight("200px");
        previewImage.getStyle().set("object-fit", "cover").set("border-radius", "8px");
        previewImage.setVisible(false);

        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        upload.addSucceededListener(event -> {
            try {
                nuevaImagenBytes = buffer.getInputStream().readAllBytes();
                eliminarImagenActual = false;
                actualizarVistaPrevia(nuevaImagenBytes);
                Notification.show("Imagen lista para guardar");
            } catch (IOException e) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        btnRemovePhoto.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnRemovePhoto.setWidthFull();
        btnRemovePhoto.addClickListener(e -> {
            eliminarImagenActual = true;
            nuevaImagenBytes = null;
            previewImage.setVisible(false);
            Notification.show("La foto se eliminará al guardar");
        });

        // 4. Formulario de texto
        FormLayout form = new FormLayout(nombreField, precioField, categoriaField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();

        // 5. Botones de Acción
        Button save = new Button("Guardar Cambios", e -> guardar());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidthFull();
        
        Button cancel = new Button("Cancelar", e -> UI.getCurrent().navigate(ProductosView.class));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        // Montaje de la UI
        card.add(title, previewImage, upload, btnRemovePhoto, form, save, cancel);
        add(card);
    }

    private void actualizarVistaPrevia(byte[] bytes) {
        StreamResource resource = new StreamResource("temp", () -> new ByteArrayInputStream(bytes));
        previewImage.setSrc(resource);
        previewImage.setVisible(true);
    }

    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        Optional<Producto> opt = repository.findById(productId);
        if (opt.isPresent()) {
            this.producto = opt.get();
            nombreField.setValue(producto.getNombre());
            precioField.setValue(producto.getPrecio());
            categoriaField.setValue(producto.getCategoria());
            
            // Cargar imagen existente si la hay
            if (producto.getImagenBlob() != null && producto.getImagenBlob().length > 0) {
                actualizarVistaPrevia(producto.getImagenBlob());
            }
        } else {
            Notification.show("Producto no encontrado").addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.rerouteTo(ProductosView.class);
        }
    }

    private void guardar() {
        if (producto != null) {
            if (nombreField.isEmpty() || precioField.getValue() == null) {
                Notification.show("Completa los campos obligatorios").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            producto.setNombre(nombreField.getValue());
            producto.setPrecio(precioField.getValue());
            producto.setCategoria(categoriaField.getValue());

            // Lógica de actualización de imagen
            if (eliminarImagenActual) {
                producto.setImagenBlob(null);
            } else if (nuevaImagenBytes != null) {
                producto.setImagenBlob(nuevaImagenBytes);
            }
            
            repository.save(producto);
            Notification.show("Producto actualizado con éxito").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ProductosView.class);
        }
    }
}