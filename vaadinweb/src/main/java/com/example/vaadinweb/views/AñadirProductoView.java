package com.example.vaadinweb.views;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.repository.ProductoRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("nuevo-producto")
@AnonymousAllowed
public class AñadirProductoView extends VerticalLayout {

    @Autowired
    public AñadirProductoView(ProductoRepository productoRepository) {
        setPadding(true);
        setSpacing(true);

        TextField nombre = new TextField("Nombre");
        NumberField precio = new NumberField("Precio");
        TextField categoria = new TextField("Categoría");

        Button guardar = new Button("Guardar producto", event -> {
            if (nombre.isEmpty() || precio.isEmpty() || categoria.isEmpty()) {
                Notification.show("Todos los campos son obligatorios", 3000, Notification.Position.MIDDLE);
                return;
            }

            Producto nuevo = new Producto();
            nuevo.setNombre(nombre.getValue());
            nuevo.setPrecio(precio.getValue());
            nuevo.setCategoria(categoria.getValue());

            productoRepository.save(nuevo);

            String sql = String.format(
                "INSERT INTO producto (nombre, precio, categoria) VALUES ('%s', %.2f, '%s');",
                nuevo.getNombre(), nuevo.getPrecio(), nuevo.getCategoria()
            );
            Notification.show("Producto guardado. Añade esto a data.sql:\n" + sql, 5000, Notification.Position.TOP_CENTER);

            nombre.clear();
            precio.clear();
            categoria.clear();
        });

Button volver = new Button("← Volver a productos", event ->
    event.getSource().getUI().ifPresent(ui -> ui.navigate("productos"))
);

volver.getStyle().set("background-color", "#6c757d")
                 .set("color", "white")
                 .set("border-radius", "5px");


        guardar.getStyle().set("background-color", "#28a745")
                          .set("color", "white")
                          .set("border-radius", "5px");

        volver.getStyle().set("background-color", "#6c757d")
                         .set("color", "white")
                         .set("border-radius", "5px");

        FormLayout form = new FormLayout(nombre, precio, categoria, guardar);
        form.setWidth("400px");

        add(form, volver);
    }
}
