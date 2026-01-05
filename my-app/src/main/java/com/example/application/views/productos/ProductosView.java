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
import org.springframework.data.domain.Sort; // Importante para el ordenamiento
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("productos")
@Route(value = "image-gallery", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.TH_LIST_SOLID)
public class ProductosView extends Main implements HasComponents, HasStyle {

    private OrderedList imageContainer;
    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService;

    public ProductosView(ProductoRepository productoRepository, ShoppingCartService cartService, AuthService authService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService;

        getElement().getStyle().set("margin-top", "var(--lumo-size-xl)");
        getElement().getStyle().set("display", "block");

        constructUI();
        // Cargamos inicialmente por defecto (ID descendente suele ser "más nuevos")
        cargarProductos(Sort.by(Sort.Direction.DESC, "id")); 
        addAdminAddProductButton(); 
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

        // --- SELECT DE ORDENACIÓN REAL ---
        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Ordenar por");
        sortBy.setItems("Más nuevos", "Precio: Menor a Mayor", "Precio: Mayor a Menor", "Nombre");
        sortBy.setValue("Más nuevos");

        sortBy.addValueChangeListener(event -> {
            String valor = event.getValue();
            if ("Precio: Menor a Mayor".equals(valor)) {
                cargarProductos(Sort.by(Sort.Direction.ASC, "precio"));
            } else if ("Precio: Mayor a Menor".equals(valor)) {
                cargarProductos(Sort.by(Sort.Direction.DESC, "precio"));
            } else if ("Nombre".equals(valor)) {
                cargarProductos(Sort.by(Sort.Direction.ASC, "nombre"));
            } else {
                cargarProductos(Sort.by(Sort.Direction.DESC, "id"));
            }
        });

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        container.add(headerContainer, sortBy);
        add(container, imageContainer);
    }

    private void cargarProductos(Sort sortOrder) {
        // Usamos el método findAll que acepta un objeto Sort de Spring Data
        List<Producto> productos = productoRepository.findAll(sortOrder);
        imageContainer.removeAll();
        for (Producto p : productos) {
            imageContainer.add(new ProductosViewCard(p, cartService, authService, productoRepository));
        }
    }

    private void addAdminAddProductButton() {
        if (authService.isAdmin()) {
            Button addProductBtn = new Button("Añadir", new Icon(VaadinIcon.PLUS));
            addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            addProductBtn.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("border-radius", "50px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.4)");

            addProductBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("add-product")));
            add(addProductBtn);
        }
    }
}