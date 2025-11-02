package com.example.vaadinweb.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.Route;

@Route("")
@AnonymousAllowed
public class MainView extends AppLayout {

    public MainView() {
        // 🔹 Botón de menú lateral (tres líneas)
        DrawerToggle toggle = new DrawerToggle();

        // 🔹 Título principal centrado y grande
        H1 title = new H1("🍔 Comida Rápida");
        title.getStyle()
                .set("margin", "20px auto")
                .set("font-size", "3em")
                .set("font-weight", "bold")
                .set("text-align", "center")
                .set("line-height", "1.2");

        // 🔹 Botón de usuario (icono Lumo user)
        Button userButton = new Button(new Icon(VaadinIcon.USER));
        userButton.getElement().setAttribute("theme", "tertiary-inline");
        userButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("login"))
        );

        // 🔹 Cabecera superior con tres zonas: izquierda, centro, derecha
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // 🔹 Layout para centrar el título
        HorizontalLayout centerTitle = new HorizontalLayout(title);
        centerTitle.setWidthFull();
        centerTitle.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        centerTitle.setAlignItems(FlexComponent.Alignment.CENTER);

        header.add(toggle, centerTitle, userButton);
        header.setFlexGrow(1, centerTitle);
        header.getStyle()
                .set("background", "#f3f3f3")
                .set("padding", "0.5em 1em");

        addToNavbar(header);

        // 🔹 Enlaces del menú lateral
        RouterLink verProductos = new RouterLink("Ver productos", ProductosView.class);
        RouterLink añadirProducto = new RouterLink("Añadir producto", AñadirProductoView.class);
        añadirProducto.add(new Icon(VaadinIcon.PLUS));

        VerticalLayout drawerContent = new VerticalLayout(verProductos, añadirProducto);
        drawerContent.setPadding(true);
        drawerContent.setSpacing(true);

        addToDrawer(drawerContent);

        // 🔹 El menú lateral empieza cerrado
        setDrawerOpened(false);
    }
}
