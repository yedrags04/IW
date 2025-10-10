package com.example.vaadinweb.views;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.repository.ProductoRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@AnonymousAllowed
public class MainView extends VerticalLayout {

    private final ProductoRepository productoRepository;

    @Autowired
    public MainView(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;

        // Diseño original
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("🍔 Bienvenido a Comida Rápida");

        Button boton = new Button("Haz tu pedido", e ->
            Notification.show("Esta es la vista principal, pronto podrás hacer pedidos 🍟")
        );

        // Nueva sección: tabla de productos
        Grid<Producto> grid = new Grid<>(Producto.class);
        grid.setColumns("id", "nombre", "precio", "categoria");
        grid.setItems(productoRepository.findAll());
        grid.setWidth("80%");

        // Añadir todo a la vista
        add(title, boton, grid);
    }
}
