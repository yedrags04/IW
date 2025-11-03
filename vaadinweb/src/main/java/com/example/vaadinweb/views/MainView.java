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

@Route("")
@AnonymousAllowed
public class MainView extends AppLayout {

    private final Button userButton = new Button(new Icon(VaadinIcon.USER));
    private final Button logoutButton = new Button("Cerrar sesión", new Icon(VaadinIcon.SIGN_OUT));

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("🍔 Comida Rápida");
        title.getStyle()
                .set("margin", "20px auto")
                .set("font-size", "3em")
                .set("font-weight", "bold")
                .set("text-align", "center");

        // 🔹 Verificar si hay usuario en sesión
        Object usuario = VaadinSession.getCurrent().getAttribute("usuario");

        // 🔹 Botón de login
        userButton.getElement().setAttribute("theme", "tertiary-inline");
        userButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

        // 🔹 Botón de logout
        logoutButton.getElement().setAttribute("theme", "tertiary-inline");
        logoutButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("usuario", null); // eliminar de sesión
            Notification.show("👋 Sesión cerrada");
            getUI().ifPresent(ui -> {
                ui.navigate("");     // volver a inicio
                ui.getPage().reload(); // recargar para actualizar el header
            });
        });

        // 🔹 Header dinámico
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout centerTitle = new HorizontalLayout(title);
        centerTitle.setWidthFull();
        centerTitle.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        centerTitle.setAlignItems(FlexComponent.Alignment.CENTER);

        // 🔹 Mostrar botón según estado de sesión
        if (usuario != null) {
            header.add(toggle, centerTitle, logoutButton);
        } else {
            header.add(toggle, centerTitle, userButton);
        }

        header.setFlexGrow(1, centerTitle);
        header.getStyle()
                .set("background", "#f3f3f3")
                .set("padding", "0.5em 1em");

        addToNavbar(header);

        // 🔹 Menú lateral
        RouterLink verProductos = new RouterLink("Ver productos", ProductosView.class);
        RouterLink añadirProducto = new RouterLink("Añadir producto", AñadirProductoView.class);
        añadirProducto.add(new Icon(VaadinIcon.PLUS));

        VerticalLayout drawerContent = new VerticalLayout(verProductos, añadirProducto);
        addToDrawer(drawerContent);

        setDrawerOpened(false);
    }
}
