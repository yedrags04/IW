package com.example.vaadinweb.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.UI;

@Route("")
@AnonymousAllowed
public class MainView extends AppLayout {

    public MainView() {

        String currentRole = (String) VaadinSession.getCurrent().getAttribute("userRole");
        boolean isAdmin = "ADMIN".equals(currentRole);
        boolean isLoggedIn = VaadinSession.getCurrent().getAttribute("userName") != null; 
        
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        H1 title = new H1("🍔 Comida Rápida");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        // Botón Carrito — ahora con estilo primario y sombra
        Button cartButton = new Button("Carrito", new Icon(VaadinIcon.CART));
        cartButton.getElement().setAttribute("theme", "primary");
        cartButton.setVisible(isLoggedIn);
        cartButton.addClickListener(e -> UI.getCurrent().navigate("carrito"));

        Button loginButton = new Button("Iniciar Sesión", new Icon(VaadinIcon.SIGN_IN));
        loginButton.getElement().setAttribute("theme", "primary");
        loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));

        Button logoutButton = new Button("Cerrar Sesión", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.getElement().setAttribute("theme", "tertiary contrast");
        logoutButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("userName", null);
            VaadinSession.getCurrent().setAttribute("userRole", null);
            Notification.show("👋 Sesión cerrada");
            UI.getCurrent().getPage().reload();
        });

        // 🔥 Contenedor de los botones de la derecha
        HorizontalLayout rightControls = new HorizontalLayout();
        rightControls.setSpacing(true);
        rightControls.setAlignItems(FlexComponent.Alignment.CENTER);

        if (isLoggedIn) {
            rightControls.add(cartButton, logoutButton);
        } else {
            rightControls.add(loginButton);
        }

        // Header final con título centrado REAL
        HorizontalLayout header = new HorizontalLayout(toggle, title, rightControls);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title); // <— CLAVE: Esto mantiene el título centrado SIEMPRE
        header.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        addToNavbar(header);

        RouterLink verProductos = new RouterLink("Ver productos", ProductosView.class);
        verProductos.getElement().setAttribute("theme", "menu-item");

        VerticalLayout drawerContent = new VerticalLayout(verProductos);
        drawerContent.setPadding(true);

        if (isAdmin) {
            RouterLink añadirProducto = new RouterLink("Añadir producto", AñadirProductoView.class);
            añadirProducto.add(new Icon(VaadinIcon.PLUS_CIRCLE));
            drawerContent.add(añadirProducto);
        }

        addToDrawer(drawerContent);
        setDrawerOpened(false);
    }
}
