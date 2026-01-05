package com.example.application.views.estadisticas;

import com.example.application.services.OrderService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle("Estadísticas | Tu Food")
@Route(value = "estadisticas", layout = MainLayout.class)
@PermitAll
public class EstadisticasView extends VerticalLayout {

    private final OrderService orderService;

    public EstadisticasView(@Autowired OrderService orderService) {
        this.orderService = orderService;
        
        addClassName("estadisticas-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(false);
        setSpacing(true);
        setWidthFull();

        // --- ENCABEZADO ---
        H2 title = new H2("Panel de Control del Restaurante");
        Paragraph subtitle = new Paragraph("Resumen general del rendimiento de ventas y productos.");
        add(title, subtitle);

        // --- LÓGICA DE CONTEO DINÁMICO ---
        // Obtenemos el total de pedidos usando el servicio que conecta con la BD
        String totalPedidos = String.valueOf(orderService.getTodosLosPedidos().size());

        // --- FILA DE TARJETAS (KPIs) ---
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        kpiLayout.add(
            createStatCard("Ventas Totales", "1.250,50 €", VaadinIcon.MONEY, "success"),
            createStatCard("Pedidos Totales", totalPedidos, VaadinIcon.TRUCK, "info"), // Ahora es dinámico
            createStatCard("Ticket Medio", "27,80 €", VaadinIcon.CHART_LINE, "warning")
        );
        add(kpiLayout);

        // --- SECCIÓN DE PRODUCTOS (GRID) ---
        add(new H3("Productos más consumidos"));
        configureGrid();
    }

    private void configureGrid() {
        Grid<ProductoStats> grid = new Grid<>(ProductoStats.class, false);
        grid.setAllRowsVisible(true);
        grid.addColumn(ProductoStats::getNombre).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(ProductoStats::getCantidad).setHeader("Unidades Vendidas").setAutoWidth(true);
        grid.addColumn(ProductoStats::getIngresos).setHeader("Ingresos Generados (€)").setAutoWidth(true);

        grid.setItems(List.of(
            new ProductoStats("Classic Burger", 120, "1.500,00"),
            new ProductoStats("Pizza Pepperoni", 85, "1.190,00"),
            new ProductoStats("Papas Supremas", 200, "1.000,00"),
            new ProductoStats("Refresco 500ml", 310, "930,00")
        ));
        
        grid.addClassName("stats-grid");
        add(grid);
    }

    private Div createStatCard(String label, String value, VaadinIcon icon, String theme) {
        Div card = new Div();
        card.addClassNames("stat-card", theme);
        Icon iconComp = icon.create();
        Span title = new Span(label);
        H2 val = new H2(value);
        card.add(iconComp, title, val);
        return card;
    }

    public static class ProductoStats {
        private String nombre;
        private int cantidad;
        private String ingresos;

        public ProductoStats(String nombre, int cantidad, String ingresos) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.ingresos = ingresos;
        }

        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public String getIngresos() { return ingresos; }
    }
}