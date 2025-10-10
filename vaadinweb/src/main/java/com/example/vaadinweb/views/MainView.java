package com.example.vaadinweb.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
public class MainView extends VerticalLayout {

    public MainView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("🍔 Bienvenido a Comida Rápida");

        Button boton = new Button("Haz tu pedido", e ->
            Notification.show("Esta es la vista principal, pronto podrás hacer pedidos 🍟")
        );

        add(title, boton);
    }
}
