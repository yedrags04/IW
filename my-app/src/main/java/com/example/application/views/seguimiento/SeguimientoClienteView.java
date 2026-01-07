package com.example.application.views.seguimiento;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;

@PageTitle("Seguimiento de Pedido | TuFood")
@Route(value = "seguimientoCliente", layout = MainLayout.class)
@AnonymousAllowed
public class SeguimientoClienteView extends VerticalLayout implements BeforeEnterObserver {

    private String metodoPago;
    private String direccionEntrega;
    private final VerticalLayout statusContainer = new VerticalLayout();
    private final Span idPedidoLabel = new Span();

    public SeguimientoClienteView() {
        addClassName("seguimiento-view");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        // Contenedor principal tipo tarjeta
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("700px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.LARGE);

        // Cabecera
        H2 title = new H2("Estado de tu pedido");
        idPedidoLabel.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        
        // --- EL TRACKER VISUAL (STEPPER) ---
        HorizontalLayout stepper = createStepper(2); // Simulamos que está en el paso 2: Preparando

        // Información de entrega
        Div infoBox = new Div();
        infoBox.addClassName("info-seguimiento");
        infoBox.setWidthFull();

        card.add(title, idPedidoLabel, stepper, statusContainer, infoBox, createFooter());
        add(card);
    }

    private HorizontalLayout createStepper(int stepActivo) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("stepper-container");
        layout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        layout.add(createStep("Recibido", VaadinIcon.CHECK_CIRCLE, stepActivo >= 1));
        layout.add(createStep("Cocinando", VaadinIcon.FIRE, stepActivo >= 2));
        layout.add(createStep("En camino", VaadinIcon.TRUCK, stepActivo >= 3));
        layout.add(createStep("Entregado", VaadinIcon.HOME, stepActivo >= 4));

        return layout;
    }

    private VerticalLayout createStep(String label, VaadinIcon icon, boolean completado) {
        VerticalLayout step = new VerticalLayout();
        step.setPadding(false);
        step.setSpacing(false);
        step.setAlignItems(Alignment.CENTER);
        step.addClassName("step");
        if (completado) step.addClassName("active");

        Icon i = icon.create();
        Span s = new Span(label);
        step.add(i, s);
        return step;
    }

    private VerticalLayout createFooter() {
        Button btnHome = new Button("Volver al inicio", e -> UI.getCurrent().navigate("home"));
        btnHome.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        
        VerticalLayout footer = new VerticalLayout(new Hr(), btnHome);
        footer.setAlignItems(Alignment.CENTER);
        return footer;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Recuperar parámetros de la URL enviados desde PagoView
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        this.metodoPago = params.getOrDefault("metodo", List.of("No especificado")).get(0);
        this.direccionEntrega = params.getOrDefault("direccion", List.of("Recogida en tienda")).get(0);

        // Actualizar la interfaz con los datos reales
        statusContainer.removeAll();
        statusContainer.add(new Paragraph(String.format("Método de pago: %s", metodoPago)));
        statusContainer.add(new Paragraph(String.format("Entregar en: %s", direccionEntrega)));
        idPedidoLabel.setText("Seguimiento en tiempo real para tu comanda");
    }
}