package com.example.application.views.mesas;

import com.example.application.model.Mesa;
import com.example.application.model.Pedido;
import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.services.MesaService;
import com.example.application.services.OrderService;
import com.example.application.views.MainLayout;
import com.example.application.views.pago.PagoView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vista para la gestión de mesas del restaurante.
 * Restringida a usuarios con rol TRABAJADOR o ADMIN.
 */
@PageTitle("Sala | TuFood")
@Route(value = "mesas", layout = MainLayout.class)
@RolesAllowed({"TRABAJADOR", "ADMIN"})
public class GestionMesasView extends VerticalLayout {

    private final MesaService mesaService;
    private final OrderService orderService;
    private final ProductoRepository repo;
    private final FlexLayout container;

    public GestionMesasView(MesaService mesaService, OrderService orderService, ProductoRepository repo) {
        this.mesaService = mesaService;
        this.orderService = orderService;
        this.repo = repo;
        
        // Configuración visual del contenedor principal
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        getStyle().set("background-color", "#f5f5f5");
        
        // Contenedor flexible para que las mesas se ajusten a la pantalla (Wrap)
        container = new FlexLayout();
        container.setWidthFull();
        container.setMaxWidth("1200px"); 
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        container.getStyle().set("gap", "30px");
        container.getStyle().set("padding", "20px");

        H1 titulo = new H1("Gestión de Sala");
        titulo.getStyle().set("color", "var(--lumo-primary-color)");
        
        add(titulo, new Hr(), container);
        refresh(); // Carga inicial de las mesas
    }

    /**
     * Dibuja o redibuja las tarjetas de las mesas basándose en su estado actual.
     */
    private void refresh() {
        container.removeAll();
        mesaService.obtenerTodas().forEach(mesa -> {
            boolean ocupada = "OCUPADA".equals(mesa.getEstado());
            double total = mesa.getTotalAcumulado();
            
            VerticalLayout card = new VerticalLayout();
            card.setAlignItems(Alignment.CENTER);
            card.setJustifyContentMode(JustifyContentMode.CENTER);
            card.setSpacing(false);
            card.setWidth("250px");
            card.setHeight("240px");
            
            // Estilo dinámico según ocupación
            if (ocupada) {
                card.getStyle().set("background-color", "var(--lumo-primary-color)");
                card.getStyle().set("color", "white");
            } else {
                card.getStyle().set("background-color", "white");
                card.getStyle().set("color", "var(--lumo-body-text-color)");
                card.getStyle().set("border", "1px solid #e0e0e0");
            }
            
            card.getStyle().set("border-radius", "25px");
            card.getStyle().set("cursor", "pointer");
            card.getStyle().set("box-shadow", "0 4px 15px rgba(0,0,0,0.1)");

            Icon iconoMesa = VaadinIcon.TABLE.create();
            iconoMesa.setSize("50px");
            iconoMesa.setColor(ocupada ? "white" : "var(--lumo-primary-color)");

            Span label = new Span("MESA " + mesa.getNumeroMesa());
            label.getStyle().set("font-weight", "800").set("font-size", "1.3rem");
            
            Span totalStr = new Span(String.format("%.2f €", total));
            totalStr.getStyle().set("margin", "10px 0");
            
            card.add(iconoMesa, label, totalStr);

            // Botón de emergencia para liberar mesa si se queda en estado 'Ocupada' sin productos
            if (ocupada && total <= 0) {
                Button btnLimpiar = new Button("LIBERAR MESA", e -> {
                    mesaService.actualizarEstado(mesa.getNumeroMesa(), "LIBRE");
                    refresh();
                });
                btnLimpiar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnLimpiar.getStyle().set("color", "white").set("text-decoration", "underline");
                card.add(btnLimpiar);
            }

            card.addClickListener(e -> openDialog(mesa));
            container.add(card);
        });
    }

    /**
     * Abre las opciones principales al hacer clic en una mesa.
     */
    private void openDialog(Mesa mesa) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Mesa " + mesa.getNumeroMesa());
        boolean ocupada = "OCUPADA".equals(mesa.getEstado());

        // Botón para abrir comanda o gestionar la existente
        Button btnComanda = new Button(ocupada ? "Gestionar Pedido" : "Abrir Mesa", e -> {
            if (!ocupada) mesaService.actualizarEstado(mesa.getNumeroMesa(), "OCUPADA");
            d.close();
            openComanda(mesa);
        });
        btnComanda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnComanda.setWidthFull();

        // Botón para navegar a la vista de pago
        Button btnPago = new Button("Cobrar Cuenta", e -> {
            d.close();
            UI.getCurrent().navigate(PagoView.class, QueryParameters.simple(Map.of(
                "total", String.valueOf(mesa.getTotalAcumulado()),
                "mesa", String.valueOf(mesa.getNumeroMesa()),
                "origen", "mesas"
            )));
        });
        // Solo visible si hay algo que cobrar
        btnPago.setVisible(ocupada && mesa.getTotalAcumulado() > 0);
        btnPago.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnPago.setWidthFull();

        d.add(new VerticalLayout(btnComanda, btnPago));
        d.getFooter().add(new Button("Cerrar", x -> d.close()));
        d.open();
    }

    /**
     * Diálogo interactivo para añadir/quitar productos de la mesa.
     */
    private void openComanda(Mesa mesa) {
        Dialog pd = new Dialog("Comanda: Mesa " + mesa.getNumeroMesa());
        pd.setWidth("800px");
        pd.setCloseOnEsc(false);
        pd.setCloseOnOutsideClick(false);
        
        Grid<Producto> grid = new Grid<>(Producto.class, false);
        grid.addColumn(Producto::getNombre).setHeader("Plato");
        grid.addColumn(p -> String.format("%.2f€", p.getPrecio())).setHeader("Precio");
        
        // Columna con botones +/- para gestionar cantidades
        grid.addComponentColumn(p -> {
            int c = mesaService.obtenerCantidadProductoEnMesa(mesa.getNumeroMesa(), p.getId());
            Button mas = new Button(VaadinIcon.PLUS.create(), e -> { 
                mesaService.modificarCantidad(mesa.getNumeroMesa(), p.getId(), 1, p.getPrecio()); 
                grid.getDataProvider().refreshItem(p); 
            });
            Button menos = new Button(VaadinIcon.MINUS.create(), e -> { 
                if(c > 0) { 
                    mesaService.modificarCantidad(mesa.getNumeroMesa(), p.getId(), -1, p.getPrecio()); 
                    grid.getDataProvider().refreshItem(p); 
                } 
            });
            return new HorizontalLayout(menos, new Span(String.valueOf(c)), mas);
        }).setHeader("Cant.");

        grid.setItems(repo.findAll());
        pd.add(grid);

        // Envía la información al OrderService para que aparezca en el monitor de cocina
        Button btnCocina = new Button("MANDAR A COCINA", VaadinIcon.FIRE.create(), e -> {
            double totalActual = mesaService.obtenerTodas().stream()
                .filter(m -> m.getNumeroMesa() == mesa.getNumeroMesa())
                .findFirst().get().getTotalAcumulado();

            if (totalActual > 0) {
                registrarPedidoEnCocina(mesa);
                pd.close();
                refresh();
            } else {
                Notification.show("No hay productos seleccionados").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnCocina.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        // Botón para cerrar el diálogo. Si la mesa está vacía, se libera automáticamente.
        Button btnCancelar = new Button("CANCELAR / VOLVER", e -> {
            double totalActual = mesaService.obtenerTodas().stream()
                .filter(m -> m.getNumeroMesa() == mesa.getNumeroMesa())
                .findFirst().get().getTotalAcumulado();
            
            if (totalActual <= 0) {
                mesaService.actualizarEstado(mesa.getNumeroMesa(), "LIBRE");
            }
            pd.close();
            refresh();
        });
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout footerLayout = new HorizontalLayout(btnCancelar, btnCocina);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        pd.getFooter().add(footerLayout);
        pd.open();
    }

    /**
     * Crea un objeto Pedido y lo persiste para su gestión en cocina.
     */
    private void registrarPedidoEnCocina(Mesa mesa) {
        Pedido pedido = new Pedido();
        // Genera un ID de ticket legible pero único
        pedido.setTicketId("M" + mesa.getNumeroMesa() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        pedido.setCliente("Mesa " + mesa.getNumeroMesa());
        pedido.setTipo("LOCAL");
        pedido.setEstado("NUEVO");
        pedido.setFecha(LocalDateTime.now());
        orderService.registrarPedido(pedido);
        Notification.show("Pedido enviado a cocina").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}