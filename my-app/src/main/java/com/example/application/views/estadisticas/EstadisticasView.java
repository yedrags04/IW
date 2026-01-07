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

/* * VISTA DE ESTADÍSTICAS
 * Proporciona un cuadro de mando con indicadores clave (KPIs) y una tabla 
 * con el desglose de productos más vendidos.
 */
@PageTitle("Estadísticas | Tu Food")
@Route(value = "estadisticas", layout = MainLayout.class)
@PermitAll
public class EstadisticasView extends VerticalLayout {

    private final OrderService orderService;

    public EstadisticasView(@Autowired OrderService orderService) {
        this.orderService = orderService;
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setWidthFull();

        H2 title = new H2("Panel de Control del Restaurante");
        Paragraph subtitle = new Paragraph("Resumen general del rendimiento de ventas y productos.");
        add(title, subtitle);

        // DATOS DINÁMICOS: Consultamos al servicio el número total de pedidos registrados
        String totalPedidos = String.valueOf(orderService.getTodosLosPedidos().size());

        // FILA DE TARJETAS KPI: Diseño visual rápido para métricas clave
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        kpiLayout.add(
            createStatCard("Ventas Totales", "1.250,50 €", VaadinIcon.MONEY, "success"),
            createStatCard("Pedidos Totales", totalPedidos, VaadinIcon.TRUCK, "info"),
            createStatCard("Ticket Medio", "27,80 €", VaadinIcon.CHART_LINE, "warning")
        );
        add(kpiLayout);

        add(new H3("Productos más consumidos"));
        configureGrid();
    }

    // Configuración de la tabla (Grid) para mostrar el detalle de productos
    private void configureGrid() {
        Grid<ProductoStats> grid = new Grid<>(ProductoStats.class, false);
        grid.setAllRowsVisible(true);
        grid.addColumn(ProductoStats::getNombre).setHeader("Producto");
        grid.addColumn(ProductoStats::getCantidad).setHeader("Unidades Vendidas");
        grid.addColumn(ProductoStats::getIngresos).setHeader("Ingresos (€)");

        // Datos de ejemplo para la visualización del Grid
        grid.setItems(List.of(
            new ProductoStats("Classic Burger", 120, "1.500,00"),
            new ProductoStats("Pizza Pepperoni", 85, "1.190,00"),
            new ProductoStats("Papas Supremas", 200, "1.000,00"),
            new ProductoStats("Refresco 500ml", 310, "930,00")
        ));
        
        add(grid);
    }

    // Método auxiliar para crear las tarjetas visuales de estadísticas
    private Div createStatCard(String label, String value, VaadinIcon icon, String theme) {
        Div card = new Div();
        card.addClassNames("stat-card", theme);
        card.add(icon.create(), new Span(label), new H2(value));
        return card;
    }

    // DTO (Data Transfer Object) interno para manejar las filas del Grid
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