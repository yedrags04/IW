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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Panel de Control | TuFood")
@Route(value = "tufood", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.UTENSILS_SOLID, title = "Gestión Pedidos")
public class TuFoodView extends Composite<VerticalLayout> {

    private final OrderService orderService;
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    private final Span txtNuevos = new Span("0");
    private final Span txtCocina = new Span("0");
    private final Span txtListos = new Span("0");

    public TuFoodView(AuthService authService, OrderService orderService) {
        this.orderService = orderService;

        VerticalLayout root = getContent();
        
        if (!authService.isAdmin()) {
            root.setAlignItems(FlexComponent.Alignment.CENTER);
            root.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            root.add(new H2("Acceso denegado"));
            return;
        }

        root.setSizeFull();
        root.setPadding(false);
        root.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("1200px");
        content.addClassNames(LumoUtility.Margin.Horizontal.AUTO, LumoUtility.Padding.MEDIUM);

        // ENCABEZADO
        H1 title = new H1("Gestión de Operaciones");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.NONE);
        
        Button btnRefresh = new Button(VaadinIcon.REFRESH.create(), e -> actualizarTodo());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(new VerticalLayout(title, new Span("Pedidos en tiempo real")), btnRefresh);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // ESTADÍSTICAS
        HorizontalLayout stats = new HorizontalLayout(
            createStatCard("Nuevos", txtNuevos, VaadinIcon.BELL, "var(--lumo-error-color)"),
            createStatCard("En Cocina", txtCocina, VaadinIcon.FIRE, "var(--lumo-warning-color)"),
            createStatCard("Listos", txtListos, VaadinIcon.PACKAGE, "var(--lumo-success-color)")
        );
        stats.setWidthFull();

        // GRID
        configureGrid();

        content.add(header, stats, new H3("Cola de Trabajo"), grid);
        root.add(content);

        actualizarTodo(); 
    }

private void configureGrid() {
    // 1. Limpiamos cualquier configuración previa de columnas
    grid.removeAllColumns();

    // 2. Definimos las columnas manualmente (Evita errores de tipos automáticos)
    grid.addColumn(Pedido::getTicketId)
        .setHeader("ID Ticket")
        .setKey("ticketId") // Asignamos una clave string interna
        .setAutoWidth(true);

    grid.addColumn(Pedido::getCliente)
        .setHeader("Cliente")
        .setAutoWidth(true);

    grid.addColumn(Pedido::getTipo)
        .setHeader("Tipo")
        .setAutoWidth(true);

    grid.addComponentColumn(this::createStatusBadge)
        .setHeader("Estado")
        .setAutoWidth(true);
    
    grid.addComponentColumn(pedido -> {
        String etiqueta = pedido.getEstado().equals("NUEVO") ? "Cocinar" : "Finalizar";
        Button btn = new Button(etiqueta, VaadinIcon.ARROW_RIGHT.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        
        btn.addClickListener(e -> {
            // Aquí usamos el Long sin problemas porque es lógica Java pura
            orderService.actualizarEstado(pedido.getId_db());
            actualizarTodo();
        });
        
        btn.setEnabled(!pedido.getEstado().equals("LISTO"));
        return btn;
    }).setHeader("Acción").setAutoWidth(true);

    // 3. Estilo y comportamiento
    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
    grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
    grid.setAllRowsVisible(true);
}
    private void actualizarTodo() {
        grid.setItems(orderService.getTodosLosPedidos());
        txtNuevos.setText(String.valueOf(orderService.contarPorEstado("NUEVO")));
        txtCocina.setText(String.valueOf(orderService.contarPorEstado("COCINANDO")));
        txtListos.setText(String.valueOf(orderService.contarPorEstado("LISTO")));
    }

    private Component createStatCard(String title, Span valueSpan, VaadinIcon icon, String color) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
        card.setWidthFull();
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon i = icon.create();
        i.getStyle().set("color", color);
        i.addClassNames(LumoUtility.Padding.SMALL, "contrast-5", LumoUtility.BorderRadius.MEDIUM);

        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);
        VerticalLayout info = new VerticalLayout(new Span(title), valueSpan);
        info.setPadding(false); info.setSpacing(false);

        card.add(i, info);
        return card;
    }

    private Span createStatusBadge(Pedido p) {
        Span badge = new Span(p.getEstado());
        badge.getElement().getThemeList().add("badge pill");
        if (p.getEstado().equals("NUEVO")) badge.getElement().getThemeList().add("error");
        else if (p.getEstado().equals("COCINANDO")) badge.getElement().getThemeList().add("warning");
        else badge.getElement().getThemeList().add("success");
        return badge;
    }
}