package com.example.application.views;

import com.example.application.security.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.List;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private final AuthService authService;

    public MainLayout(AuthService authService) {
        this.authService = authService;
        
        // El Drawer empuja el contenido y empieza cerrado
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(false); 

        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // --- BOTONES DE ACCIÓN (DERECHA) ---
        
        // Botón para ir al Carrito
        Button cartBtn = new Button(VaadinIcon.CART.create());
        cartBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cartBtn.addClickListener(e -> UI.getCurrent().navigate("carrito"));
        cartBtn.getElement().setAttribute("title", "Ver carrito");

        // Botón para Cerrar Sesión
        Button logoutBtn = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> {
            authService.logout();
            UI.getCurrent().navigate(""); // Redirige a la raíz (Login)
        });

        // Contenedor alineado a la derecha
        HorizontalLayout actionLayout = new HorizontalLayout(cartBtn, logoutBtn);
        actionLayout.addClassNames(LumoUtility.Margin.Left.AUTO);
        actionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionLayout.setSpacing(true);

        Header header = new Header(toggle, viewTitle, actionLayout);
        header.addClassNames(
            LumoUtility.Display.FLEX, 
            LumoUtility.AlignItems.CENTER, 
            LumoUtility.Padding.Horizontal.MEDIUM
        );
        header.setWidthFull();

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        Span appName = new Span("TuFood App");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);
        header.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        
        menuEntries.forEach(entry -> {
            // SOLUCIÓN AL ERROR: Convertimos el IconUrl de String a SvgIcon
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });
        return nav;
    }

    private Footer createFooter() {
        return new Footer();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
        // Cerramos el menú al cambiar de página
        setDrawerOpened(false); 
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}