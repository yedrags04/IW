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

/**
 * Permite modificar un producto existente. Recibe el ID vía URL.
 */
@PageTitle("Editar Producto | Tu Food")
@Route(value = "edit-product", layout = MainLayout.class)
public class EditProductView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductoRepository repository;
    private Producto producto;

    private final TextField nombreField = new TextField("Nombre del Producto");
    private final NumberField precioField = new NumberField("Precio (€)");
    private final TextField categoriaField = new TextField("Categoría");

    private final Image previewImage = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final Button btnRemovePhoto = new Button("Eliminar Foto Actual");
    private byte[] nuevaImagenBytes = null;
    private boolean eliminarImagenActual = false;

    public EditProductView(ProductoRepository repository, AuthService authService) {
        this.repository = repository;

        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductosView.class)));
            return;
        }

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("500px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.MEDIUM);

        previewImage.setWidth("200px");
        previewImage.setHeight("200px");
        previewImage.getStyle().set("object-fit", "cover").set("border-radius", "8px");

        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.addSucceededListener(event -> {
            try {
                nuevaImagenBytes = buffer.getInputStream().readAllBytes();
                eliminarImagenActual = false;
                actualizarVistaPrevia(nuevaImagenBytes);
            } catch (IOException e) {
                Notification.show("Error al cargar imagen");
            }
        });

        btnRemovePhoto.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnRemovePhoto.addClickListener(e -> {
            eliminarImagenActual = true;
            nuevaImagenBytes = null;
            previewImage.setVisible(false);
        });

        FormLayout form = new FormLayout(nombreField, precioField, categoriaField);
        Button save = new Button("Guardar Cambios", e -> guardar());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancel = new Button("Cancelar", e -> UI.getCurrent().navigate(ProductosView.class));

        card.add(new H2("Editar Producto"), previewImage, upload, btnRemovePhoto, form, save, cancel);
        add(card);
    }

    private void actualizarVistaPrevia(byte[] bytes) {
        StreamResource resource = new StreamResource("temp", () -> new ByteArrayInputStream(bytes));
        previewImage.setSrc(resource);
        previewImage.setVisible(true);
    }

    /**
     * Se ejecuta al navegar a la ruta. Busca el producto por ID.
     */
    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        Optional<Producto> opt = repository.findById(productId);
        if (opt.isPresent()) {
            this.producto = opt.get();
            nombreField.setValue(producto.getNombre());
            precioField.setValue(producto.getPrecio());
            categoriaField.setValue(producto.getCategoria());
            
            if (producto.getImagenBlob() != null) {
                actualizarVistaPrevia(producto.getImagenBlob());
            }
        } else {
            event.rerouteTo(ProductosView.class);
        }
    }

    private void guardar() {
        if (producto != null) {
            producto.setNombre(nombreField.getValue());
            producto.setPrecio(precioField.getValue());
            producto.setCategoria(categoriaField.getValue());

            if (eliminarImagenActual) producto.setImagenBlob(null);
            else if (nuevaImagenBytes != null) producto.setImagenBlob(nuevaImagenBytes);
            
            repository.save(producto);
            Notification.show("Producto actualizado");
            UI.getCurrent().navigate(ProductosView.class);
        }
    }
}