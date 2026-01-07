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

/**
 * VISTA DE SEGUIMIENTO AVANZADA
 * Incluye conectores visuales entre estados y una tarjeta de contacto del repartidor.
 */
@PageTitle("Seguimiento de Pedido | TuFood")
@Route(value = "seguimiento", layout = MainLayout.class)
public class SeguimientoView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private Span infoDireccion = new Span(); // Refleja la dirección del pedido
    private Span infoMetodo = new Span();    // Refleja el método de pago
    private VerticalLayout stepperContainer = new VerticalLayout();
    private Component repartidorCard; // Tarjeta del repartidor (solo si no es recogida)

    public SeguimientoView() {
        VerticalLayout root = getContent();
        root.setAlignItems(FlexComponent.Alignment.CENTER);
        root.getStyle().set("margin-top", "var(--lumo-size-xl)");

        // Tarjeta blanca contenedora
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("800px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, 
                           LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.LARGE);

        H1 titulo = new H1("Estado de tu Pedido");
        titulo.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.NONE);
        
        Paragraph sub = new Paragraph("¡Gracias por confiar en TuFood!");
        sub.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE);

        stepperContainer.setPadding(false);
        
        card.add(titulo, sub, new Hr(), stepperContainer, new Hr(), createResumenEntrega());
        root.add(card);
    }

    /**
     * Actualiza la vista basándose en si el pedido es Recogida o Domicilio.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String direccion = event.getLocation().getQueryParameters().getSingleParameter("direccion").orElse("No especificada");
        String metodo = event.getLocation().getQueryParameters().getSingleParameter("metodo").orElse("No especificado");

        boolean esRecogida = direccion.equalsIgnoreCase("Recogida en Tienda");

        stepperContainer.removeAll();
        // Genera el stepper con las líneas conectoras
        stepperContainer.add(createTrackingStepper(esRecogida));

        // Si es entrega a domicilio, mostramos la tarjeta del repartidor Carlos
        if (!esRecogida) {
            if (repartidorCard == null) repartidorCard = createRepartidorCard();
            stepperContainer.add(repartidorCard);
        }

        infoDireccion.setText(esRecogida ? "Punto de recogida: Calle Principal 123 (TuFood Central)" : direccion);
        infoMetodo.setText(metodo);
    }

    /**
     * Crea el Stepper horizontal con líneas (conectores) entre iconos.
     */
    private Component createTrackingStepper(boolean esRecogida) {
        HorizontalLayout stepper = new HorizontalLayout();
        stepper.setWidthFull();
        stepper.addClassNames(LumoUtility.Margin.Vertical.XLARGE, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);

        // Lógica para cambiar el tercer icono según el modo de entrega
        String tercerPasoEtiqueta = esRecogida ? "Listo" : "En camino";
        VaadinIcon tercerPasoIcono = esRecogida ? VaadinIcon.SHOP : VaadinIcon.TRUCK;

        stepper.add(
            createStep("Confirmado", VaadinIcon.CHECK_CIRCLE, true),
            createConnector(true), // Línea naranja (activa)
            createStep("Cocina", VaadinIcon.FIRE, true),
            createConnector(true),
            createStep(tercerPasoEtiqueta, tercerPasoIcono, true), // Paso actual
            createConnector(false), // Línea gris (inactiva)
            createStep("Entregado", VaadinIcon.PACKAGE, false)
        );
        return stepper;
    }

    /**
     * Crea el círculo con el icono y el texto debajo.
     */
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

    /**
     * Crea la línea horizontal que une dos círculos del stepper.
     */
    private Div createConnector(boolean active) {
        Div line = new Div();
        line.setHeight("4px"); line.setWidth("50px");
        line.getStyle().set("background-color", active ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)");
        line.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return line;
    }

    /**
     * Tarjeta informativa del repartidor con avatar dinámico.
     */
    private Component createRepartidorCard() {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                           LumoUtility.BorderRadius.LARGE, LumoUtility.AlignItems.CENTER);
        
        // Avatar generado mediante una API de avatares gratuita
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