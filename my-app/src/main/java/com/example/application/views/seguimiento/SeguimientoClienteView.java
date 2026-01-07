package com.example.application.views.seguimiento;

import com.example.application.security.AuthService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

/**
 * VISTA DE SEGUIMIENTO PARA EL CLIENTE (SIMPLE)
 * Muestra el progreso del pedido y los detalles de entrega/pago recibidos por URL.
 */
@PageTitle("Seguimiento de Pedido | TuFood")
@Route(value = "seguimientoCliente", layout = MainLayout.class)
@AnonymousAllowed // Permite que clientes no registrados vean el estado de su pedido
public class SeguimientoClienteView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService authService;
    private final VerticalLayout statusContainer = new VerticalLayout(); // Contenedor para detalles dinámicos
    private final Span idPedidoLabel = new Span(); // Etiqueta de estado general

    public SeguimientoClienteView(AuthService authService) {
        this.authService = authService;

        // Estilo de la vista: centrado y fondo suave
        addClassName("seguimiento-view");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // Tarjeta principal blanca (Card)
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("700px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, 
                           LumoUtility.BoxShadow.MEDIUM, LumoUtility.Padding.LARGE);

        H2 title = new H2("Estado de tu pedido");
        title.getStyle().set("color", "var(--lumo-primary-color)");
        idPedidoLabel.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        
        // Creamos la barra de pasos (Stepper) simulando que está en el paso 2 (Cocinando)
        HorizontalLayout stepper = createStepper(2); 

        statusContainer.setSpacing(false);
        statusContainer.setPadding(false);

        // Montamos la tarjeta
        card.add(title, idPedidoLabel, new Hr(), stepper, statusContainer, createFooter());
        add(card);
    }

    /**
     * MÉTODO ANTES DE ENTRAR: Captura parámetros y gestiona seguridad.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // SEGURIDAD: Los trabajadores no deben usar esta vista, se les redirige a la gestión de sala
        if (authService.isUserLoggedIn() && authService.isWorker()) {
            Notification n = Notification.show("Acceso denegado: Los trabajadores deben gestionar pedidos desde el panel de sala.");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo("mesas");
            return;
        }

        // Recuperamos parámetros de la URL (?metodo=...&direccion=...)
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        String metodo = params.getOrDefault("metodo", List.of("No especificado")).get(0);
        String direccion = params.getOrDefault("direccion", List.of("Recogida en tienda")).get(0);

        statusContainer.removeAll();
        
        // Creamos las filas informativas con iconos
        HorizontalLayout rowPago = new HorizontalLayout(VaadinIcon.CREDIT_CARD.create(), 
                                                       new Span("Método de pago: " + metodo));
        HorizontalLayout rowDir = new HorizontalLayout(VaadinIcon.MAP_MARKER.create(), 
                                                      new Span("Entregar en: " + direccion));
        
        rowPago.setAlignItems(Alignment.CENTER);
        rowDir.setAlignItems(Alignment.CENTER);

        statusContainer.add(rowPago, rowDir);
        idPedidoLabel.setText("Tu pedido está siendo procesado en tiempo real");
    }

    /**
     * Crea la barra de progreso con pasos.
     */
    private HorizontalLayout createStepper(int stepActivo) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        layout.getStyle().set("margin", "2rem 0");

        // Definimos los 4 estados posibles
        layout.add(createStep("Recibido", VaadinIcon.CHECK_CIRCLE, stepActivo >= 1));
        layout.add(createStep("Cocinando", VaadinIcon.FIRE, stepActivo >= 2));
        layout.add(createStep("Reparto", VaadinIcon.TRUCK, stepActivo >= 3));
        layout.add(createStep("¡Listo!", VaadinIcon.HOME, stepActivo >= 4));

        return layout;
    }

    /**
     * Crea un paso individual (Icono + Texto).
     */
    private VerticalLayout createStep(String label, VaadinIcon icon, boolean completado) {
        VerticalLayout step = new VerticalLayout();
        step.setPadding(false); step.setSpacing(false);
        step.setAlignItems(Alignment.CENTER);

        Icon i = icon.create();
        i.setSize("32px");
        // Cambia el color si el paso ya se ha completado
        i.setColor(completado ? "var(--lumo-primary-color)" : "var(--lumo-contrast-20pct)");
        
        Span s = new Span(label);
        s.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.BOLD);
        s.getStyle().set("color", completado ? "var(--lumo-body-text-color)" : "var(--lumo-disabled-text-color)");

        step.add(i, s);
        return step;
    }

    private VerticalLayout createFooter() {
        Button btnHome = new Button("Volver al inicio", e -> UI.getCurrent().navigate(""));
        btnHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnHome.setWidthFull();
        
        VerticalLayout footer = new VerticalLayout(new Hr(), btnHome);
        footer.setPadding(false);
        footer.setAlignItems(Alignment.CENTER);
        return footer;
    }
}