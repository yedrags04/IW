package com.example.application.views.main; // Corregido para que coincida con la carpeta 'Main'

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon; // Importación genérica de Icon
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Inicio | Tu Food")
@Route(value = "home", layout = MainLayout.class)
public class HomeView extends VerticalLayout {

    public HomeView() {
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
        
        Button cta = new Button("Explorar Menú", VaadinIcon.SEARCH.create());
        cta.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        cta.addClickListener(e -> UI.getCurrent().navigate("productos"));
        cta.addClassName("cta-button");

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

        // Usamos CAKE en lugar de ICE_CREAM para asegurar compatibilidad
        categories.add(
        createCategoryItem("Hamburguesas", VaadinIcon.SHOP.create()),
        createCategoryItem("Pizzas", VaadinIcon.PIE_CHART.create()),
        createCategoryItem("Postres", VaadinIcon.STAR.create()), // Cambiado a STAR para asegurar compilación
        createCategoryItem("Bebidas", VaadinIcon.GLASS.create())
    );

        section.add(title, categories);
        return section;
    }

    private Div createCategoryItem(String name, Icon icon) { // 'Icon' debe ser de com.vaadin.flow.component.icon
        Div item = new Div();
        item.addClassName("category-card");
        
        Span label = new Span(name);
        item.add(icon, label);
        return item;
    }

    private VerticalLayout createFeaturedSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-padding");
        section.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Lo más pedido");
        title.addClassName("section-title");

        HorizontalLayout products = new HorizontalLayout();
        products.setWidthFull();
        products.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        products.addClassName("product-grid");

        products.add(createProductCard("Classic Burger", "12.50€", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=300&h=200&auto=format&fit=crop"));
        products.add(createProductCard("Pizza Pepperoni", "14.00€", "https://images.unsplash.com/photo-1628840042765-356cda07504e?q=80&w=300&h=200&auto=format&fit=crop"));
        products.add(createProductCard("Sushi Mix", "18.20€", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=300&h=200&auto=format&fit=crop"));

        section.add(title, products);
        return section;
    }

    private Div createProductCard(String name, String price, String imgUrl) {
        Div card = new Div();
        card.addClassName("product-card");

        Image img = new Image(imgUrl, name);
        H3 title = new H3(name);
        Span p = new Span(price);
        p.addClassName("price-tag");

        Button addBtn = new Button(VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(p, addBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);

        card.add(img, title, footer);
        return card;
    }
}