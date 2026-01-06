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
import com.vaadin.flow.theme.lumo.LumoUtility.*;

public class ProductosViewCard extends ListItem {

    public ProductosViewCard(Producto producto, ShoppingCartService cartService, 
                            AuthService authService, ProductoRepository productoRepository) {
        
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, 
                     AlignItems.START, Padding.MEDIUM, BorderRadius.LARGE);

        // Imagen (igual que antes)
        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, 
                         JustifyContent.CENTER, Margin.Bottom.MEDIUM, Overflow.HIDDEN, 
                         BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");
        Image image = new Image("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80", producto.getNombre());
        image.setWidthFull();
        div.add(image);

        Span header = new Span(producto.getNombre());
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);

        Span subtitle = new Span(String.format("%.2f €", producto.getPrecio()));
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);


        // Botón Pedir (Usuario normal)
        Button btnPedir = new Button("Pedir", VaadinIcon.PLUS.create());
        btnPedir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnPedir.setWidthFull();
        btnPedir.addClickListener(e -> {
            cartService.addProduct(producto);
            Notification.show(producto.getNombre() + " añadido", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        add(div, header, subtitle, btnPedir);

        // --- SECCIÓN ADMIN ---
        if (authService.isAdmin()) {
            HorizontalLayout adminButtons = new HorizontalLayout();
            adminButtons.setWidthFull();
            adminButtons.setMargin(true);
            adminButtons.addClassName(Margin.Top.SMALL);

            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
            editBtn.setTooltipText("Editar producto");
            editBtn.addClickListener(e -> {
                // Navegamos pasando el ID del producto
                UI.getCurrent().navigate(EditProductView.class, producto.getId());
            });

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Eliminar producto");
            deleteBtn.addClickListener(e -> {
                productoRepository.delete(producto);
                Notification.show("Producto eliminado");
                // Recargamos la vista para que desaparezca la card
                UI.getCurrent().getPage().reload();
            });

            adminButtons.add(editBtn, deleteBtn);
            add(adminButtons);
        }
    }
}