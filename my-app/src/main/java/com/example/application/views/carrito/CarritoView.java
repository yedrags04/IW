package com.example.application.views.carrito;

import com.example.application.model.Producto;
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
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

@PageTitle("Carrito")
@Route(value = "carrito", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
public class CarritoView extends VerticalLayout {

    public CarritoView(ShoppingCartService cartService) {

        // --- 1. CONFIGURACIÓN Y POSICIONAMIENTO ---
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);

        // Ajustes para evitar solapamientos con MainLayout
        getStyle().set("margin-top", "var(--lumo-size-xl)");
        getStyle().set("padding-left", "4rem");

        // --- 2. TÍTULO ---
        H1 titulo = new H1("🛍️ Tu Carrito");
        titulo.addClassNames(LumoUtility.Margin.Vertical.LARGE, LumoUtility.TextColor.PRIMARY);
        add(titulo);

        // --- 3. LISTADO DE PRODUCTOS ---
        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("100%");
        itemsLayout.setMaxWidth("750px"); 
        itemsLayout.setPadding(false);

        if (cartService.getCartContents().isEmpty()) {
            itemsLayout.add(new Span("El carrito está vacío. ¡Explora nuestros productos!"));
        } else {
            cartService.getCartContents().forEach((Producto p, Integer cantidad) -> {
                HorizontalLayout card = new HorizontalLayout();
                card.setWidthFull();
                card.setAlignItems(Alignment.CENTER);
                card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                // Estética de la tarjeta de producto
                card.getStyle()
                    .set("border-radius", "12px")
                    .set("padding", "1rem 1.5rem")
                    .set("background", "var(--lumo-base-color)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)");

                // Nombre del producto
                Span nombre = new Span(p.getNombre());
                nombre.getStyle().set("font-weight", "bold").set("width", "150px");

                // --- CONTROLES DE CANTIDAD (DINÁMICOS) ---
                HorizontalLayout controles = new HorizontalLayout();
                controles.setAlignItems(Alignment.CENTER);

                // Botón Menos/Eliminar: Si es la última unidad, mostramos una papelera
                Button btnMenos = new Button(cantidad > 1 ? VaadinIcon.MINUS.create() : VaadinIcon.TRASH.create());
                btnMenos.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnMenos.addClickListener(e -> {
                    cartService.removeProduct(p); // Ahora el método existe en el servicio
                    getUI().ifPresent(ui -> ui.getPage().reload());
                });

                Span cantText = new Span(cantidad.toString());
                cantText.getStyle().set("font-weight", "bold").set("min-width", "30px").set("text-align", "center");

                Button btnMas = new Button(VaadinIcon.PLUS.create());
                btnMas.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnMas.addClickListener(e -> {
                    cartService.addProduct(p);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                });

                controles.add(btnMenos, cantText, btnMas);

                // Subtotal del producto
                Span precio = new Span(String.format("%.2f €", p.getPrecio() * cantidad));
                precio.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SUCCESS);
                precio.getStyle().set("min-width", "80px").set("text-align", "right");

                card.add(nombre, controles, precio);
                itemsLayout.add(card);
            });
        }

        add(itemsLayout);

        // --- 4. RESUMEN Y BOTONES DE ACCIÓN ---
        if (!cartService.getCartContents().isEmpty()) {
            H3 total = new H3("Total: " + String.format("%.2f €", cartService.getTotalPrice()));
            total.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.FontSize.XXLARGE);
            add(total);

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setSpacing(true);

            Button clear = new Button("Vaciar Carrito", VaadinIcon.TRASH.create());
            clear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            clear.addClickListener(e -> {
                cartService.clearCart();
                getUI().ifPresent(ui -> ui.getPage().reload());
            });

            Button pagar = new Button("Proceder al Pago", VaadinIcon.CREDIT_CARD.create());
            pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            pagar.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("credit-card-form")));

            buttons.add(clear, pagar);
            add(buttons);
        }
    }
}