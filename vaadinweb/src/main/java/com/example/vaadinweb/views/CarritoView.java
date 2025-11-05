package com.example.vaadinweb.views;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.services.ShoppingCartService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("carrito")
@AnonymousAllowed
public class CarritoView extends VerticalLayout {

    @Autowired
    public CarritoView(ShoppingCartService cartService) {

        // --- ESTILOS GLOBAL ---
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);

        // Título bonito
        H1 titulo = new H1("🛍️ Tu Carrito");
        titulo.getStyle()
                .set("margin-top", "40px")
                .set("color", "var(--lumo-primary-color)");
        add(titulo);

        // Contenedor para los productos
        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("450px");
        itemsLayout.setSpacing(true);
        itemsLayout.setPadding(false);

        // --- Tarjetas de productos ---
        cartService.getCartContents().forEach((Producto p, Integer cantidad) -> {

            HorizontalLayout card = new HorizontalLayout();
            card.setWidthFull();
            card.setAlignItems(Alignment.CENTER);

            // Estética tarjeta
            card.getStyle()
                .set("border-radius", "12px")
                .set("padding", "14px 18px")
                .set("background", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.10)");

            Span nombre = new Span(p.getNombre() + " × " + cantidad);
            nombre.getStyle().set("font-weight", "bold");

            Span precio = new Span(String.format("%.2f €", p.getPrecio() * cantidad));
            precio.getStyle().set("font-size", "1.1em");

            card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            card.add(nombre, precio);

            itemsLayout.add(card);
        });

        add(itemsLayout);

        // Total destacado visualmente
        H3 total = new H3("Total: " + String.format("%.2f €", cartService.getTotalPrice()));
        total.getStyle()
                .set("margin-top", "20px")
                .set("font-weight", "bold")
                .set("font-size", "1.6em");
        add(total);

        // Botón vaciar carrito
        Button clear = new Button("Vaciar Carrito");
        clear.getElement().setAttribute("theme", "error primary");
        clear.getStyle().set("margin-top", "10px");
        clear.addClickListener(e -> {
            cartService.clearCart();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });

        add(clear);
    }
}
