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
 * Layout principal de la aplicación. 
 * Gestiona la interfaz dinámica: muestra u oculta botones y menús según el estado del AuthService.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private final AuthService authService;
    private final String appNameText;

    public MainLayout(AuthService authService, AppConfigRepository configRepo) {
        this.authService = authService;
        
        // Carga de configuración estética desde BD
        AppConfig config = configRepo.findById(1L).orElse(new AppConfig());
        this.appNameText = config.getNombreApp() != null ? config.getNombreApp() : "TuFood App";

        // Aplicación del color corporativo dinámico
        UI.getCurrent().getElement().executeJs(
            "document.documentElement.style.setProperty('--lumo-primary-color', $0);", 
            config.getColorPrimario()
        );

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    /**
     * Construye la barra superior con lógica de visibilidad para el usuario.
     */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.AlignItems.CENTER);
        actionLayout.setSpacing(true);

        // --- LÓGICA DE BOTONES DINÁMICOS ---
        if (authService.isUserLoggedIn()) {
            // Si hay sesión: Carrito, Perfil y Salir
            Button cartBtn = new Button(VaadinIcon.CART.create(), e -> UI.getCurrent().navigate("carrito"));
            cartBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button personBtn = new Button(VaadinIcon.USER.create(), e -> UI.getCurrent().navigate(PerfilView.class));
            personBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button logoutBtn = new Button("Salir", VaadinIcon.SIGN_OUT.create(), e -> confirmarSalida());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            actionLayout.add(cartBtn, personBtn, logoutBtn);
        } else {
            // Si NO hay sesión: Botón prominente de Iniciar Sesión
            Button loginBtn = new Button("Iniciar sesión", VaadinIcon.SIGN_IN.create(), 
                e -> UI.getCurrent().navigate(LoginFlipView.class));
            loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            actionLayout.add(loginBtn);
        }

        Header header = new Header(toggle, viewTitle, actionLayout);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.MEDIUM);
        header.setWidthFull();

        addToNavbar(true, header);
    }

    /**
     * Diálogo de confirmación para cerrar sesión.
     */
    private void confirmarSalida() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cerrar Sesión");
        
        VerticalLayout dialogLayout = new VerticalLayout(new Span("¿Estás seguro de que deseas salir de " + appNameText + "?"));
        dialog.add(dialogLayout);

        Button logoutButton = new Button("Sí, salir", VaadinIcon.SIGN_OUT.create(), e -> {
            authService.logout();
            dialog.close();
            // Recargamos a la página de login para limpiar el estado de la UI
            UI.getCurrent().getPage().setLocation("login");
        });
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, logoutButton);
        dialog.open();
    }

    private void addDrawerContent() {
        Span appName = new Span(this.appNameText);
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);
        header.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    /**
     * Menú lateral filtrado por autenticación y roles.
     */
    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Items para TODOS (Público)
        nav.addItem(new SideNavItem("Inicio", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));
        nav.addItem(new SideNavItem("Productos", ProductosView.class, LineAwesomeIcon.LIST_SOLID.create()));
        
        // Item Mi Perfil: SOLO si está logueado
        if (authService.isUserLoggedIn()) {
            nav.addItem(new SideNavItem("Mi Perfil", PerfilView.class, LineAwesomeIcon.USER_SOLID.create()));
        }

        // Vistas de ADMINISTRADOR
        if (authService.isAdmin()) {
            nav.addItem(new SideNavItem("Añadir Producto", AddProductView.class, LineAwesomeIcon.PLUS_CIRCLE_SOLID.create()));
            nav.addItem(new SideNavItem("Estadísticas", EstadisticasView.class, LineAwesomeIcon.CHART_BAR_SOLID.create()));
            nav.addItem(new SideNavItem("Alta Usuarios", AdminUserRegistrationView.class, VaadinIcon.PLUS.create()));
            nav.addItem(new SideNavItem("Gestión Mesas", GestionMesasView.class, VaadinIcon.TABLE.create()));
            nav.addItem(new SideNavItem("Gestión Pedidos", TuFoodView.class, LineAwesomeIcon.UTENSILS_SOLID.create()));
            nav.addItem(new SideNavItem("Personalizar Web", ConfiguracionView.class, LineAwesomeIcon.COG_SOLID.create()));
        }

        // Vistas de TRABAJADOR
        if (authService.isWorker()) {
            nav.addItem(new SideNavItem("Gestión Mesas", GestionMesasView.class, VaadinIcon.TABLE.create()));
            nav.addItem(new SideNavItem("Gestión Pedidos", TuFoodView.class, LineAwesomeIcon.UTENSILS_SOLID.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        if (authService.isUserLoggedIn()) {
            String userName = (String) com.vaadin.flow.server.VaadinSession.getCurrent()
                                .getAttribute(AuthService.USERNAME_SESSION_ATTRIBUTE);
            
            footer.add(new Span("Sesión: " + (userName != null ? userName : "Usuario")));
            footer.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Padding.MEDIUM, LumoUtility.TextColor.SECONDARY);
        }
        return footer;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (getContent() != null) {
            viewTitle.setText(MenuConfiguration.getPageHeader(getContent()).orElse("TuFood"));
        }
        setDrawerOpened(false); 
    }
}