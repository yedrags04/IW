package com.example.application.views.productos;

import com.example.application.model.Producto; 
import com.example.application.repository.ProductoRepository; 
import com.example.application.services.ShoppingCartService; // <-- IMPORTANTE
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
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
    private final ShoppingCartService cartService; // <-- AÑADIDO

    // --- Constructor con Inyección de Repositorio y Servicio de Carrito ---
    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService; // <-- AÑADIDO
        
        // EMPUJE SUPERIOR PARA VISTA "MAIN" (Ajustado para no solaparse)
        getElement().getStyle().set("margin-top", "var(--lumo-size-xl)");
        getElement().getStyle().set("display", "block");

        constructUI();
        cargarProductos();
    }

    private void cargarProductos() {
        List<Producto> productos = productoRepository.findAll();
        imageContainer.removeAll();
        for (Producto p : productos) {
            // Pasamos tanto el producto como el servicio a la tarjeta
            imageContainer.add(new ProductosViewCard(p, cartService));
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
}