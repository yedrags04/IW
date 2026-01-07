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


@PageTitle("Seguimiento de Pedido | TuFood")
@Route(value = "seguimiento", layout = MainLayout.class)
public class SeguimientoView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private Span infoDireccion = new Span();
    private Span infoMetodo = new Span();
    private VerticalLayout stepperContainer = new VerticalLayout();
    private Component repartidorCard;

    public SeguimientoView() {
        VerticalLayout root = getContent();
        root.setAlignItems(FlexComponent.Alignment.CENTER);
        root.getStyle().set("margin-top", "var(--lumo-size-xl)");

        // Contenedor de la "Tarjeta de seguimiento"
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("800px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.LARGE);

        H1 titulo = new H1("Estado de tu Pedido");
        titulo.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.NONE);
        
        Paragraph sub = new Paragraph("¡Gracias por confiar en TuFood!");
        sub.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE);

        stepperContainer.setPadding(false);
        
        card.add(titulo, sub, new Hr(), stepperContainer, new Hr(), createResumenEntrega());
        root.add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String direccion = event.getLocation().getQueryParameters().getSingleParameter("direccion").orElse("No especificada");
        String metodo = event.getLocation().getQueryParameters().getSingleParameter("metodo").orElse("No especificado");

        boolean esRecogida = direccion.equalsIgnoreCase("Recogida en Tienda");

        stepperContainer.removeAll();
        stepperContainer.add(createTrackingStepper(esRecogida));

        if (!esRecogida) {
            if (repartidorCard == null) repartidorCard = createRepartidorCard();
            stepperContainer.add(repartidorCard);
        }

        infoDireccion.setText(esRecogida ? "Punto de recogida: Calle Principal 123 (TuFood Central)" : direccion);
        infoMetodo.setText(metodo);
    }

    private Component createTrackingStepper(boolean esRecogida) {
        HorizontalLayout stepper = new HorizontalLayout();
        stepper.setWidthFull();
        stepper.addClassNames(LumoUtility.Margin.Vertical.XLARGE, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        String tercerPasoEtiqueta = esRecogida ? "Listo" : "En camino";
        VaadinIcon tercerPasoIcono = esRecogida ? VaadinIcon.SHOP : VaadinIcon.TRUCK;

        stepper.add(
            createStep("Confirmado", VaadinIcon.CHECK_CIRCLE, true),
            createConnector(true),
            createStep("Cocina", VaadinIcon.FIRE, true),
            createConnector(true),
            createStep(tercerPasoEtiqueta, tercerPasoIcono, true), // Paso actual activo
            createConnector(false),
            createStep("Entregado", VaadinIcon.PACKAGE, false)
        );
        return stepper;
    }

    private VerticalLayout createStep(String label, VaadinIcon icon, boolean active) {
        VerticalLayout v = new VerticalLayout();
        v.setPadding(false); v.setSpacing(false);
        v.setAlignItems(FlexComponent.Alignment.CENTER);
        v.setWidth("min-content");
        Div circle = new Div(icon.create());
        circle.getStyle()
            .set("background-color", active ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)")
            .set("color", active ? "white" : "var(--lumo-disabled-text-color)")
            .set("border-radius", "50%").set("padding", "12px").set("display", "flex");
        Span text = new Span(label);
        text.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Margin.Top.SMALL, LumoUtility.FontWeight.BOLD);
        v.add(circle, text);
        return v;
    }

    private Div createConnector(boolean active) {
        Div line = new Div();
        line.setHeight("4px"); line.setWidth("50px");
        line.getStyle().set("background-color", active ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)");
        line.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return line;
    }

    private Component createRepartidorCard() {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.AlignItems.CENTER);
        
        Image avatar = new Image("https://api.dicebear.com/7.x/avataaars/svg?seed=Felix", "Repartidor");
        avatar.setWidth("50px");
        avatar.getStyle().set("border-radius", "50%");

        VerticalLayout info = new VerticalLayout(
            new Span("Repartidor: Carlos Gómez"),
            new Span("Moto: Honda SH125 Blanca")
        );
        info.setSpacing(false); info.setPadding(false);
        info.addClassNames(LumoUtility.FontSize.SMALL);
        
        card.add(avatar, info);
        return card;
    }

    private Component createResumenEntrega() {
        VerticalLayout res = new VerticalLayout();
        res.setPadding(false);
        
        H3 subtitulo = new H3("Detalles de la Entrega");
        subtitulo.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        HorizontalLayout dirRow = new HorizontalLayout(VaadinIcon.MAP_MARKER.create(), infoDireccion);
        dirRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        HorizontalLayout metRow = new HorizontalLayout(VaadinIcon.CREDIT_CARD.create(), infoMetodo);
        metRow.setAlignItems(FlexComponent.Alignment.CENTER);

        res.add(subtitulo, dirRow, metRow);
        return res;
    }
}