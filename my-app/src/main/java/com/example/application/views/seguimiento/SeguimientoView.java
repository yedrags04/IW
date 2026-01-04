package com.example.application.views.seguimiento;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

@PageTitle("Seguimiento de Pedido | TuFood")
@Route(value = "seguimiento", layout = MainLayout.class)
public class SeguimientoView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private Paragraph infoDireccion = new Paragraph();
    private Paragraph infoMetodo = new Paragraph();
    private VerticalLayout stepperContainer = new VerticalLayout();
    private Component repartidorCard;

    public SeguimientoView() {
        VerticalLayout root = getContent();
        root.getStyle().set("margin-top", "var(--lumo-size-xl)");
        root.getStyle().set("padding-left", "4rem");
        root.addClassNames(LumoUtility.Margin.Horizontal.AUTO, LumoUtility.Padding.LARGE);
        root.setMaxWidth("900px");

        H1 titulo = new H1("Estado de tu Pedido");
        titulo.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.TextColor.PRIMARY);
        
        root.add(titulo, new Hr());

        // Contenedores vacíos que llenaremos en beforeEnter
        stepperContainer.setPadding(false);
        root.add(stepperContainer);
        
        // El resumen de entrega siempre se ve
        root.add(createResumenEntrega());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String direccion = event.getLocation().getQueryParameters().getSingleParameter("direccion").orElse("");
        String metodo = event.getLocation().getQueryParameters().getSingleParameter("metodo").orElse("");

        boolean esRecogida = direccion.equalsIgnoreCase("Recogida en Tienda");

        // Limpiamos y redibujamos el stepper según el modo
        stepperContainer.removeAll();
        stepperContainer.add(createTrackingStepper(esRecogida));

        // Si es a domicilio, añadimos la tarjeta del repartidor
        if (!esRecogida) {
            if (repartidorCard == null) repartidorCard = createRepartidorCard();
            stepperContainer.add(repartidorCard);
        }

        infoDireccion.setText("📍 " + (esRecogida ? "Punto de recogida: Calle Principal 123" : "Entrega en: " + direccion));
        infoMetodo.setText("💳 Pago: " + metodo);
    }

    private Component createTrackingStepper(boolean esRecogida) {
        HorizontalLayout stepper = new HorizontalLayout();
        stepper.setWidthFull();
        stepper.addClassNames(LumoUtility.Margin.Vertical.XLARGE, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        // Cambiamos el tercer paso según el modo
        String tercerPasoEtiqueta = esRecogida ? "Listo para recoger" : "En camino";
        VaadinIcon tercerPasoIcono = esRecogida ? VaadinIcon.SHOP : VaadinIcon.TRUCK;

        stepper.add(
            createStep("Confirmado", VaadinIcon.CHECK_CIRCLE, true),
            createConnector(true),
            createStep("Cocinando", VaadinIcon.FIRE, true),
            createConnector(true),
            createStep(tercerPasoEtiqueta, tercerPasoIcono, true), // Paso actual
            createConnector(false),
            createStep("Finalizado", VaadinIcon.PACKAGE, false)
        );
        return stepper;
    }

    // --- MÉTODOS AUXILIARES (createStep, createConnector, createRepartidorCard, createResumenEntrega igual que antes) ---
    
    private VerticalLayout createStep(String label, VaadinIcon icon, boolean active) {
        VerticalLayout v = new VerticalLayout();
        v.setPadding(false); v.setSpacing(false);
        v.setAlignItems(FlexComponent.Alignment.CENTER);
        v.setWidth("min-content");
        Div circle = new Div(icon.create());
        circle.getStyle()
            .set("background-color", active ? "var(--lumo-success-color)" : "var(--lumo-contrast-10pct)")
            .set("color", active ? "white" : "var(--lumo-disabled-text-color)")
            .set("border-radius", "50%").set("padding", "12px").set("display", "flex");
        Span text = new Span(label);
        text.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Margin.Top.SMALL, LumoUtility.FontWeight.BOLD);
        v.add(circle, text);
        return v;
    }

    private Div createConnector(boolean active) {
        Div line = new Div();
        line.setHeight("4px"); line.setWidth("60px");
        line.getStyle().set("background-color", active ? "var(--lumo-success-color)" : "var(--lumo-contrast-10pct)");
        line.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return line;
    }

    private Component createRepartidorCard() {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.AlignItems.CENTER);
        VerticalLayout info = new VerticalLayout(new Span("Juan Pérez (Repartidor)"), new Span("Llegada estimada: 5 min"));
        info.setSpacing(false); info.setPadding(false);
        card.add(VaadinIcon.USER.create(), info);
        return card;
    }

    private Component createResumenEntrega() {
        VerticalLayout res = new VerticalLayout();
        res.setPadding(false);
        res.add(new H3("Información de entrega"), infoDireccion, infoMetodo);
        return res;
    }
}