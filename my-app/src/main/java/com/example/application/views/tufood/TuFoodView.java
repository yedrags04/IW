package com.example.application.views.tufood;

import com.example.application.model.Pedido;
import com.example.application.security.AuthService;
import com.example.application.services.OrderService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;

/**
 * Panel de Gestión de Operaciones (Back-office).
 * Permite gestionar la cola de pedidos y visualizar KPIs en tiempo real.
 */
@PageTitle("Panel de Control | TuFood")
@Route(value = "tufood", layout = MainLayout.class)
public class TuFoodView extends Composite<VerticalLayout> {

    private final OrderService orderService;
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    
    // Spans dinámicos para el cuadro de mandos (KPIs)
    private final Span txtNuevos = new Span("0");
    private final Span txtPreparacion = new Span("0");
    private final Span txtListos = new Span("0");
    private final Span txtEntregados = new Span("0");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public TuFoodView(AuthService authService, OrderService orderService) {
        this.orderService = orderService;
        VerticalLayout root = getContent();
        
        root.setSizeFull();
        root.setPadding(true);

        // Control de acceso para personal autorizado
        if (!authService.isAdmin() && !authService.isWorker()) {
            root.setAlignItems(FlexComponent.Alignment.CENTER);
            root.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            root.add(new H2("Acceso denegado"));
            return;
        }

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1200px");
        mainContainer.addClassNames(LumoUtility.Margin.Horizontal.AUTO);

        H1 title = new H1("Gestión de Operaciones");
        
        Button btnRefresh = new Button(VaadinIcon.REFRESH.create(), e -> actualizarTodo());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        
        HorizontalLayout header = new HorizontalLayout(
            new VerticalLayout(title, new Span("Control de flujo cocina y sala")), 
            btnRefresh
        );
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // KPIs de estadísticas: tarjetas de colores para ver el volumen de trabajo
        HorizontalLayout kpiLayout = new HorizontalLayout(
            createStatCard("Nuevos", txtNuevos, VaadinIcon.BELL, "success"),
            createStatCard("Cocinando", txtPreparacion, VaadinIcon.FIRE, "warning"),
            createStatCard("Listos", txtListos, VaadinIcon.PACKAGE, "info"),
            createStatCard("Cerrados", txtEntregados, VaadinIcon.CHECK_CIRCLE, null)
        );
        kpiLayout.setWidthFull();

        configureGrid();
        
        mainContainer.add(header, kpiLayout, new H3("Cola de Comandas"), grid);
        root.add(mainContainer);

        actualizarTodo(); 
    }

    /**
     * Configura la tabla de pedidos con columnas personalizadas y botones de acción.
     */
    private void configureGrid() {
        grid.removeAllColumns();
        
        grid.addColumn(p -> p.getFecha() != null ? p.getFecha().format(formatter) : "--:--")
            .setHeader("Hora").setAutoWidth(true);

        grid.addColumn(Pedido::getCliente).setHeader("Mesa / Cliente").setAutoWidth(true);
        grid.addColumn(Pedido::getTipo).setHeader("Tipo").setAutoWidth(true);
        
        // Columna con Badge de color según el estado
        grid.addComponentColumn(this::createStatusBadge).setHeader("Estado").setAutoWidth(true);
    
        // Columna de acción: cambia el texto del botón según el estado actual del pedido
        grid.addComponentColumn(pedido -> {
            String estado = pedido.getEstado().toUpperCase();
            String etiqueta;
            boolean visible = true;

            switch (estado) {
                case "NUEVO" -> etiqueta = "Empezar Cocina";
                case "EN PREPARACION" -> etiqueta = "Plato Listo";
                case "LISTO" -> etiqueta = "Finalizar";
                default -> { visible = false; etiqueta = ""; }
            }

            Button btn = new Button(etiqueta);
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btn.setVisible(visible);
            btn.addClickListener(e -> {
                // Lógica del servicio para avanzar al siguiente estado
                orderService.actualizarEstado(pedido.getId_db());
                actualizarTodo();
            });
            return btn;
        }).setHeader("Acción").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    /**
     * Sincroniza la tabla y los contadores con el servicio de base de datos.
     */
    private void actualizarTodo() {
        grid.setItems(orderService.getTodosLosPedidos());
        txtNuevos.setText(String.valueOf(orderService.contarPorEstado("NUEVO")));
        txtPreparacion.setText(String.valueOf(orderService.contarPorEstado("EN PREPARACION")));
        txtListos.setText(String.valueOf(orderService.contarPorEstado("LISTO")));
        txtEntregados.setText(String.valueOf(orderService.contarPorEstado("ENTREGADO")));
    }

    /**
     * Crea una tarjeta de estadísticas para la parte superior del panel.
     */
    private Component createStatCard(String title, Span valueSpan, VaadinIcon icon, String theme) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card");
        if (theme != null) card.addClassName(theme);
        
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(false);
        
        Icon i = icon.create();
        i.setSize("30px");
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);
        
        card.add(i, new Span(title), valueSpan);
        return card;
    }

    /**
     * Crea un badge visual para la tabla.
     */
    private Span createStatusBadge(Pedido p) {
        String estado = p.getEstado().toUpperCase();
        Span badge = new Span(estado);
        badge.getElement().getThemeList().add("badge pill");
        if(estado.equals("NUEVO")) badge.getElement().getThemeList().add("error");
        else if(estado.equals("EN PREPARACION")) badge.getElement().getThemeList().add("warning");
        else if(estado.equals("LISTO")) badge.getElement().getThemeList().add("success");
        return badge;
    }
}