package com.example.vaadinweb.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.Route;

@Route("")
@AnonymousAllowed
public class MainView extends AppLayout {

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("🍔 Comida Rápida");
        title.getStyle().set("margin", "0").set("font-size", "1.5em");

        HorizontalLayout header = new HorizontalLayout(toggle, title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle().set("background", "#f3f3f3");

        addToNavbar(header);

        RouterLink verProductos = new RouterLink();
        verProductos.setText("Ver productos");
        verProductos.setRoute(ProductosView.class);
        verProductos.add(new Icon(VaadinIcon.LIST));

        RouterLink añadirProducto = new RouterLink();
        añadirProducto.setText("Añadir producto");
        añadirProducto.setRoute(AñadirProductoView.class);
        añadirProducto.add(new Icon(VaadinIcon.PLUS));

        VerticalLayout drawerContent = new VerticalLayout(verProductos, añadirProducto);
        drawerContent.setPadding(true);
        drawerContent.setSpacing(true);

        addToDrawer(drawerContent);
    }
}
