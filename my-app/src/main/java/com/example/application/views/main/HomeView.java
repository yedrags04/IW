package com.example.application.views.main;

import com.example.application.model.AppConfig;
import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.security.AuthService;
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
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.example.application.repository.AppConfigRepository;
import java.util.List;
import java.util.Map;

/**
 * PÁGINA DE INICIO (HOME)
 * Esta es la primera pantalla que ven los usuarios. Está dividida en tres secciones:
 * 1. Hero: Llamada a la acción principal.
 * 2. Categorías: Filtrado rápido por tipo de comida.
 * 3. Destacados: Muestra una selección de productos mediante tarjetas.
 */
@PageTitle("Inicio | Tu Food")
@Route(value = "", layout = MainLayout.class) // Ruta raíz de la aplicación
public class HomeView extends VerticalLayout {

    private final ProductoRepository productoRepository;
    private final ShoppingCartService cartService;
    private final AuthService authService;

    public HomeView(ProductoRepository productoRepository, 
                    ShoppingCartService cartService, 
                    AuthService authService, 
                    AppConfigRepository configRepo) {
        
        this.productoRepository = productoRepository;
        this.cartService = cartService;
        this.authService = authService;

        // Carga de la configuración personalizada desde la base de datos
        AppConfig config = configRepo.findById(1L).orElse(new AppConfig());

        // Configuración estética del contenedor principal
        addClassName("home-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        // Construcción por módulos de la página
        add(createHeroSection());
        add(createCategoriesSection());
        add(createFeaturedSection());
    }

    /**
     * SECCIÓN HERO (Banner Principal)
     * Proporciona un impacto visual inmediato y el botón de acceso al menú completo.
     */
    private Section createHeroSection() {
        Section hero = new Section();
        hero.addClassName("hero-section");
        
        Div content = new Div();
        content.addClassName("hero-content");
        
        H1 title = new H1("¡Tu comida favorita, a un clic!");
        Paragraph description = new Paragraph("Descubre los mejores sabores de tu ciudad y recíbelos en tiempo récord.");
        
        Button cta = new Button("Explorar Menú");
        cta.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        // Navegación directa a la galería de productos
        cta.addClickListener(e -> UI.getCurrent().navigate(ProductosView.class));
        
        content.add(title, description, cta);
        hero.add(content);
        return hero;
    }

    /**
     * SECCIÓN DE CATEGORÍAS
     * Obtiene de forma dinámica las categorías que tienen productos asociados en la BD.
     */
    private VerticalLayout createCategoriesSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-padding");
        section.setAlignItems(Alignment.CENTER);
        
        H2 title = new H2("Explora por Categoría");
        title.addClassName("section-title");
        
        HorizontalLayout categoriesContainer = new HorizontalLayout();
        categoriesContainer.setWidthFull();
        categoriesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        categoriesContainer.setSpacing(true);
        categoriesContainer.addClassName("category-container");

        // Obtenemos solo los nombres de categorías que existen en los productos actuales
        List<String> categorias = productoRepository.findDistinctCategorias();
        for (String cat : categorias) {
            categoriesContainer.add(createCategoryItem(cat));
        }

        section.add(title, categoriesContainer);
        return section;
    }

    /**
     * ITEM DE CATEGORÍA
     * Crea una tarjeta interactiva que aplica un filtro al navegar.
     */
    private Div createCategoryItem(String name) {
        Div item = new Div();
        item.addClassName("category-card");
        item.getStyle().set("cursor", "pointer");
        
        Span label = new Span(name);
        item.add(label);

        // LÓGICA DE FILTRADO: Navega a 'image-gallery' enviando el parámetro ?categoria=X
        item.addClickListener(e -> {
            UI.getCurrent().navigate("image-gallery", 
                QueryParameters.simple(Map.of("categoria", name)));
        });
        
        return item;
    }

    /**
     * SECCIÓN DE DESTACADOS
     * Muestra una cuadrícula con los primeros 4 productos de la base de datos.
     */
    private VerticalLayout createFeaturedSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-padding");
        section.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Lo más pedido");
        title.addClassName("section-title");

        // Cuadrícula definida mediante estilos en línea para control total del grid
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

        // Recuperamos los productos y limitamos la vista a 4 elementos
        List<Producto> productos = productoRepository.findAll();
        int limit = Math.min(productos.size(), 4);
        for (int i = 0; i < limit; i++) {
            // Reutilizamos el componente 'ProductosViewCard' para mantener coherencia visual
            productsGrid.add(new ProductosViewCard(productos.get(i), cartService, authService, productoRepository));
        }

        section.add(title, productsGrid);
        return section;
    }
}