package com.example.application.views.carrito;

import com.example.application.model.Producto;
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.example.application.views.pago.PagoView; // Asegúrate de importar tu vista de pago
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

@PageTitle("Carrito")
@Route(value = "carrito", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
public class CarritoView extends VerticalLayout {

    public CarritoView(ShoppingCartService cartService) {
        // ... (Configuración inicial se mantiene igual)
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);

        getStyle().set("margin-top", "var(--lumo-size-xl)");
        getStyle().set("padding-left", "4rem");

        H1 titulo = new H1("🛍️ Tu Carrito");
        titulo.addClassNames(LumoUtility.Margin.Vertical.LARGE, LumoUtility.TextColor.PRIMARY);
        add(titulo);

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
                card.getStyle()
                    .set("border-radius", "12px")
                    .set("padding", "1rem 1.5rem")
                    .set("background", "var(--lumo-base-color)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)");

                Span nombre = new Span(p.getNombre());
                nombre.getStyle().set("font-weight", "bold").set("width", "150px");

                HorizontalLayout controles = new HorizontalLayout();
                controles.setAlignItems(Alignment.CENTER);

                Button btnMenos = new Button(cantidad > 1 ? VaadinIcon.MINUS.create() : VaadinIcon.TRASH.create());
                btnMenos.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnMenos.addClickListener(e -> {
                    cartService.removeProduct(p);
                    UI.getCurrent().getPage().reload();
                });

                Span cantText = new Span(cantidad.toString());
                cantText.getStyle().set("font-weight", "bold").set("min-width", "30px").set("text-align", "center");

                Button btnMas = new Button(VaadinIcon.PLUS.create());
                btnMas.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnMas.addClickListener(e -> {
                    cartService.addProduct(p);
                    UI.getCurrent().getPage().reload();
                });

                controles.add(btnMenos, cantText, btnMas);

                Span precio = new Span(String.format("%.2f €", p.getPrecio() * cantidad));
                precio.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SUCCESS);
                precio.getStyle().set("min-width", "80px").set("text-align", "right");

                card.add(nombre, controles, precio);
                itemsLayout.add(card);
            });
        }

        add(itemsLayout);

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
                UI.getCurrent().getPage().reload();
            });

            // --- CORRECCIÓN DE LA REDIRECCIÓN ---
            Button pagar = new Button("Proceder al Pago", VaadinIcon.CREDIT_CARD.create());
            pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            pagar.addClickListener(e -> {
                // Navegación segura usando la clase de la vista
                UI.getCurrent().navigate(PagoView.class);
            });

            buttons.add(clear, pagar);
            add(buttons);
        }
    }
}