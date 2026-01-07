package com.example.application.views.mesas;

import com.example.application.model.Mesa;
import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import com.example.application.services.MesaService;
import com.example.application.views.MainLayout;
import com.example.application.views.pago.PagoView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.Map;

@PageTitle("Sala | TuFood")
@Route(value = "mesas", layout = MainLayout.class)
@RolesAllowed({"TRABAJADOR", "ADMIN"})
public class GestionMesasView extends VerticalLayout {

    private final MesaService mesaService;
    private final ProductoRepository repo;
    private final FlexLayout container;

    public GestionMesasView(MesaService mesaService, ProductoRepository repo) {
        this.mesaService = mesaService;
        this.repo = repo;
        
        addClassName("mesas-view");
        setAlignItems(Alignment.CENTER);
        
        container = new FlexLayout();
        container.addClassName("mesa-grid");
        container.setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H1("Gestión de Sala"), container);
        refresh();
    }

    private void refresh() {
        container.removeAll();
        mesaService.obtenerTodas().forEach(mesa -> {
            boolean ocupada = "OCUPADA".equals(mesa.getEstado());
            VerticalLayout card = new VerticalLayout();
            card.addClassNames("mesa-item", ocupada ? "occupied" : "free");
            
            Span label = new Span("MESA " + mesa.getNumeroMesa());
            label.addClassName("mesa-label");
            
            Span total = new Span(String.format("%.2f €", mesa.getTotalAcumulado()));
            Span badge = new Span(ocupada ? "Ocupada" : "Libre");
            badge.addClassName("status-indicator");

            card.add(label, total, badge);
            card.addClickListener(e -> openDialog(mesa));
            container.add(card);
        });
    }

    private void openDialog(Mesa mesa) {
        Dialog d = new Dialog("Mesa " + mesa.getNumeroMesa());
        boolean ocupada = "OCUPADA".equals(mesa.getEstado());

        Button btnComanda = new Button(ocupada ? "Editar Comanda" : "Abrir Mesa", e -> {
            if (!ocupada) mesaService.actualizarEstado(mesa.getNumeroMesa(), "OCUPADA");
            d.close();
            openComanda(mesa);
        });
        btnComanda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnPago = new Button("Cobrar", VaadinIcon.CASH.create(), e -> {
            d.close();
            UI.getCurrent().navigate(PagoView.class, QueryParameters.simple(Map.of(
                "total", String.valueOf(mesa.getTotalAcumulado()),
                "mesa", String.valueOf(mesa.getNumeroMesa()),
                "origen", "mesas"
            )));
        });
        btnPago.setVisible(ocupada);
        btnPago.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        d.getFooter().add(new Button("Cancelar", x -> d.close()), btnComanda, btnPago);
        d.open();
    }

    private void openComanda(Mesa mesa) {
        Dialog pd = new Dialog("Comanda");
        pd.setWidth("700px");
        Grid<Producto> grid = new Grid<>(Producto.class, false);
        grid.addColumn(Producto::getNombre).setHeader("Producto");
        grid.addComponentColumn(p -> {
            int c = mesaService.obtenerCantidadProductoEnMesa(mesa.getNumeroMesa(), p.getId());
            Button mas = new Button(VaadinIcon.PLUS.create(), e -> { mesaService.modificarCantidad(mesa.getNumeroMesa(), p.getId(), 1, p.getPrecio()); grid.getDataProvider().refreshItem(p); });
            Button menos = new Button(VaadinIcon.MINUS.create(), e -> { if(c>0) { mesaService.modificarCantidad(mesa.getNumeroMesa(), p.getId(), -1, p.getPrecio()); grid.getDataProvider().refreshItem(p); } });
            return new HorizontalLayout(menos, new Span(String.valueOf(c)), mas);
        }).setHeader("Cant.");
        grid.setItems(repo.findAll());
        pd.add(grid);
        pd.getFooter().add(new Button("Finalizar", e -> { pd.close(); refresh(); }));
        pd.open();
    }
}