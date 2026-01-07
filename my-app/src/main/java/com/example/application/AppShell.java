package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * Configuración del Shell de la aplicación.
 * @Push: Habilita la actualización de la UI desde el servidor (útil para pedidos en tiempo real).
 * @Theme: Define el nombre del tema CSS/Lumo que se encuentra en frontend/themes.
 * @PWA: Permite que la app se instale en móviles como si fuera una aplicación nativa.
 */
@Push
@Theme(value = "my-app") 
@PWA(name = "TuFood App", shortName = "TuFood")
public class AppShell implements AppShellConfigurator {
}