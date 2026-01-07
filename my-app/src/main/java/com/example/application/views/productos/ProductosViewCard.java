package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService;
import com.example.application.services.ShoppingCartService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.io.ByteArrayInputStream;

/**
 * Componente visual de tarjeta para un producto.
 */
public class ProductosViewCard extends ListItem {

    public ProductosViewCard(Producto producto, ShoppingCartService cartService, 
                            AuthService authService, ProductoRepository productoRepository) {
        
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, 
                     AlignItems.START, Padding.MEDIUM, BorderRadius.LARGE);

        // Contenedor de imagen con carga desde BLOB o URL genérica
        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, Width.FULL, BorderRadius.MEDIUM, Overflow.HIDDEN);
        div.setHeight("160px");

        Image image = new Image();
        if (producto.getImagenBlob() != null && producto.getImagenBlob().length > 0) {
            StreamResource resource = new StreamResource("img-" + producto.getId(), 
                () -> new ByteArrayInputStream(producto.getImagenBlob()));
            image.setSrc(resource);
        } else {
            image.setSrc("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400");
        }
        image.setWidthFull();
        image.getStyle().set("object-fit", "cover");
        div.add(image);

        Span header = new Span(producto.getNombre());
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);

        Span subtitle = new Span(String.format("%.2f €", producto.getPrecio()));
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        // Botón para añadir al carrito de compras
        Button btnPedir = new Button("Pedir", VaadinIcon.PLUS.create(), e -> {
            cartService.addProduct(producto);
            Notification.show(producto.getNombre() + " añadido").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        btnPedir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnPedir.setWidthFull();

        add(div, header, subtitle, btnPedir);

        // Botones de administración (Editar/Borrar) solo para ADMIN
        if (authService.isAdmin()) {
            HorizontalLayout adminButtons = new HorizontalLayout();
            adminButtons.setWidthFull();
            adminButtons.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.AROUND);

            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> UI.getCurrent().navigate(EditProductView.class, producto.getId()));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);

            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                productoRepository.delete(producto);
                UI.getCurrent().getPage().reload();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

            adminButtons.add(editBtn, deleteBtn);
            add(adminButtons);
        }
    }
}