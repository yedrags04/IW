package com.example.application.views.tufood;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("TuFood")
@Route(value = "tufood", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class TuFoodView extends Composite<VerticalLayout> {

    public TuFoodView() {
        VerticalLayout root = getContent();
        root.getStyle().set("margin-top", "var(--lumo-size-xl)");
        
        // --- DISEÑO DE PANEL CENTRADO ---
        root.setWidthFull();
        root.setMaxWidth("850px"); // Ancho ideal para lectura
        root.addClassNames(LumoUtility.Margin.Horizontal.AUTO);
        
        root.getStyle()
            .set("background-color", "white")
            .set("padding", "2.5rem")
            .set("border-radius", "16px")
            .set("box-shadow", "0 4px 20px rgba(0,0,0,0.08)");

        // --- 1. CABECERA ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H1 title = new H1("Gestión de Alimentos");
        title.getStyle().set("font-size", "1.8rem").set("margin", "0");
        
        Button btnAction = new Button("Nueva Orden", VaadinIcon.PLUS.create());
        btnAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        header.add(title, btnAction);

        // --- 2. BARRA DE ESTADÍSTICAS ---
        HorizontalLayout infoBar = new HorizontalLayout();
        infoBar.setWidthFull();
        infoBar.getStyle().set("gap", "3rem").set("margin-top", "1.5rem");
        infoBar.add(createStat("Pendientes", "12"), createStat("Entregados", "45"), createStat("Total", "57"));

        // --- 3. FORMULARIO ---
        H3 formTitle = new H3("Detalles del Pedido");
        formTitle.getStyle().set("margin-top", "2rem");

        HorizontalLayout formRow = new HorizontalLayout();
        formRow.setWidthFull();
        formRow.getStyle().set("flex-wrap", "wrap"); 

        TextField productName = new TextField("Producto");
        productName.setPrefixComponent(VaadinIcon.PACKAGE.create());
        productName.setPlaceholder("Ej. Pizza Margarita");
        productName.setMinWidth("300px");
        
        TextField tableNumber = new TextField("Mesa");
        tableNumber.setPlaceholder("Nº");
        tableNumber.setWidth("100px");

        formRow.add(productName, tableNumber);
        formRow.setFlexGrow(1.0, productName);

        root.add(header, new Hr(), infoBar, formTitle, formRow);
    }

    private VerticalLayout createStat(String label, String value) {
        VerticalLayout v = new VerticalLayout();
        v.setPadding(false); v.setSpacing(false); 
        v.setWidth("min-content");
        
        Span sLabel = new Span(label);
        sLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD);
        
        Span sValue = new Span(value);
        sValue.getStyle().set("font-size", "1.5rem").set("font-weight", "800");
        
        v.add(sLabel, sValue);
        return v;
    }
}