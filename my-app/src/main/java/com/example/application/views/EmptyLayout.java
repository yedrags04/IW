package com.example.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

// RouterLayout permite que esta clase sea usada como layout de otras vistas.
@AnonymousAllowed // Importante para la página de login
public class EmptyLayout extends Div implements RouterLayout {
    // Este layout no hace nada más que servir como contenedor vacío.
}