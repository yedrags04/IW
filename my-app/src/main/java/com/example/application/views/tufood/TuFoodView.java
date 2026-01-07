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
 * VISTA DE GESTIÓN DE OPERACIONES (Cocina y Comandas)
 * Esta vista centraliza el flujo de trabajo del restaurante, permitiendo a los empleados
 * monitorizar y cambiar el estado de los pedidos en tiempo real.
 */
@PageTitle("Panel de Control | TuFood")
@Route(value = "tufood", layout = MainLayout.class)
public class TuFoodView extends Composite<VerticalLayout> {

    private final OrderService orderService;
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    
    // Spans para las tarjetas de estadísticas (KPIs) de la parte superior
    private final Span txtNuevos = new Span("0");
    private final Span txtPreparacion = new Span("0");
    private final Span txtListos = new Span("0");
    private final Span txtEntregados = new Span("0");

    // Formateador de hora para mostrar solo el momento de entrada del pedido
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public TuFoodView(AuthService authService, OrderService orderService) {
        this.orderService = orderService;
        VerticalLayout root = getContent();
        
        root.setSizeFull();
        root.setPadding(true);
        root.getStyle().set("overflow", "auto");

        // 1. SEGURIDAD: Solo ADMIN o TRABAJADOR pueden gestionar comandas
        if (!authService.isAdmin() && !authService.isWorker()) {
            root.setAlignItems(FlexComponent.Alignment.CENTER);
            root.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            root.add(new H2("Acceso denegado"));
            return;
        }

        // Contenedor principal con ancho máximo para pantallas grandes
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1200px");
        mainContainer.addClassNames(LumoUtility.Margin.Horizontal.AUTO);
        mainContainer.setPadding(false);

        // --- CABECERA ---
        H1 title = new H1("Gestión de Operaciones");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.NONE);
        
        // Botón de refresco manual para actualizar la cola de pedidos
        Button btnRefresh = new Button(VaadinIcon.REFRESH.create(), e -> actualizarTodo());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        
        HorizontalLayout header = new HorizontalLayout(
            new VerticalLayout(title, new Span("Control de flujo cocina y sala")), 
            btnRefresh
        );
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // --- KPIs DE ESTADÍSTICAS ---
        // Fila superior que muestra el conteo de pedidos por estado
        HorizontalLayout kpiLayout = new HorizontalLayout(
            createStatCard("Nuevos", txtNuevos, VaadinIcon.BELL, "success"),
            createStatCard("Cocinando", txtPreparacion, VaadinIcon.FIRE, "warning"),
            createStatCard("Listos", txtListos, VaadinIcon.PACKAGE, "info"),
            createStatCard("Cerrados", txtEntregados, VaadinIcon.CHECK_CIRCLE, null)
        );
        kpiLayout.setWidthFull();
        kpiLayout.addClassNames(LumoUtility.Margin.Vertical.MEDIUM);

        configureGrid(); // Configuración de la tabla de pedidos
        
        mainContainer.add(header, kpiLayout, new H3("Cola de Comandas"), grid);
        root.add(mainContainer);

        actualizarTodo(); // Carga inicial de datos al abrir la vista
    }

    /**
     * Define las columnas y el comportamiento de la tabla de comandas.
     */
    private void configureGrid() {
        grid.removeAllColumns();
        
        // Columna de Hora: Formatea el objeto LocalDateTime a String HH:mm
        grid.addColumn(p -> p.getFecha() != null ? p.getFecha().format(formatter) : "--:--")
            .setHeader("Hora")
            .setAutoWidth(true);

        grid.addColumn(Pedido::getCliente).setHeader("Mesa / Cliente").setAutoWidth(true);
        grid.addColumn(Pedido::getTipo).setHeader("Tipo").setAutoWidth(true);
        
        // Columna de Estado: Muestra un "Badge" (etiqueta de color)
        grid.addComponentColumn(this::createStatusBadge).setHeader("Estado").setAutoWidth(true);
    
        // Columna de Acción: El botón cambia según el estado actual del pedido
        grid.addComponentColumn(pedido -> {
            String estado = pedido.getEstado().toUpperCase();
            String etiqueta;
            boolean visible = true;

            // Determina la siguiente acción lógica en el flujo de trabajo
            switch (estado) {
                case "NUEVO" -> etiqueta = "Empezar Cocina";
                case "EN PREPARACION" -> etiqueta = "Plato Listo";
                case "LISTO" -> etiqueta = "Finalizar";
                default -> { visible = false; etiqueta = ""; } // Pedidos entregados no tienen más acciones
            }

            Button btn = new Button(etiqueta);
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btn.setVisible(visible);
            btn.addClickListener(e -> {
                // Avanza el estado del pedido en la BD y refresca la UI
                orderService.actualizarEstado(pedido.getId_db());
                actualizarTodo();
            });
            return btn;
        }).setHeader("Acción").setAutoWidth(true);

        // Estilos visuales para la tabla
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
    }

    /**
     * Sincroniza la tabla y los contadores con el estado actual de la base de datos.
     */
    private void actualizarTodo() {
        grid.setItems(orderService.getTodosLosPedidos());
        txtNuevos.setText(String.valueOf(orderService.contarPorEstado("NUEVO")));
        txtPreparacion.setText(String.valueOf(orderService.contarPorEstado("EN PREPARACION")));
        txtListos.setText(String.valueOf(orderService.contarPorEstado("LISTO")));
        txtEntregados.setText(String.valueOf(orderService.contarPorEstado("ENTREGADO")));
    }

    /**
     * Crea una tarjeta pequeña para mostrar estadísticas rápidas.
     */
    private Component createStatCard(String title, Span valueSpan, VaadinIcon icon, String theme) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card");
        
        if (theme != null && !theme.trim().isEmpty()) {
            card.addClassName(theme);
        }
        
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(false);
        card.setPadding(true);
        
        Icon i = icon.create();
        i.setSize("30px");
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);
        
        card.add(i, new Span(title), valueSpan);
        return card;
    }

    /**
     * Genera una etiqueta visual coloreada según el estado del pedido.
     */
    private Span createStatusBadge(Pedido p) {
        String estado = p.getEstado().toUpperCase();
        Span badge = new Span(estado);
        badge.getElement().getThemeList().add("badge pill");
        
        // Asignación de colores por estado
        if(estado.equals("NUEVO")) badge.getElement().getThemeList().add("error"); // Rojo
        else if(estado.equals("EN PREPARACION")) badge.getElement().getThemeList().add("warning"); // Amarillo
        else if(estado.equals("LISTO")) badge.getElement().getThemeList().add("success"); // Verde
        
        return badge;
    }
}