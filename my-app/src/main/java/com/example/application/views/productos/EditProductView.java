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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Optional;

@PageTitle("Editar Producto | Tu Food")
@Route(value = "edit-product", layout = MainLayout.class)
public class EditProductView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ProductoRepository repository;
    private Producto producto;

    private final TextField nombreField = new TextField("Nombre del Producto");
    private final NumberField precioField = new NumberField("Precio (€)");
    private final TextField categoriaField = new TextField("Categoría");

    public EditProductView(ProductoRepository repository, AuthService authService) {
        this.repository = repository;

        // 1. Configuración del Layout Principal para CENTRAR
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER); // Centrado vertical
        setAlignItems(Alignment.CENTER);                 // Centrado horizontal
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // Seguridad
        if (!authService.isAdmin()) {
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductosView.class)));
            return;
        }

        // 2. Crear una "Tarjeta" para el formulario
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setMaxWidth("500px"); // Limita el ancho para que no se estire demasiado
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.MEDIUM,
            LumoUtility.Padding.LARGE
        );

        H2 title = new H2("Editar Producto");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.XXLARGE);

        // 3. Formulario
        FormLayout form = new FormLayout(nombreField, precioField, categoriaField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1)); // Siempre 1 columna en esta tarjeta estrecha
        
        nombreField.setWidthFull();
        precioField.setWidthFull();
        categoriaField.setWidthFull();

        // 4. Botones
        Button save = new Button("Guardar Cambios", e -> guardar());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidthFull();
        
        Button cancel = new Button("Cancelar", e -> UI.getCurrent().navigate(ProductosView.class));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        // Montaje
        card.add(title, form, save, cancel);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, Long productId) {
        Optional<Producto> opt = repository.findById(productId);
        if (opt.isPresent()) {
            this.producto = opt.get();
            nombreField.setValue(producto.getNombre());
            precioField.setValue(producto.getPrecio());
            categoriaField.setValue(producto.getCategoria());
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
            
            repository.save(producto);
            Notification.show("Producto actualizado con éxito")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ProductosView.class);
        }
    }
}