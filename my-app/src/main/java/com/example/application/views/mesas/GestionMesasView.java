package com.example.application.views.mesas;

import com.example.application.model.Mesa;
import com.example.application.security.AuthService;
import com.example.application.services.MesaService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Gestión de Mesas | Tu Food")
@Route(value = "mesas", layout = MainLayout.class)
@RolesAllowed({"TRABAJADOR", "ADMIN"})
public class GestionMesasView extends VerticalLayout {

    private final MesaService mesaService;
    private final AuthService authService;
    private final FlexLayout mesaContainer;

    public GestionMesasView(MesaService mesaService, AuthService authService) {
        this.mesaService = mesaService;
        this.authService = authService;

        // Configuración del contenedor principal
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("min-height", "100vh");

        // --- ENCABEZADO ---
        H1 title = new H1("Gestión de Mesas");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Top.MEDIUM);
        
        Paragraph subtitle = new Paragraph("Panel operativo de sala y facturación");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.LARGE);

        // --- BARRA ADMIN (Solo visible si es ADMIN) ---
        HorizontalLayout adminBar = new HorizontalLayout();
        adminBar.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        if (authService.isAdmin()) {
            Button btnAdd = new Button("Añadir Mesa", VaadinIcon.PLUS.create(), e -> {
                mesaService.añadirMesa();
                refreshMesas();
            });
            btnAdd.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

            Button btnRemove = new Button("Quitar Mesa", VaadinIcon.MINUS.create(), e -> {
                mesaService.eliminarUltimaMesa();
                refreshMesas();
            });
            btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            
            adminBar.add(btnAdd, btnRemove);
        }

        // --- CONTENEDOR DE MESAS (Lógica de 4 por fila y centrado) ---
        mesaContainer = new FlexLayout();
        mesaContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        mesaContainer.setJustifyContentMode(JustifyContentMode.CENTER); // Centra las mesas de la última fila si es impar
        
        // Limitamos el ancho para que quepan exactamente 4 mesas (180px card + 20px gap) * 4 = ~800px
        mesaContainer.setMaxWidth("850px"); 
        mesaContainer.getStyle().set("gap", "25px");

        add(title, subtitle, adminBar, mesaContainer);
        refreshMesas();
    }

    private void refreshMesas() {
        mesaContainer.removeAll();
        mesaService.obtenerTodas().forEach(mesa -> {
            mesaContainer.add(createMesaUI(mesa));
        });
    }

    private VerticalLayout createMesaUI(Mesa mesa) {
        boolean ocupada = "OCUPADA".equals(mesa.getEstado());
        
        VerticalLayout card = new VerticalLayout();
        card.setWidth("180px"); // Ancho fijo para mantener la cuadrícula
        card.setHeight("180px");
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setSpacing(true);
        
        // Estilo Premium
        card.getStyle()
            .set("border-radius", "24px")
            .set("cursor", "pointer")
            .set("transition", "all 0.3s ease")
            .set("box-shadow", "var(--lumo-box-shadow-s)")
            .set("background-color", "var(--lumo-base-color)")
            .set("border", "2px solid " + (ocupada ? "var(--lumo-error-color)" : "var(--lumo-success-color-50pct)"));

        // Efecto Hover (vía JavaScript o CSS inline)
        card.getElement().addEventListener("mouseenter", e -> card.getStyle().set("transform", "scale(1.05)"));
        card.getElement().addEventListener("mouseleave", e -> card.getStyle().set("transform", "scale(1.0)"));

        Icon icon = (ocupada ? VaadinIcon.SHOP : VaadinIcon.TABLE).create();
        icon.setSize("50px");
        icon.setColor(ocupada ? "var(--lumo-error-color)" : "var(--lumo-success-color)");

        Span label = new Span("MESA " + mesa.getNumeroMesa());
        label.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);
        
        Span status = new Span(mesa.getEstado());
        status.getElement().getThemeList().add("badge pill " + (ocupada ? "error" : "success"));

        card.add(icon, label, status);
        card.addClickListener(e -> openMesaDialog(mesa));
        
        return card;
    }

    private void openMesaDialog(Mesa mesa) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Gestión Mesa " + mesa.getNumeroMesa());
        
        boolean estaOcupada = "OCUPADA".equals(mesa.getEstado());
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        
        if (estaOcupada) {
            content.add(new Span("Mesa con servicio en curso."));
            // Podrías añadir aquí: content.add(new H3("Total: " + mesa.getTotalAcumulado() + " €"));
        } else {
            content.add(new Span("¿Desea abrir esta mesa para una nueva comanda?"));
        }

        Button btnAddProduct = new Button("Abrir Comanda / Añadir", VaadinIcon.PLUS.create(), e -> {
            mesaService.actualizarEstado(mesa.getNumeroMesa(), "OCUPADA");
            refreshMesas();
            dialog.close();
        });
        btnAddProduct.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAddProduct.setWidthFull();

        Button btnCobrar = new Button("Finalizar y Cobrar", VaadinIcon.CASH.create(), e -> {
            mesaService.actualizarEstado(mesa.getNumeroMesa(), "LIBRE");
            Notification.show("Mesa liberada");
            refreshMesas();
            dialog.close();
        });
        btnCobrar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnCobrar.setVisible(estaOcupada);
        btnCobrar.setWidthFull();

        dialog.add(content);
        dialog.getFooter().add(new Button("Cerrar", e -> dialog.close()), btnAddProduct, btnCobrar);
        dialog.open();
    }
}