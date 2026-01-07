package com.example.application.views.carrito;

import com.example.application.model.Producto;
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.example.application.views.pago.PagoView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * Vista del carrito de compras.
 * Permite gestionar los productos seleccionados antes de realizar el pedido.
 */
@PageTitle("Carrito")
@Route(value = "carrito", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
public class CarritoView extends VerticalLayout {

    public CarritoView(ShoppingCartService cartService) {
        // Configuración de layout
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);

        // Estilos personalizados para márgenes
        getStyle().set("margin-top", "var(--lumo-size-xl)");

        H1 titulo = new H1("🛍️ Tu Carrito");
        titulo.addClassNames(LumoUtility.Margin.Vertical.LARGE, LumoUtility.TextColor.PRIMARY);
        add(titulo);

        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("100%");
        itemsLayout.setMaxWidth("750px"); 

        // Comprobamos si el carrito tiene contenido
        if (cartService.getCartContents().isEmpty()) {
            itemsLayout.add(new Span("El carrito está vacío. ¡Explora nuestros productos!"));
        } else {
            // Renderizamos cada producto en una "Card" horizontal
            cartService.getCartContents().forEach((Producto p, Integer cantidad) -> {
                HorizontalLayout card = new HorizontalLayout();
                card.setWidthFull();
                card.setAlignItems(Alignment.CENTER);
                card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                card.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderRadius.MEDIUM);

                Span nombre = new Span(p.getNombre());
                nombre.getStyle().set("font-weight", "bold").set("width", "150px");

                // Controles de cantidad (+ / - / Borrar)
                HorizontalLayout controles = new HorizontalLayout();
                controles.setAlignItems(Alignment.CENTER);

                // Botón de reducción (se convierte en papelera si la cantidad es 1)
                Button btnMenos = new Button(cantidad > 1 ? VaadinIcon.MINUS.create() : VaadinIcon.TRASH.create());
                btnMenos.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                btnMenos.addClickListener(e -> {
                    cartService.removeProduct(p);
                    UI.getCurrent().getPage().reload(); // Recargamos para actualizar totales
                });

                Span cantText = new Span(cantidad.toString());
                cantText.getStyle().set("font-weight", "bold");

                Button btnMas = new Button(VaadinIcon.PLUS.create());
                btnMas.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
                btnMas.addClickListener(e -> {
                    cartService.addProduct(p);
                    UI.getCurrent().getPage().reload();
                });

                controles.add(btnMenos, cantText, btnMas);

                // Cálculo de subtotal por producto
                Span precio = new Span(String.format("%.2f €", p.getPrecio() * cantidad));
                precio.addClassNames(LumoUtility.TextColor.SUCCESS);

                card.add(nombre, controles, precio);
                itemsLayout.add(card);
            });
        }

        add(itemsLayout);

        // Sección de resumen y acciones finales
        if (!cartService.getCartContents().isEmpty()) {
            H3 total = new H3("Total: " + String.format("%.2f €", cartService.getTotalPrice()));
            add(total);

            HorizontalLayout buttons = new HorizontalLayout();
            
            Button clear = new Button("Vaciar Carrito", VaadinIcon.TRASH.create());
            clear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            clear.addClickListener(e -> {
                cartService.clearCart();
                UI.getCurrent().getPage().reload();
            });

            Button pagar = new Button("Proceder al Pago", VaadinIcon.CREDIT_CARD.create());
            pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            pagar.addClickListener(e -> UI.getCurrent().navigate(PagoView.class));

            buttons.add(clear, pagar);
            add(buttons);
        }
    }
}