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

import jakarta.annotation.security.RolesAllowed;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Panel de Control | TuFood")
@Route(value = "tufood", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.UTENSILS_SOLID, title = "Gestión Pedidos")
public class TuFoodView extends Composite<VerticalLayout> {

    private final OrderService orderService;
    private final Grid<Pedido> grid = new Grid<>(Pedido.class, false);
    
    private final Span txtNuevos = new Span("0");
    private final Span txtPreparacion = new Span("0");
    private final Span txtListos = new Span("0");
    private final Span txtEntregados = new Span("0");

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
        root.setSpacing(false);
        root.getStyle().set("min-width", "0");
        root.getStyle().set("overflow", "hidden"); 
        root.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        VerticalLayout scrollContainer = new VerticalLayout();
        scrollContainer.setSizeFull();
        scrollContainer.getStyle().set("overflow-y", "auto");
        scrollContainer.getStyle().set("min-width", "0");
        
        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("1200px");
        content.setWidthFull();
        content.addClassNames(LumoUtility.Margin.Horizontal.AUTO, LumoUtility.Padding.MEDIUM);

        H1 title = new H1("Gestión de Operaciones");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.NONE);
        Button btnRefresh = new Button(VaadinIcon.REFRESH.create(), e -> actualizarTodo());
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(new VerticalLayout(title, new Span("Panel de control dinámico")), btnRefresh);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout stats = new HorizontalLayout(
            createStatCard("Nuevos", txtNuevos, VaadinIcon.BELL, "var(--lumo-error-color)"),
            createStatCard("En Preparación", txtPreparacion, VaadinIcon.FIRE, "var(--lumo-warning-color)"),
            createStatCard("Listos", txtListos, VaadinIcon.PACKAGE, "var(--lumo-success-color)"),
            createStatCard("Entregados", txtEntregados, VaadinIcon.CHECK_CIRCLE, "var(--lumo-contrast-50pct)")
        );
        stats.setWidthFull();

        configureGrid();

        content.add(header, stats, new H3("Cola de Trabajo"), grid);
        scrollContainer.add(content);
        root.add(scrollContainer);

        actualizarTodo(); 
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.setWidthFull();
        
        grid.addColumn(pedido -> {
        return pedido.getFecha() != null ? 
               pedido.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
    }).setHeader("Hora").setAutoWidth(true);

        grid.addColumn(Pedido::getTicketId).setHeader("ID Ticket").setAutoWidth(true);
        grid.addColumn(Pedido::getCliente).setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(Pedido::getTipo).setHeader("Tipo").setAutoWidth(true);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Estado").setAutoWidth(true);
    
        grid.addComponentColumn(pedido -> {
            String estado = pedido.getEstado().toUpperCase();
            String etiqueta;
            VaadinIcon icono = VaadinIcon.ARROW_RIGHT;
            boolean visible = true;

            switch (estado) {
                case "NUEVO" -> etiqueta = "Preparar";
                case "EN PREPARACION" -> etiqueta = "Listo";
                case "LISTO" -> etiqueta = "Entregar";
                default -> {
                    etiqueta = "Finalizado";
                    icono = VaadinIcon.CHECK;
                    visible = false;
                }
            }

            Button btn = new Button(etiqueta, icono.create());
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btn.setVisible(visible);
            btn.addClickListener(e -> {
                orderService.actualizarEstado(pedido.getId_db());
                actualizarTodo();
            });
            return btn;
        }).setHeader("Acción").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
    }

    private void actualizarTodo() {
        grid.setItems(orderService.getTodosLosPedidos());
        txtNuevos.setText(String.valueOf(orderService.contarPorEstado("NUEVO")));
        txtPreparacion.setText(String.valueOf(orderService.contarPorEstado("EN PREPARACION")));
        txtListos.setText(String.valueOf(orderService.contarPorEstado("LISTO")));
        txtEntregados.setText(String.valueOf(orderService.contarPorEstado("ENTREGADO")));
    }

    private Component createStatCard(String title, Span valueSpan, VaadinIcon icon, String color) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
        card.setWidthFull();
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon i = icon.create();
        i.getStyle().set("color", color);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);
        
        VerticalLayout info = new VerticalLayout(new Span(title), valueSpan);
        info.setPadding(false); info.setSpacing(false);

        card.add(i, info);
        return card;
    }

    private Span createStatusBadge(Pedido p) {
        String estado = p.getEstado().toUpperCase();
        Span badge = new Span(estado);
        badge.getElement().getThemeList().add("badge pill");
        
        switch (estado) {
            case "NUEVO" -> badge.getElement().getThemeList().add("error");
            case "EN PREPARACION" -> badge.getElement().getThemeList().add("warning");
            case "LISTO" -> badge.getElement().getThemeList().add("success");
            default -> badge.getStyle().set("background-color", "var(--lumo-contrast-20pct)");
        }
        return badge;
    }
}