package com.example.application.views.seguimiento;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component; // <-- IMPORTACIÓN QUE FALTABA
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Seguimiento de Pedido")
@Route(value = "seguimiento", layout = MainLayout.class)
// NO debe haber etiqueta @Menu aquí
public class SeguimientoView extends Composite<VerticalLayout> {

    public SeguimientoView() {
        VerticalLayout root = getContent();
        
        // 1. AJUSTES DE POSICIONAMIENTO Y ESTILO
        root.getStyle().set("margin-top", "var(--lumo-size-xl)");
        root.getStyle().set("padding-left", "4rem");
        root.getStyle().set("box-sizing", "border-box");
        root.setWidthFull();
        root.setMaxWidth("900px");
        root.addClassNames(LumoUtility.Margin.Horizontal.AUTO, LumoUtility.Padding.LARGE);

        // --- 2. CABECERA ---
        H1 titulo = new H1("Seguimiento de tu Pedido");
        titulo.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.TextColor.PRIMARY);
        
        Span idPedido = new Span("Pedido #TF-98432");
        idPedido.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        
        root.add(titulo, idPedido, new Hr());

        // --- 3. LÍNEA DE TIEMPO VISUAL ---
        root.add(createTrackingStepper());

        // --- 4. DETALLES DEL REPARTIDOR ---
        root.add(createRepartidorCard());

        // --- 5. RESUMEN DE ENTREGA ---
        root.add(createResumenEntrega());
    }

    private Component createTrackingStepper() {
        HorizontalLayout stepper = new HorizontalLayout();
        stepper.setWidthFull();
        stepper.addClassNames(LumoUtility.Margin.Vertical.XLARGE, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        // Simulación de pasos: Confirmado -> Cocinando -> En camino -> Entregado
        stepper.add(
            createStep("Confirmado", VaadinIcon.CHECK_CIRCLE, true),
            createConnector(true),
            createStep("Cocinando", VaadinIcon.FIRE, true),
            createConnector(true),
            createStep("En camino", VaadinIcon.TRUCK, true), // Paso actual
            createConnector(false),
            createStep("Entregado", VaadinIcon.PACKAGE, false)
        );

        return stepper;
    }

    private VerticalLayout createStep(String label, VaadinIcon icon, boolean active) {
        VerticalLayout v = new VerticalLayout();
        v.setPadding(false);
        v.setSpacing(false);
        v.setAlignItems(FlexComponent.Alignment.CENTER);
        v.setWidth("min-content");

        Div circle = new Div(icon.create());
        circle.getStyle()
            .set("background-color", active ? "var(--lumo-success-color)" : "var(--lumo-contrast-10pct)")
            .set("color", active ? "white" : "var(--lumo-disabled-text-color)")
            .set("border-radius", "50%")
            .set("padding", "12px")
            .set("display", "flex");

        Span text = new Span(label);
        text.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Margin.Top.SMALL, LumoUtility.FontWeight.BOLD);
        if (!active) text.addClassNames(LumoUtility.TextColor.DISABLED);

        v.add(circle, text);
        return v;
    }

    private Div createConnector(boolean active) {
        Div line = new Div();
        line.setHeight("4px");
        line.setWidth("60px");
        line.getStyle().set("background-color", active ? "var(--lumo-success-color)" : "var(--lumo-contrast-10pct)");
        line.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return line;
    }

    private Component createRepartidorCard() {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.addClassNames(
            LumoUtility.Background.CONTRAST_5, 
            LumoUtility.Padding.MEDIUM, 
            LumoUtility.BorderRadius.LARGE, 
            LumoUtility.AlignItems.CENTER
        );
        
        Div avatar = new Div(VaadinIcon.USER.create());
        avatar.addClassNames(LumoUtility.Background.CONTRAST_20, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM);
        
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false); 
        info.setSpacing(false);
        
        Span nombre = new Span("Juan Pérez");
        nombre.addClassNames(LumoUtility.FontWeight.BOLD);
        Span status = new Span("Tu repartidor está a 5 minutos de distancia");
        status.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        
        info.add(nombre, status);
        card.add(avatar, info);
        
        return card;
    }

    private Component createResumenEntrega() {
        VerticalLayout res = new VerticalLayout();
        res.setPadding(false);
        res.addClassNames(LumoUtility.Margin.Top.LARGE);

        H3 titulo = new H3("Información de entrega");
        Paragraph info = new Paragraph("Dirección: Calle Mayor 123, Madrid");
        Paragraph metodo = new Paragraph("Método de pago: Tarjeta de Crédito (Visa **** 4242)");
        
        res.add(titulo, info, metodo);
        return res;
    }
}