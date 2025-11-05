package com.example.vaadinweb.views;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.repository.ProductoRepository;
import com.example.vaadinweb.services.ShoppingCartService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

@Route("productos")
@AnonymousAllowed
public class ProductosView extends VerticalLayout {

    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final FlexLayout cardContainer = new FlexLayout();

    @Autowired
    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        
        setPadding(true);
        setSpacing(true);
        setSizeFull();
        
        H3 title = new H3("Nuestra Selección de Comida Rápida 🍔");
        title.getStyle()
             .set("color", "var(--lumo-primary-color-text)")
             .set("margin-bottom", "var(--lumo-space-l)");
             
        add(title, cardContainer);

        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER);
        cardContainer.getStyle()
                     .set("gap", "var(--lumo-space-l)")
                     .set("width", "100%");

        updateProductList();
    }

    private void updateProductList() {
        cardContainer.removeAll();
        List<Producto> productos = productoRepository.findAll();
        
        for (Producto producto : productos) {
            cardContainer.add(createProductCard(producto));
        }
    }

    private Div createProductCard(Producto producto) {

        String currentRole = (String) VaadinSession.getCurrent().getAttribute("userRole");
        boolean isAdmin = "ADMIN".equals(currentRole);
        boolean isLoggedIn = VaadinSession.getCurrent().getAttribute("userName") != null;

        Icon icon = getIconForCategory(producto.getCategoria());
        icon.setSize("60px");
        icon.getStyle().set("color", "var(--lumo-primary-color)");

        H3 name = new H3(producto.getNombre());
        name.getStyle().set("margin", "var(--lumo-space-s) 0");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        Span price = new Span(formatter.format(producto.getPrecio()));
        price.getStyle()
             .set("font-size", "var(--lumo-font-size-xl)")
             .set("font-weight", "var(--lumo-font-weight-bold)")
             .set("color", "var(--lumo-error-color)"); 

        Button addToCartButton = new Button("Añadir al carrito", VaadinIcon.CART.create());
        addToCartButton.getElement().setAttribute("theme", "primary small");
        addToCartButton.setVisible(isLoggedIn);
        addToCartButton.addClickListener(e -> {
            cartService.addProduct(producto);
            Notification.show(producto.getNombre() + " añadido al carrito ✅");
        });

        Button eliminarButton = new Button("Eliminar", new Icon(VaadinIcon.TRASH));
        eliminarButton.getStyle().set("background-color", "var(--lumo-error-color)")
                                 .set("color", "white")
                                 .set("border-radius", "var(--lumo-border-radius-m)");
        
        eliminarButton.addClickListener(event -> {
            productoRepository.delete(producto);
            updateProductList();
            Notification.show("Producto '" + producto.getNombre() + "' eliminado.");
        });
        eliminarButton.setVisible(isAdmin); 

        VerticalLayout cardContent = new VerticalLayout(icon, name, price);
        cardContent.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContent.setSpacing(false);
        cardContent.setPadding(true);

        if (isLoggedIn) cardContent.add(addToCartButton);
        if (isAdmin) cardContent.add(eliminarButton);

        Div card = new Div(cardContent);
        card.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-m)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-m)")
            .set("width", "250px")
            .set("text-align", "center")
            .set("transition", "0.3s");

        return card;
    }

    private Icon getIconForCategory(String categoria) {
        String lowerCategory = categoria.toLowerCase();
        if (lowerCategory.contains("comida") || lowerCategory.contains("hamburguesa")) {
            return new Icon(VaadinIcon.CUTLERY);
        } else if (lowerCategory.contains("bebida") || lowerCategory.contains("cola")) {
            return new Icon(VaadinIcon.GLASS);
        } else if (lowerCategory.contains("patatas")) {
            return new Icon(VaadinIcon.CUBE);
        } else {
            return new Icon(VaadinIcon.PACKAGE); 
        }
    }
}
