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

/* * VISTA DEL CARRITO DE COMPRA
 * Muestra los productos seleccionados por el cliente, permite modificar cantidades
 * y calcula el total antes de proceder al pago.
 */
@PageTitle("Carrito")
@Route(value = "carrito", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
public class CarritoView extends VerticalLayout {

    public CarritoView(ShoppingCartService cartService) {
        // Configuración visual del contenedor principal
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);

        H1 titulo = new H1("🛍️ Tu Carrito");
        titulo.addClassNames(LumoUtility.Margin.Vertical.LARGE, LumoUtility.TextColor.PRIMARY);
        add(titulo);

        // Contenedor donde se listarán los productos
        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.setWidth("100%");
        itemsLayout.setMaxWidth("750px"); 

        // Lógica condicional: Si el carrito está vacío, mostramos un mensaje informativo
        if (cartService.getCartContents().isEmpty()) {
            itemsLayout.add(new Span("El carrito está vacío. ¡Explora nuestros productos!"));
        } else {
            // Iteramos sobre el contenido del carrito (Mapa de Producto -> Cantidad)
            cartService.getCartContents().forEach((Producto p, Integer cantidad) -> {
                HorizontalLayout card = new HorizontalLayout();
                card.setWidthFull();
                card.setAlignItems(Alignment.CENTER);
                card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                card.addClassNames("perfil-card"); // Aplicamos el estilo de tarjeta del sistema

                Span nombre = new Span(p.getNombre());
                nombre.getStyle().set("font-weight", "bold");

                // CONTROLES DE CANTIDAD (+ / -)
                HorizontalLayout controles = new HorizontalLayout();
                
                // Botón para reducir cantidad o eliminar si es el último
                Button btnMenos = new Button(cantidad > 1 ? VaadinIcon.MINUS.create() : VaadinIcon.TRASH.create());
                btnMenos.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                btnMenos.addClickListener(e -> {
                    cartService.removeProduct(p);
                    UI.getCurrent().getPage().reload(); // Recargamos para refrescar totales
                });

                Span cantText = new Span(cantidad.toString());
                
                // Botón para incrementar cantidad
                Button btnMas = new Button(VaadinIcon.PLUS.create());
                btnMas.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
                btnMas.addClickListener(e -> {
                    cartService.addProduct(p);
                    UI.getCurrent().getPage().reload();
                });

                controles.add(btnMenos, cantText, btnMas);

                // Cálculo del subtotal por producto
                Span precio = new Span(String.format("%.2f €", p.getPrecio() * cantidad));
                precio.addClassNames(LumoUtility.TextColor.SUCCESS);

                card.add(nombre, controles, precio);
                itemsLayout.add(card);
            });
        }

        add(itemsLayout);

        // SECCIÓN DE FINALIZACIÓN DE COMPRA
        if (!cartService.getCartContents().isEmpty()) {
            H3 total = new H3("Total: " + String.format("%.2f €", cartService.getTotalPrice()));
            add(total);

            HorizontalLayout buttons = new HorizontalLayout();

            // Opción para limpiar toda la sesión del carrito
            Button clear = new Button("Vaciar Carrito", VaadinIcon.TRASH.create());
            clear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            clear.addClickListener(e -> {
                cartService.clearCart();
                UI.getCurrent().getPage().reload();
            });

            // Botón de navegación hacia la pasarela de pago
            Button pagar = new Button("Proceder al Pago", VaadinIcon.CREDIT_CARD.create());
            pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            pagar.addClickListener(e -> UI.getCurrent().navigate(PagoView.class));

            buttons.add(clear, pagar);
            add(buttons);
        }
    }
}