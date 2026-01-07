package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * CONFIGURACIÓN DEL SHELL DE LA APLICACIÓN
 * Esta clase configura cómo se comporta la aplicación en el navegador y dispositivos móviles.
 */
@Push // Activa la comunicación bidireccional mediante WebSockets (necesario para actualizaciones en tiempo real)
@Theme(value = "my-app") // Especifica el nombre del tema personalizado ubicado en frontend/themes/my-app
@PWA(
    name = "TuFood App", 
    shortName = "TuFood",
    description = "Sistema de gestión para restaurantes y pedidos a domicilio"
) // Convierte la web en una Progressive Web App (instalable en móviles, con icono propio)
public class AppShell implements AppShellConfigurator {
    // Esta interfaz permite configurar el <head> de la página, añadir scripts o enlaces a iconos.
}