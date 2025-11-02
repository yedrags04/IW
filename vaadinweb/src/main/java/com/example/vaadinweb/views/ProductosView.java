package com.example.vaadinweb.views;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.repository.ProductoRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("productos")
@AnonymousAllowed
public class ProductosView extends VerticalLayout {

    @Autowired
    public ProductosView(ProductoRepository productoRepository) {
        setPadding(true);
        setSpacing(true);

        Grid<Producto> grid = new Grid<>(Producto.class);
        grid.setColumns("id", "nombre", "precio", "categoria");
        grid.setItems(productoRepository.findAll());
        grid.setWidthFull();

        grid.addComponentColumn(producto -> {
            Button eliminar = new Button("Eliminar", event -> {
                productoRepository.delete(producto);
                grid.setItems(productoRepository.findAll());
                Notification.show("Producto eliminado", 3000, Notification.Position.TOP_CENTER);
            });
            eliminar.getStyle().set("background-color", "#dc3545")
                                .set("color", "white")
                                .set("border-radius", "5px");
            return eliminar;
        }).setHeader("Acciones");

        add(grid);
    }
}
