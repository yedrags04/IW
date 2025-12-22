package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.services.ShoppingCartService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

public class ProductosViewCard extends ListItem {

    // Añadimos el servicio como parámetro en el constructor
    public ProductosViewCard(Producto producto, ShoppingCartService cartService) {
        // Estilo del contenedor de la tarjeta
        addClassNames(
            Background.CONTRAST_5, 
            Display.FLEX, 
            FlexDirection.COLUMN, 
            AlignItems.START, 
            Padding.MEDIUM,
            BorderRadius.LARGE
        );

        // Contenedor de la imagen
        Div div = new Div();
        div.addClassNames(
            Background.CONTRAST, 
            Display.FLEX, 
            AlignItems.CENTER, 
            JustifyContent.CENTER,
            Margin.Bottom.MEDIUM, 
            Overflow.HIDDEN, 
            BorderRadius.MEDIUM, 
            Width.FULL
        );
        div.setHeight("160px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80");
        image.setAlt(producto.getNombre());
        div.add(image);

        // Nombre y Precio
        Span header = new Span(producto.getNombre());
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);

        Span subtitle = new Span(String.format("%.2f €", producto.getPrecio()));
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        Paragraph description = new Paragraph("Producto fresco de alta calidad preparado al momento.");
        description.addClassNames(Margin.Vertical.MEDIUM, FontSize.SMALL);

        // --- BOTÓN DE PEDIR (Sustituye al badge estático) ---
        Button btnPedir = new Button("Pedir", VaadinIcon.PLUS.create());
        btnPedir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnPedir.setWidthFull();
        btnPedir.getStyle().set("margin-top", "auto"); // Empuja el botón al final de la tarjeta

        btnPedir.addClickListener(e -> {
            cartService.addProduct(producto);
            Notification notification = Notification.show(
                producto.getNombre() + " añadido al carrito", 
                3000, 
                Notification.Position.BOTTOM_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        // Ensamblaje de la tarjeta
        add(div, header, subtitle, description, btnPedir);
    }
}