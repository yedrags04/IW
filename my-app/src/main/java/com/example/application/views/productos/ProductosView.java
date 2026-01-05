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

@PageTitle("productos")
@Route(value = "image-gallery", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.TH_LIST_SOLID)
public class ProductosView extends Main implements HasComponents, HasStyle, HasUrlParameter<String> {

    private OrderedList imageContainer;
    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService;
    
    private String categoriaActiva = null;
    private H2 headerTitle;

    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService, AuthService authService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService;

        // Ajuste para evitar que el banner oculte el título
        getStyle().set("margin-top", "80px"); 
        getStyle().set("display", "block");

        constructUI();
        addAdminAddProductButton(); 
    }

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
        addClassNames("productos-view", MaxWidth.SCREEN_MEDIUM, Margin.Horizontal.AUTO, 
                     Padding.Bottom.LARGE, Padding.Horizontal.LARGE, Padding.Top.XLARGE);

        // --- CABECERA CENTRADA ---
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.setPadding(false);
        headerContainer.setAlignItems(FlexComponent.Alignment.CENTER); 
        headerContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        headerTitle = new H2("Nuestros Productos");
        headerTitle.addClassNames(Margin.Bottom.NONE, Margin.Top.NONE, FontSize.XXXLARGE);
        
        Paragraph description = new Paragraph("Selecciona tus platos favoritos.");
        description.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.NONE, TextColor.SECONDARY);
        headerContainer.add(headerTitle, description);

        // --- FILTROS ALINEADOS A LA DERECHA ---
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        // CAMBIO AQUÍ: Alineamos el contenido al final (derecha)
        filterLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        filterLayout.addClassNames(Margin.Bottom.LARGE);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Ordenar por");
        sortBy.setItems("Más nuevos", "Precio: Bajo a Alto", "Precio: Alto a Bajo");
        sortBy.setValue("Más nuevos");
        sortBy.addValueChangeListener(e -> aplicarOrden(e.getValue()));

        Button clearFilter = new Button("Ver todo", e -> UI.getCurrent().navigate(ProductosView.class));
        clearFilter.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        filterLayout.add(clearFilter, sortBy);

        // --- GRID DE PRODUCTOS ---
        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.LARGE, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        
        imageContainer.getStyle()
            .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 320px))")
            .set("justify-content", "center")
            .set("max-width", "1100px")
            .set("margin", "0 auto");

        add(headerContainer, filterLayout, imageContainer);
    }

    private void aplicarOrden(String criterio) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        if (criterio.equals("Precio: Bajo a Alto")) sort = Sort.by(Sort.Direction.ASC, "precio");
        if (criterio.equals("Precio: Alto a Bajo")) sort = Sort.by(Sort.Direction.DESC, "precio");
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

    private void addAdminAddProductButton() {
        if (authService.isAdmin()) {
            Button addProductBtn = new Button(new Icon(VaadinIcon.PLUS));
            addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            addProductBtn.getStyle()
                .set("position", "fixed")
                .set("bottom", "30px")
                .set("right", "30px")
                .set("z-index", "1000")
                .set("border-radius", "50%")
                .set("width", "60px")
                .set("height", "60px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.3)");
            
            addProductBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("add-product")));
            add(addProductBtn);
        }
    }
}