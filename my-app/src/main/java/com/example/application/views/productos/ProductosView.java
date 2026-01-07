package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.services.ShoppingCartService;
import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * Galería principal de productos con filtros y ordenación.
 */
@PageTitle("productos")
@Route(value = "image-gallery", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.TH_LIST_SOLID)
public class ProductosView extends Main implements HasComponents, HasStyle, HasUrlParameter<String> {

    private OrderedList imageContainer; // Contenedor dinámico de las tarjetas
    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService;
    
    private String categoriaActiva = null;
    private H2 headerTitle;

    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService, AuthService authService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService;

        getStyle().set("margin-top", "80px"); 
        constructUI();
        addAdminAddProductButton(); 
    }

    /**
     * Captura el parámetro opcional de categoría de la URL.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        List<String> categoriaParams = event.getLocation().getQueryParameters().getParameters().get("categoria");
        
        if (categoriaParams != null && !categoriaParams.isEmpty()) {
            this.categoriaActiva = categoriaParams.get(0);
            headerTitle.setText("Categoría: " + categoriaActiva);
        } else {
            this.categoriaActiva = null;
            headerTitle.setText("Nuestros Productos");
        }
        cargarProductos(Sort.by(Sort.Direction.DESC, "id"));
    }

    private void constructUI() {
        addClassNames("productos-view", MaxWidth.SCREEN_MEDIUM, Margin.Horizontal.AUTO, Padding.Vertical.XLARGE);

        headerTitle = new H2("Nuestros Productos");
        VerticalLayout headerContainer = new VerticalLayout(headerTitle, new Paragraph("Selecciona tus platos favoritos."));
        headerContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Barra de herramientas: Filtro "Ver todo" y Select de ordenación
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Ordenar por");
        sortBy.setItems("Más nuevos", "Precio: Bajo a Alto", "Precio: Alto a Bajo");
        sortBy.addValueChangeListener(e -> aplicarOrden(e.getValue()));

        Button clearFilter = new Button("Ver todo", e -> UI.getCurrent().navigate(ProductosView.class));
        filterLayout.add(clearFilter, sortBy);

        // Cuadrícula Grid (CSS)
        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.LARGE, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        imageContainer.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))");

        add(headerContainer, filterLayout, imageContainer);
    }

    private void aplicarOrden(String criterio) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        if (criterio.contains("Bajo a Alto")) sort = Sort.by(Sort.Direction.ASC, "precio");
        else if (criterio.contains("Alto a Bajo")) sort = Sort.by(Sort.Direction.DESC, "precio");
        cargarProductos(sort);
    }

    private void cargarProductos(Sort sort) {
        imageContainer.removeAll();
        List<Producto> productos = (categoriaActiva != null) 
            ? productoRepository.findByCategoria(categoriaActiva, sort)
            : productoRepository.findAll(sort);

        for (Producto p : productos) {
            imageContainer.add(new ProductosViewCard(p, cartService, authService, productoRepository));
        }
    }

    /**
     * Botón flotante (+) visible solo para administradores.
     */
    private void addAdminAddProductButton() {
        if (authService.isAdmin()) {
            Button addProductBtn = new Button(new Icon(VaadinIcon.PLUS), e -> UI.getCurrent().navigate("add-product"));
            addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            addProductBtn.getStyle().set("position", "fixed").set("bottom", "30px").set("right", "30px").set("border-radius", "50%");
            add(addProductBtn);
        }
    }
}