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
 * LAYOUT PRINCIPAL (Contenedor Maestro)
 * Esta clase define la estructura global de la aplicación: la barra superior (Navbar)
 * y el menú lateral (Drawer). Implementa AfterNavigationObserver para actualizar
 * el título de la cabecera automáticamente al navegar.
 */
@Layout
@AnonymousAllowed // Permite que el layout se renderice incluso sin login (para mostrar el Home público)
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle; // Texto que muestra el nombre de la vista actual
    private final AuthService authService; // Servicio para comprobar roles y sesión
    private final String appNameText; // Nombre de la aplicación cargado de BD

    public MainLayout(AuthService authService, AppConfigRepository configRepo) {
        this.authService = authService;
        
        // 1. CARGA DE CONFIGURACIÓN: Obtenemos el nombre y colores personalizados de la BD
        AppConfig config = configRepo.findById(1L).orElse(new AppConfig());
        this.appNameText = config.getNombreApp() != null ? config.getNombreApp() : "TuFood App";

        // 2. ESTILO DINÁMICO: Inyectamos el color primario elegido por el usuario en el CSS de la página
        UI.getCurrent().getElement().executeJs(
            "document.documentElement.style.setProperty('--lumo-primary-color', $0);", 
            config.getColorPrimario()
        );

        // 3. ESTRUCTURA: Definimos que la sección primaria es el menú lateral (Drawer)
        setPrimarySection(Section.DRAWER);
        addDrawerContent(); // Crea el menú lateral
        addHeaderContent(); // Crea la barra superior
    }

    /**
     * CONSTRUCCIÓN DE LA BARRA SUPERIOR (NAVBAR)
     * Gestiona la visibilidad de los botones de Carrito, Perfil y Login/Logout.
     */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle(); // Botón para abrir/cerrar menú en móviles
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Layout para los botones alineados a la derecha
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.AlignItems.CENTER);
        actionLayout.setSpacing(true);

        // --- LÓGICA DE BOTONES SEGÚN SESIÓN ---
        if (authService.isUserLoggedIn()) {
            // Caso: Usuario Logueado
            Button cartBtn = new Button(VaadinIcon.CART.create(), e -> UI.getCurrent().navigate("carrito"));
            cartBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button personBtn = new Button(VaadinIcon.USER.create(), e -> UI.getCurrent().navigate(PerfilView.class));
            personBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button logoutBtn = new Button("Salir", VaadinIcon.SIGN_OUT.create(), e -> confirmarSalida());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            actionLayout.add(cartBtn, personBtn, logoutBtn);
        } else {
            // Caso: Usuario Invitado (se muestra botón de Iniciar Sesión)
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
     * DIÁLOGO DE CERRAR SESIÓN
     * Muestra una ventana emergente para confirmar que el usuario quiere salir.
     */
    private void confirmarSalida() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cerrar Sesión");
        
        VerticalLayout dialogLayout = new VerticalLayout(new Span("¿Estás seguro de que deseas salir de " + appNameText + "?"));
        dialog.add(dialogLayout);

        Button logoutButton = new Button("Sí, salir", VaadinIcon.SIGN_OUT.create(), e -> {
            authService.logout(); // Destruye la sesión en el servidor
            dialog.close();
            // IMPORTANTE: Refresca la URL para limpiar el estado de la UI y los menús
            UI.getCurrent().getPage().setLocation("login");
        });
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, logoutButton);
        dialog.open();
    }

    /**
     * CONSTRUCCIÓN DEL MENÚ LATERAL (DRAWER)
     */
    private void addDrawerContent() {
        Span appName = new Span(this.appNameText);
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);
        header.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        // Scroller permite que el menú tenga barra de desplazamiento si hay muchos items
        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    /**
     * GENERACIÓN DINÁMICA DE RUTAS (Filtro por Roles)
     * Decide qué opciones de menú mostrar basándose en si el usuario es Admin, Trabajador o Cliente.
     */
    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // 1. Acceso Público: Siempre visibles
        nav.addItem(new SideNavItem("Inicio", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));
        nav.addItem(new SideNavItem("Productos", ProductosView.class, LineAwesomeIcon.LIST_SOLID.create()));
        
        // 2. Acceso Registrado: Solo si el usuario inició sesión
        if (authService.isUserLoggedIn()) {
            nav.addItem(new SideNavItem("Mi Perfil", PerfilView.class, LineAwesomeIcon.USER_SOLID.create()));
        }

        // 3. Acceso ADMINISTRADOR: Gestión completa del sistema
        if (authService.isAdmin()) {
            nav.addItem(new SideNavItem("Añadir Producto", AddProductView.class, LineAwesomeIcon.PLUS_CIRCLE_SOLID.create()));
            nav.addItem(new SideNavItem("Estadísticas", EstadisticasView.class, LineAwesomeIcon.CHART_BAR_SOLID.create()));
            nav.addItem(new SideNavItem("Alta Usuarios", AdminUserRegistrationView.class, VaadinIcon.PLUS.create()));
            nav.addItem(new SideNavItem("Personalizar Web", ConfiguracionView.class, LineAwesomeIcon.COG_SOLID.create()));
        }

        // 4. Acceso TRABAJADOR: Operaciones de sala y cocina
        if (authService.isWorker()) {
            nav.addItem(new SideNavItem("Gestión Mesas", GestionMesasView.class, VaadinIcon.TABLE.create()));
            nav.addItem(new SideNavItem("Gestión Pedidos", TuFoodView.class, LineAwesomeIcon.UTENSILS_SOLID.create()));
        }

        return nav;
    }

    /**
     * PIE DEL MENÚ LATERAL
     * Muestra el nombre del usuario conectado actualmente.
     */
    private Footer createFooter() {
        Footer footer = new Footer();
        if (authService.isUserLoggedIn()) {
            // Recuperamos el nombre directamente de la sesión de Vaadin
            String userName = (String) com.vaadin.flow.server.VaadinSession.getCurrent()
                                .getAttribute(AuthService.USERNAME_SESSION_ATTRIBUTE);
            
            footer.add(new Span("Sesión: " + (userName != null ? userName : "Usuario")));
            footer.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Padding.MEDIUM, LumoUtility.TextColor.SECONDARY);
        }
        return footer;
    }

    /**
     * EVENTO POST-NAVEGACIÓN
     * Se dispara cada vez que cambiamos de vista para actualizar el título del Navbar.
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (getContent() != null) {
            // Obtiene el título definido en @PageTitle de la vista actual
            viewTitle.setText(MenuConfiguration.getPageHeader(getContent()).orElse("TuFood"));
        }
        setDrawerOpened(false); // Cierra el menú lateral automáticamente en móviles tras elegir una opción
    }
}