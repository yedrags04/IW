package com.example.application.views.main;

import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle("Inicio | Tu Food")
@Route(value = "home", layout = MainLayout.class)
public class HomeView extends VerticalLayout {

    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;

    @Autowired
    public HomeView(ProductoRepository productoRepository, ShoppingCartService cartService) {
        this.productoRepository = productoRepository;
        this.cartService = cartService;

        addClassName("home-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        // 1. HERO SECTION
        add(createHeroSection());

        // 2. CATEGORÍAS
        add(createCategoriesSection());

        // 3. PRODUCTOS DESTACADOS
        add(createFeaturedSection());
    }

    private Section createHeroSection() {
        Section hero = new Section();
        hero.addClassName("hero-section");

        Div content = new Div();
        content.addClassName("hero-content");

        H1 title = new H1("¡Tu comida favorita, a un clic!");
        Paragraph description = new Paragraph("Descubre los mejores sabores de tu ciudad y recíbelos en tiempo récord.");

        Button cta = new Button("Explorar Menú");
        cta.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        cta.addClickListener(e -> UI.getCurrent().navigate("image-gallery")); // Cambiado a tu ruta de productos

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

        categories.add(
            createCategoryItem("Hamburguesas"),
            createCategoryItem("Pizzas"),
            createCategoryItem("Postres"),
            createCategoryItem("Bebidas")
        );

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

        // CAMBIO CLAVE: Usamos un Div con CSS Grid en lugar de HorizontalLayout
        Div productsGrid = new Div();
        productsGrid.setWidthFull();
        productsGrid.addClassName("custom-products-grid");

        // Estilos Inline para el Grid (también puedes ponerlo en tu .css)
        productsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(2, 1fr)") // 2 columnas iguales
            .set("gap", "20px")                             // Espacio entre tarjetas
            .set("max-width", "900px")                     // Ancho máximo del contenedor
            .set("margin", "0 auto")                       // Centrar el grid en la página
            .set("justify-items", "center");               // Centrar tarjetas dentro de su celda

        List<Producto> productos = productoRepository.findAll();
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            ProductosViewCard card = new ProductosViewCard(p, cartService);
            
            // Si es el último elemento y el total es impar...
            if (i == productos.size() - 1 && productos.size() % 2 != 0) {
                // Hacemos que la última tarjeta ocupe las dos columnas y se centre
                card.getStyle().set("grid-column", "1 / span 2");
                card.getStyle().set("justify-self", "center");
            }
            
            productsGrid.add(card);
        }

        section.add(title, productsGrid);
        return section;
    }
}