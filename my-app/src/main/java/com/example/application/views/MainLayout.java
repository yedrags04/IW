package com.example.application.views;

import com.example.application.model.AppConfig;
import com.example.application.security.AuthService;
import com.example.application.views.main.HomeView;
import com.example.application.views.mesas.GestionMesasView;
import com.example.application.views.productos.ProductosView;
import com.example.application.views.productos.AddProductView;
import com.example.application.views.tufood.TuFoodView;
import com.example.application.views.perfil.PerfilView;
import com.example.application.views.config.ConfiguracionView;
import com.example.application.views.dardealta.AdminUserRegistrationView;
import com.example.application.views.estadisticas.EstadisticasView;
import com.example.application.views.login.LoginFlipView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.example.application.repository.AppConfigRepository;

/**
 * Layout principal de la aplicación. Define la estructura común (Header y Sidebar).
 * Implementa AfterNavigationObserver para actualizar el título de la página automáticamente.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private final AuthService authService;
    private final String appNameText;

    public MainLayout(AuthService authService, AppConfigRepository configRepo) {
        this.authService = authService;
        
        // Cargar configuración personalizada (Nombre y Color) de la base de datos
        AppConfig config = configRepo.findById(1L).orElse(new AppConfig());
        this.appNameText = config.getNombreApp() != null ? config.getNombreApp() : "TuFood App";

        // Inyección de JavaScript para cambiar el color primario de la App en tiempo real
        UI.getCurrent().getElement().executeJs(
            "document.documentElement.style.setProperty('--lumo-primary-color', $0);", 
            config.getColorPrimario()
        );

        setPrimarySection(Section.DRAWER); // El menú lateral tiene prioridad visual
        addDrawerContent(); // Construir menú lateral
        addHeaderContent(); // Construir barra superior
    }

    /**
     * Crea la barra superior con el título de la vista y botones de acción rápida.
     */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle(); // Botón para abrir/cerrar menú
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Botón de Carrito
        Button cartBtn = new Button(VaadinIcon.CART.create());
        cartBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cartBtn.addClickListener(e -> UI.getCurrent().navigate("carrito"));

        // Botón de Perfil
        Button personBtn = new Button(VaadinIcon.USER.create());
        personBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        personBtn.addClickListener(e -> UI.getCurrent().navigate(PerfilView.class)); 

        // Botón de Logout (Cerrar sesión)
        Button logoutBtn = new Button("Salir", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> confirmarSalida());

        HorizontalLayout actionLayout = new HorizontalLayout(cartBtn, personBtn, logoutBtn);
        actionLayout.addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.AlignItems.CENTER);
        actionLayout.setSpacing(true);

        Header header = new Header(toggle, viewTitle, actionLayout);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.MEDIUM);
        header.setWidthFull();

        addToNavbar(true, header);
    }

    /**
     * Muestra un diálogo de confirmación antes de destruir la sesión del usuario.
     */
    private void confirmarSalida() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cerrar Sesión");
        
        VerticalLayout dialogLayout = new VerticalLayout(new Span("¿Estás seguro de que deseas salir de " + appNameText + "?"));
        dialog.add(dialogLayout);

        Button logoutButton = new Button("Sí", VaadinIcon.SIGN_OUT.create(), e -> {
            authService.logout();
            UI.getCurrent().navigate(LoginFlipView.class);
            dialog.close();
        });
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, logoutButton);
        dialog.open();
    }

    /**
     * Configura el contenido del panel lateral (Logo + Navegación + Footer).
     */
    private void addDrawerContent() {
        Span appName = new Span(this.appNameText);
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);
        header.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        Scroller scroller = new Scroller(createNavigation()); // Permite scroll si hay muchos items
        addToDrawer(header, scroller, createFooter());
    }

    /**
     * Genera el menú de navegación filtrando las vistas según el rol del usuario.
     */
    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Items para todos los usuarios (Público/Clientes)
        nav.addItem(new SideNavItem("Inicio", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));
        nav.addItem(new SideNavItem("Productos", ProductosView.class, LineAwesomeIcon.LIST_SOLID.create()));
        nav.addItem(new SideNavItem("Mi Perfil", PerfilView.class, LineAwesomeIcon.USER_SOLID.create()));
        
        // Vistas exclusivas para ADMINISTRADORES
        if (authService.isAdmin()) {
            nav.addItem(new SideNavItem("Añadir Producto", AddProductView.class, LineAwesomeIcon.PLUS_CIRCLE_SOLID.create()));
            nav.addItem(new SideNavItem("Estadísticas", EstadisticasView.class, LineAwesomeIcon.CHART_BAR_SOLID.create()));
            nav.addItem(new SideNavItem("Alta Usuarios", AdminUserRegistrationView.class, VaadinIcon.PLUS.create()));
            nav.addItem(new SideNavItem("Gestión Mesas", GestionMesasView.class, VaadinIcon.TABLE.create()));
            nav.addItem(new SideNavItem("Gestión Pedidos", TuFoodView.class, LineAwesomeIcon.UTENSILS_SOLID.create()));
            nav.addItem(new SideNavItem("Personalizar Web", ConfiguracionView.class, LineAwesomeIcon.COG_SOLID.create()));
        }

        // Vistas para TRABAJADORES (Camareros/Cocina)
        if (authService.isWorker()) {
            nav.addItem(new SideNavItem("Gestión Mesas", GestionMesasView.class, VaadinIcon.TABLE.create()));
            nav.addItem(new SideNavItem("Gestión Pedidos", TuFoodView.class, LineAwesomeIcon.UTENSILS_SOLID.create()));
        }

        return nav;
    }

    /**
     * Muestra el nombre del usuario logueado en la parte inferior del menú.
     */
    private Footer createFooter() {
        Footer footer = new Footer();
        if (authService.isUserLoggedIn()) {
            Object nameAttr = com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute(AuthService.USERNAME_SESSION_ATTRIBUTE);
            String userName = nameAttr != null ? nameAttr.toString() : "Usuario";
            
            footer.add(new Span("Sesión: " + userName));
            footer.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Padding.MEDIUM, LumoUtility.TextColor.SECONDARY);
        }
        return footer;
    }

    /**
     * Se activa tras cada navegación para actualizar el texto del H1 con el nombre de la vista activa.
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (getContent() != null) {
            viewTitle.setText(MenuConfiguration.getPageHeader(getContent()).orElse("TuFood"));
        }
        setDrawerOpened(false); // Cierra el menú automáticamente en móviles tras navegar
    }
}