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

/**
 * VISTA DE ESTADÍSTICAS (Dashboard)
 * Esta clase proporciona una visión analítica del negocio, mostrando métricas clave (KPIs)
 * y un desglose de los productos con mayor rendimiento comercial.
 */
@PageTitle("Estadísticas | Tu Food")
@Route(value = "estadisticas", layout = MainLayout.class)
@PermitAll // Requiere que el usuario esté autenticado para visualizar los datos sensibles
public class EstadisticasView extends VerticalLayout {

    private final OrderService orderService;

    /**
     * Constructor de la vista. Inyecta el servicio de pedidos para obtener datos reales.
     */
    public EstadisticasView(@Autowired OrderService orderService) {
        this.orderService = orderService;
        
        // Configuración básica del contenedor principal
        addClassName("estadisticas-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(false);
        setSpacing(true);
        setWidthFull();

        // --- SECCIÓN: ENCABEZADO ---
        H2 title = new H2("Panel de Control del Restaurante");
        Paragraph subtitle = new Paragraph("Resumen general del rendimiento de ventas y productos.");
        add(title, subtitle);

        // --- LÓGICA: OBTENCIÓN DE DATOS DINÁMICOS ---
        // Consultamos el servicio para saber cuántos pedidos existen en la base de datos actualmente
        String totalPedidos = String.valueOf(orderService.getTodosLosPedidos().size());

        // --- SECCIÓN: FILA DE TARJETAS (KPIs) ---
        // Utilizamos un layout horizontal para mostrar las métricas de un vistazo
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Añadimos tarjetas con diferentes temas visuales (success, info, warning)
        kpiLayout.add(
            createStatCard("Ventas Totales", "1.250,50 €", VaadinIcon.MONEY, "success"),
            createStatCard("Pedidos Totales", totalPedidos, VaadinIcon.TRUCK, "info"), // Dato dinámico del servicio
            createStatCard("Ticket Medio", "27,80 €", VaadinIcon.CHART_LINE, "warning")
        );
        add(kpiLayout);

        // --- SECCIÓN: DESGLOSE DE PRODUCTOS ---
        add(new H3("Productos más consumidos"));
        configureGrid(); // Inicializa y rellena la tabla de datos
    }

    /**
     * Configura el componente Grid para mostrar el listado de productos más vendidos.
     */
    private void configureGrid() {
        // Creamos el Grid basado en la clase interna ProductoStats
        Grid<ProductoStats> grid = new Grid<>(ProductoStats.class, false);
        grid.setAllRowsVisible(true); // Ajusta la altura del grid al contenido

        // Definición manual de columnas para mayor control sobre los encabezados
        grid.addColumn(ProductoStats::getNombre).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(ProductoStats::getCantidad).setHeader("Unidades Vendidas").setAutoWidth(true);
        grid.addColumn(ProductoStats::getIngresos).setHeader("Ingresos Generados (€)").setAutoWidth(true);

        // Carga de datos de ejemplo (Normalmente aquí se llamaría a una consulta 'Group By' de la BD)
        grid.setItems(List.of(
            new ProductoStats("Classic Burger", 120, "1.500,00"),
            new ProductoStats("Pizza Pepperoni", 85, "1.190,00"),
            new ProductoStats("Papas Supremas", 200, "1.000,00"),
            new ProductoStats("Refresco 500ml", 310, "930,00")
        ));
        
        grid.addClassName("stats-grid");
        add(grid);
    }

    /**
     * Método auxiliar para generar tarjetas visuales de estadísticas.
     * @param label Nombre de la métrica.
     * @param value Valor numérico o texto a mostrar.
     * @param icon Icono descriptivo de Vaadin.
     * @param theme Clase CSS para el estilo (color).
     * @return Componente Div que representa la tarjeta.
     */
    private Div createStatCard(String label, String value, VaadinIcon icon, String theme) {
        Div card = new Div();
        card.addClassNames("stat-card", theme); // El CSS definirá colores según el 'theme'
        
        Icon iconComp = icon.create();
        Span title = new Span(label);
        H2 val = new H2(value);
        
        card.add(iconComp, title, val);
        return card;
    }

    /**
     * POJO (Plain Old Java Object) interno para representar los datos de la tabla.
     * Facilita el mapeo de columnas en el Grid.
     */
    public static class ProductoStats {
        private String nombre;
        private int cantidad;
        private String ingresos;

        public ProductoStats(String nombre, int cantidad, String ingresos) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.ingresos = ingresos;
        }

        // Getters necesarios para que el Grid pueda acceder a los datos mediante Reflection
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public String getIngresos() { return ingresos; }
    }
}