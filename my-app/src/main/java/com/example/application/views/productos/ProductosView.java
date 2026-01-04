package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.services.ShoppingCartService;
import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("productos")
@Route(value = "image-gallery", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.TH_LIST_SOLID)
public class ProductosView extends Main implements HasComponents, HasStyle {

    private OrderedList imageContainer;
    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService; // Añadido

    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService, AuthService authService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService; // Inicializado

        // Ajustes de margen superior
        getElement().getStyle().set("margin-top", "var(--lumo-size-xl)");
        getElement().getStyle().set("display", "block");

        constructUI();
        cargarProductos();
        addAdminAddProductButton(); 
    }

    private void cargarProductos() {
        List<Producto> productos = productoRepository.findAll();
        imageContainer.removeAll();
// Dentro del bucle for en ProductosView
for (Producto p : productos) {
    imageContainer.add(new ProductosViewCard(p, cartService, authService, productoRepository));
}
    }

    private void constructUI() {
        addClassNames("productos-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, 
                     Padding.Bottom.LARGE, Padding.Horizontal.LARGE, Padding.Top.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.setPadding(false);
        
        H2 header = new H2("Nuestros Productos"); 
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.XXXLARGE);
        
        Paragraph description = new Paragraph("Selecciona tus platos favoritos y añádelos al carrito.");
        description.addClassNames(Margin.Bottom.XLARGE, Margin.Top.NONE, TextColor.SECONDARY);
        headerContainer.add(header, description);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Ordenar por");
        sortBy.setItems("Popularidad", "Más nuevos", "Precio");
        sortBy.setValue("Popularidad");

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer, sortBy);
        add(container, imageContainer);
    }

    private void addAdminAddProductButton() {
        // Usamos el método isAdmin() que ya definiste en tu AuthService
        if (authService.isAdmin()) {
            System.out.println("DEBUG: El usuario es ADMIN. Generando botón...");

            Button addProductBtn = new Button("Añadir", new Icon(VaadinIcon.PLUS));
            addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

            // Posicionamiento fijo abajo a la derecha
            addProductBtn.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("border-radius", "50px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.4)");

            addProductBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("add-product")));

            add(addProductBtn);
        } else {
            System.out.println("DEBUG: Botón oculto. isAdmin() devolvió false.");
        }
    }
}