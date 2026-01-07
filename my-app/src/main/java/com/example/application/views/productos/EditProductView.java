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
 * VISTA DE EDICIÓN DE PRODUCTOS
 * Permite modificar un producto existente identificado por su ID.
 * Implementa HasUrlParameter para capturar el ID desde la URL (ej: /edit-product/5).
 */
@PageTitle("Editar Producto | Tu Food")
@Route(value = "edit-product", layout = MainLayout.class)
public class EditProductView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductoRepository repository;
    private Producto producto; // Instancia del producto cargado desde la BD

    // Campos del formulario
    private final TextField nombreField = new TextField("Nombre del Producto");
    private final NumberField precioField = new NumberField("Precio (€)");
    private final TextField categoriaField = new TextField("Categoría");

    // Componentes para la gestión de imagen y vista previa
    private final Image previewImage = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer(); // Almacén temporal para nuevas subidas
    private final Upload upload = new Upload(buffer);
    private final Button btnRemovePhoto = new Button("Eliminar Foto Actual");
    
    // Estados para la lógica de actualización binaria
    private byte[] nuevaImagenBytes = null;
    private boolean eliminarImagenActual = false;

    public EditProductView(ProductoRepository repository, AuthService authService) {
        this.repository = repository;

        // 1. CONFIGURACIÓN DEL LAYOUT
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // SEGURIDAD: Solo administradores pueden editar productos
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductosView.class)));
            return;
        }

        // 2. TARJETA DEL FORMULARIO (Uso de LumoUtility para diseño limpio)
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setMaxWidth("500px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.MEDIUM,
            LumoUtility.Padding.LARGE
        );

        H2 title = new H2("Editar Producto");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.XXLARGE);

        // 3. CONFIGURACIÓN DE IMAGEN Y COMPONENTE UPLOAD
        previewImage.setWidth("200px");
        previewImage.setHeight("200px");
        previewImage.getStyle().set("object-fit", "cover").set("border-radius", "8px");
        previewImage.setVisible(false); // Se muestra solo si hay imagen

        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        
        // Listener: Se dispara cuando se termina de subir una nueva imagen
        upload.addSucceededListener(event -> {
            try {
                // Convertimos el flujo de entrada en un array de bytes
                nuevaImagenBytes = buffer.getInputStream().readAllBytes();
                eliminarImagenActual = false; // Si sube una nueva, cancelamos la orden de borrar
                actualizarVistaPrevia(nuevaImagenBytes);
                Notification.show("Imagen lista para guardar");
            } catch (IOException e) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Botón para marcar la imagen actual para su borrado
        btnRemovePhoto.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnRemovePhoto.setWidthFull();
        btnRemovePhoto.addClickListener(e -> {
            eliminarImagenActual = true;
            nuevaImagenBytes = null;
            previewImage.setVisible(false);
            Notification.show("La foto se eliminará al guardar");
        });

        // 4. FORMULARIO DE TEXTO
        FormLayout form = new FormLayout(nombreField, precioField, categoriaField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();

        // 5. BOTONES DE ACCIÓN
        Button save = new Button("Guardar Cambios", e -> guardar());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidthFull();
        
        Button cancel = new Button("Cancelar", e -> UI.getCurrent().navigate(ProductosView.class));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        // Montaje de los componentes en la tarjeta
        card.add(title, previewImage, upload, btnRemovePhoto, form, save, cancel);
        add(card);
    }

    /**
     * Convierte un array de bytes en un recurso visualizable para el componente Image.
     */
    private void actualizarVistaPrevia(byte[] bytes) {
        StreamResource resource = new StreamResource("temp", () -> new ByteArrayInputStream(bytes));
        previewImage.setSrc(resource);
        previewImage.setVisible(true);
    }

    /**
     * MÉTODO DE CARGA: Se ejecuta al entrar en la vista con un ID en la URL.
     * Busca el producto en el repositorio y rellena los campos.
     */
    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        Optional<Producto> opt = repository.findById(productId);
        if (opt.isPresent()) {
            this.producto = opt.get();
            // Carga de datos textuales
            nombreField.setValue(producto.getNombre());
            precioField.setValue(producto.getPrecio());
            categoriaField.setValue(producto.getCategoria());
            
            // Carga de imagen binaria si existe en la BD
            if (producto.getImagenBlob() != null && producto.getImagenBlob().length > 0) {
                actualizarVistaPrevia(producto.getImagenBlob());
            }
        } else {
            // Si el ID no existe, informamos y redirigimos
            Notification.show("Producto no encontrado").addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.rerouteTo(ProductosView.class);
        }
    }

    /**
     * PERSISTENCIA: Aplica los cambios al objeto producto y lo guarda en la BD.
     */
    private void guardar() {
        if (producto != null) {
            // Validación mínima de campos obligatorios
            if (nombreField.isEmpty() || precioField.getValue() == null) {
                Notification.show("Completa los campos obligatorios").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Sincronización de datos del formulario al objeto modelo
            producto.setNombre(nombreField.getValue());
            producto.setPrecio(precioField.getValue());
            producto.setCategoria(categoriaField.getValue());

            // LÓGICA DE ACTUALIZACIÓN DE IMAGEN:
            // 1. Si el usuario pulsó eliminar.
            // 2. Si el usuario subió una nueva (reemplaza).
            // 3. Si no hizo nada, se mantiene la actual.
            if (eliminarImagenActual) {
                producto.setImagenBlob(null);
            } else if (nuevaImagenBytes != null) {
                producto.setImagenBlob(nuevaImagenBytes);
            }
            
            // Guardado mediante JPA
            repository.save(producto);
            Notification.show("Producto actualizado con éxito").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ProductosView.class); // Volver al catálogo
        }
    }
}