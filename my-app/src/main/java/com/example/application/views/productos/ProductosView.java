package com.example.application.views.productos;

import com.example.application.model.Producto; 
import com.example.application.repository.ProductoRepository; 
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
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import java.util.List; // <-- IMPORTAMOS LIST
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("productos")
@Route("image-gallery")
@Menu(order = 3, icon = LineAwesomeIconUrl.TH_LIST_SOLID)
public class ProductosView extends Main implements HasComponents, HasStyle {

    private OrderedList imageContainer;
    private final ProductoRepository productoRepository; // <-- AÑADIDO

    // --- Constructor modificado para INYECTAR el Repositorio ---
    public ProductosView(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository; // <-- AÑADIDO
        
        constructUI();
        cargarProductos(); // <-- AÑADIDO
    }

    // --- Nuevo método para cargar datos de la BD ---
    private void cargarProductos() {
        // 1. Pedimos todos los productos a la BD
        List<Producto> productos = productoRepository.findAll();

        // 2. Limpiamos el contenedor (por si acaso)
        imageContainer.removeAll();

        // 3. Creamos una tarjeta por cada producto y la añadimos
        for (Producto p : productos) {
            imageContainer.add(new ProductosViewCard(p));
        }
    }

    private void constructUI() {
        addClassNames("productos-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        // Cambiamos el título a algo más apropiado
        H2 header = new H2("Nuestros Productos"); 
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE, FontSize.XXXLARGE);
        Paragraph description = new Paragraph("Productos cargados desde la nube");
        description.addClassNames(Margin.Bottom.XLARGE, Margin.Top.NONE, TextColor.SECONDARY);
        headerContainer.add(header, description);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Popularity", "Newest first", "Oldest first");
        sortBy.setValue("Popularity");

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer, sortBy);
        add(container, imageContainer);
    }
}