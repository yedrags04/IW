package com.example.application.views.tufood;

import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.router.Menu;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Gestión Pedidos | TuFood") // 1. Título de la pestaña del navegador
@Route(value = "tufood", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.UTENSILS_SOLID, title = "Gestión Pedidos") // 2. Título en el menú lateral
public class TuFoodView extends Composite<VerticalLayout> {

    public TuFoodView(AuthService authService) {
        VerticalLayout root = getContent();
        
        // --- 1. CONTROL DE ACCESO ---
        if (!authService.isAdmin()) {
            root.add(new Span("Acceso denegado. Redirigiendo..."));
            addAttachListener(e -> getUI().ifPresent(ui -> ui.navigate("home")));
            return;
        }

        // --- 2. CONFIGURACIÓN DE LA VISTA ---
        root.addClassName("tu-food-view");
        root.setAlignItems(FlexComponent.Alignment.CENTER);
        root.setSizeFull();
        root.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // --- PANEL CENTRAL ---
        VerticalLayout mainPanel = new VerticalLayout();
        mainPanel.addClassName("main-panel");
        mainPanel.setMaxWidth("1100px");
        mainPanel.setWidthFull();
        mainPanel.setPadding(true);
        mainPanel.setSpacing(true);
        mainPanel.getStyle().set("gap", "2.5rem");

        // --- 1. ENCABEZADO ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // 3. Título visual dentro de la página
        H1 title = new H1("Gestión de Pedidos"); 
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.EXTRABOLD);

        Button btnAction = new Button("Nueva Orden", VaadinIcon.PLUS.create());
        btnAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAction.getElement().setAttribute("theme", "large"); 
        
        header.add(title, btnAction);

        // --- 2. ESTADÍSTICAS ---
        HorizontalLayout infoBar = new HorizontalLayout();
        infoBar.setWidthFull();
        infoBar.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        infoBar.setSpacing(true);
        infoBar.getStyle().set("gap", "1.5rem");
        
        Component stat1 = createStat("Pendientes", "12", VaadinIcon.CLOCK, "#ff4d00");
        Component stat2 = createStat("Entregados", "45", VaadinIcon.CHECK_CIRCLE, "#2d6a4f");
        Component stat3 = createStat("Total Hoy", "57", VaadinIcon.BAR_CHART, "#1d3557");

        infoBar.add(stat1, stat2, stat3);
        infoBar.setFlexGrow(1, stat1);
        infoBar.setFlexGrow(1, stat2);
        infoBar.setFlexGrow(1, stat3);

        // --- 3. FORMULARIO ---
        H3 formTitle = new H3("Detalles del Pedido");
        formTitle.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.FontSize.XLARGE);

        HorizontalLayout formRow = new HorizontalLayout();
        formRow.setWidthFull();

        TextField productName = new TextField("Producto");
        productName.setPrefixComponent(VaadinIcon.PACKAGE.create());
        productName.setPlaceholder("Ej. Pizza Margarita");
        productName.getElement().setAttribute("theme", "large");
        
        TextField tableNumber = new TextField("Mesa");
        tableNumber.setPlaceholder("Nº");
        tableNumber.setWidth("120px");
        tableNumber.getElement().setAttribute("theme", "large");

        formRow.add(productName, tableNumber);
        formRow.setFlexGrow(1.0, productName);

        mainPanel.add(header, new Hr(), infoBar, formTitle, formRow);
        root.add(mainPanel);
    }

    private VerticalLayout createStat(String label, String value, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card");
        card.setSpacing(false);
        card.setPadding(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon i = icon.create();
        i.getStyle().set("color", color);
        
        Span sValue = new Span(value);
        sValue.getStyle().set("font-size", "2rem").set("font-weight", "800");
        
        Span sLabel = new Span(label);
        sLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD);
        
        card.add(i, sValue, sLabel);
        return card;
    }
}