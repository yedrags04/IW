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
 * COMPONENTE DE TARJETA DE PRODUCTO
 * Esta clase representa un elemento individual de la lista (ListItem).
 * Implementa una lógica triple: visualización básica, acciones de cliente y gestión de administrador.
 */
public class ProductosViewCard extends ListItem {

    public ProductosViewCard(Producto producto, ShoppingCartService cartService, 
                            AuthService authService, ProductoRepository productoRepository) {
        
        // Configuración de estilos de la tarjeta usando Lumo Utility (Flexbox, padding y bordes redondeados)
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, 
                     AlignItems.START, Padding.MEDIUM, BorderRadius.LARGE);

        // --- SECCIÓN: CONTENEDOR DE IMAGEN ---
        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, 
                         JustifyContent.CENTER, Margin.Bottom.MEDIUM, Overflow.HIDDEN, 
                         BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        Image image = new Image();
        image.setAlt(producto.getNombre());
        image.setWidthFull();
        image.getStyle().set("object-fit", "cover"); // Asegura que la imagen llene el espacio sin deformarse

        /**
         * LÓGICA DE CARGA BINARIA: 
         * Si el producto tiene datos en 'imagenBlob', los convierte en un recurso web.
         * Si no, utiliza una URL de imagen por defecto (placeholder).
         */
        if (producto.getImagenBlob() != null && producto.getImagenBlob().length > 0) {
            StreamResource resource = new StreamResource("img-" + producto.getId(), 
                () -> new ByteArrayInputStream(producto.getImagenBlob()));
            image.setSrc(resource);
        } else {
            // Imagen genérica en caso de que el producto no tenga foto propia
            image.setSrc("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80");
        }
        div.add(image);

        // --- SECCIÓN: DATOS DEL PRODUCTO ---
        Span header = new Span(producto.getNombre());
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);

        Span subtitle = new Span(String.format("%.2f €", producto.getPrecio()));
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        // Se añaden los elementos comunes para todos los tipos de usuario
        add(div, header, subtitle);

        // --- SECCIÓN: LÓGICA DE COMPRA RESTRINGIDA ---
        /**
         * SEGURIDAD A NIVEL DE UI:
         * Solo mostramos el botón de "Pedir" si el usuario ha iniciado sesión.
         * Si es un invitado, se le muestra un mensaje informativo.
         */
        if (authService.isUserLoggedIn()) {
            Button btnPedir = new Button("Pedir", VaadinIcon.PLUS.create());
            btnPedir.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btnPedir.setWidthFull();
            btnPedir.addClickListener(e -> {
                // Añade el producto al servicio de carrito en memoria (Session Scope)
                cartService.addProduct(producto);
                Notification.show(producto.getNombre() + " añadido", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            add(btnPedir);
        } else {
            // Texto de ayuda para invitar al usuario a registrarse o loguearse
            Span loginInfo = new Span("Inicia sesión para pedir");
            loginInfo.addClassNames(FontSize.XXSMALL, TextColor.TERTIARY, Margin.Top.SMALL);
            add(loginInfo);
        }

        // --- SECCIÓN: PANEL DE ADMINISTRADOR ---
        /**
         * PRIVILEGIOS DE ADMIN:
         * Si el usuario logueado tiene el rol ADMIN, se añaden botones para editar o eliminar.
         */
        if (authService.isAdmin()) {
            HorizontalLayout adminButtons = new HorizontalLayout();
            adminButtons.setWidthFull();
            adminButtons.addClassName(Margin.Top.SMALL);
            adminButtons.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.AROUND);

            // Botón Editar: Navega a la vista de edición pasando el ID del producto
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
            editBtn.addClickListener(e -> UI.getCurrent().navigate(EditProductView.class, producto.getId()));

            // Botón Eliminar: Borra el registro de la BD y recarga la vista
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                productoRepository.delete(producto);
                Notification.show("Producto eliminado");
                UI.getCurrent().getPage().reload(); // Recarga para actualizar la galería
            });

            adminButtons.add(editBtn, deleteBtn);
            add(adminButtons);
        }
    }
}