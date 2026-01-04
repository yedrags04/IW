package com.example.application.views.main;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService; // Import añadido
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.example.application.views.productos.ProductosView;
import com.example.application.views.productos.ProductosViewCard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


import java.util.List;

@PageTitle("Inicio | Tu Food")
@Route(value = "home", layout = MainLayout.class)
public class HomeView extends VerticalLayout {

    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService; // Añadido para los permisos de las cards

    public HomeView(ProductoRepository productoRepository, ShoppingCartService cartService, AuthService authService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService; // Inicializado

        addClassName("home-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        add(createHeroSection());
        add(createCategoriesSection());
        add(createFeaturedSection());
    }

    // ... createHeroSection y createCategoriesSection se mantienen igual ...
    private Section createHeroSection() {
        Section hero = new Section();
        hero.addClassName("hero-section");
        Div content = new Div();
        content.addClassName("hero-content");
        H1 title = new H1("¡Tu comida favorita, a un clic!");
        Paragraph description = new Paragraph("Descubre los mejores sabores de tu ciudad y recíbelos en tiempo récord.");
        Button cta = new Button("Explorar Menú");
        cta.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        cta.addClickListener(e -> UI.getCurrent().navigate(ProductosView.class));
        content.add(title, description, cta);
        hero.add(content);
        return hero;
    }

    private VerticalLayout createCategoriesSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-padding");
        section.setAlignItems(Alignment.CENTER);
        H2 title = new H2("Explora por Categoría");
        title.addClassName("section-title");
        HorizontalLayout categories = new HorizontalLayout();
        categories.setWidthFull();
        categories.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        categories.setSpacing(true);
        categories.addClassName("category-container");
        categories.add(createCategoryItem("Hamburguesas"), createCategoryItem("Pizzas"), createCategoryItem("Postres"), createCategoryItem("Bebidas"));
        section.add(title, categories);
        return section;
    }

    private Div createCategoryItem(String name) {
        Div item = new Div();
        item.addClassName("category-card");
        Span label = new Span(name);
        item.add(label);
        return item;
    }

    private VerticalLayout createFeaturedSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-padding");
        section.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Lo más pedido");
        title.addClassName("section-title");

        Div productsGrid = new Div();
        productsGrid.setWidthFull();
        productsGrid.addClassName("custom-products-grid");
        productsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(2, 1fr)")
            .set("gap", "20px")
            .set("max-width", "900px")
            .set("margin", "0 auto")
            .set("justify-items", "center");

        List<Producto> productos = productoRepository.findAll();
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            
            // CORRECCIÓN AQUÍ: Ahora pasamos los 4 parámetros requeridos
            ProductosViewCard card = new ProductosViewCard(p, cartService, authService, productoRepository);
            
            if (i == productos.size() - 1 && productos.size() % 2 != 0) {
                card.getStyle().set("grid-column", "1 / span 2");
                card.getStyle().set("justify-self", "center");
            }
            productsGrid.add(card);
        }

        section.add(title, productsGrid);
        return section;
    }
}