package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Push
@Theme(value = "my-app") // Configuración de tema centralizada
@PWA(name = "TuFood App", shortName = "TuFood")
public class AppShell implements AppShellConfigurator {
}