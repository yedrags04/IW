package com.example.application.views.mesas;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Gestión de Mesas | Tu Food")
@Route(value = "mesas", layout = MainLayout.class)
@RolesAllowed({"TRABAJADOR", "ADMIN"})
public class GestionMesasView extends VerticalLayout {

    private FlexLayout mesaContainer;
    private int contadorMesas = 10; // Empezamos con 10 mesas

    public GestionMesasView() {
        addClassName("mesas-view");
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Panel de Mesas");
        Paragraph subtitle = new Paragraph("Administre las mesas del local y sus comandas.");
        
        // --- BARRA DE ACCIONES SUPERIOR ---
        HorizontalLayout adminBar = new HorizontalLayout();
        adminBar.addClassName("admin-table-bar");

        Button btnAddTable = new Button("Añadir Mesa", VaadinIcon.PLUS.create(), e -> añadirMesa());
        btnAddTable.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        Button btnRemoveTable = new Button("Quitar Mesa", VaadinIcon.MINUS.create(), e -> quitarMesa());
        btnRemoveTable.addThemeVariants(ButtonVariant.LUMO_ERROR);

        adminBar.add(btnAddTable, btnRemoveTable);
        add(title, subtitle, adminBar);

        // Contenedor de mesas
        mesaContainer = new FlexLayout();
        mesaContainer.addClassName("mesa-grid");
        mesaContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        mesaContainer.setJustifyContentMode(JustifyContentMode.CENTER);

        renderizarMesas();
        add(mesaContainer);
    }

    private void renderizarMesas() {
        mesaContainer.removeAll();
        for (int i = 1; i <= contadorMesas; i++) {
            boolean ocupada = (i % 3 == 0); 
            mesaContainer.add(createMesa(i, ocupada));
        }
    }

    private void añadirMesa() {
        contadorMesas++;
        renderizarMesas();
        Notification.show("Mesa " + contadorMesas + " añadida.");
    }

    private void quitarMesa() {
        if (contadorMesas > 0) {
            Notification.show("Mesa " + contadorMesas + " eliminada.");
            contadorMesas--;
            renderizarMesas();
        }
    }

    private Button createMesa(int numero, boolean ocupada) {
        Button mesaBtn = new Button();
        mesaBtn.addClassName("mesa-item");

        Icon icon = VaadinIcon.TABLE.create();
        Span label = new Span("Mesa " + numero);
        label.addClassName("mesa-label");

        Span statusIndicator = new Span(ocupada ? "OCUPADA" : "LIBRE");
        statusIndicator.addClassName("status-indicator");

        VerticalLayout layout = new VerticalLayout(icon, label, statusIndicator);
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(false);
        layout.setPadding(false);

        mesaBtn.getElement().appendChild(layout.getElement());
        
        if (ocupada) {
            mesaBtn.addThemeNames("occupied");
            mesaBtn.addClickListener(e -> showComandaDialog(numero));
        } else {
            mesaBtn.addThemeNames("free");
            mesaBtn.addClickListener(e -> showComandaDialog(numero)); // Ahora permitimos abrir para añadir comanda
        }

        return mesaBtn;
    }

    private void showComandaDialog(int mesaNum) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Comanda - Mesa " + mesaNum);
        
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        // Simulación de comanda
        UnorderedList list = new UnorderedList();
        list.addClassName("comanda-list");
        list.add(new ListItem("2x Classic Burger - 25.00€"));
        
        // --- BOTÓN PARA AÑADIR PRODUCTO A LA COMANDA ---
        Button btnAddProduct = new Button("Añadir Producto", VaadinIcon.PLUS_CIRCLE.create());
        btnAddProduct.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnAddProduct.setWidthFull();
        btnAddProduct.addClickListener(e -> {
            Notification.show("Abriendo catálogo para Mesa " + mesaNum);
            // Aquí podrías abrir otro diálogo con la lista de productos
        });

        H4 total = new H4("Total: 25.00 €");
        total.getStyle().set("color", "var(--lumo-primary-color)");

        dialogContent.add(list, btnAddProduct, total);
        dialog.add(dialogContent);

        Button closeButton = new Button("Cerrar", e -> dialog.close());
        Button printButton = new Button("Cobrar", new Icon(VaadinIcon.PRINT));
        printButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialog.getFooter().add(closeButton, printButton);
        dialog.open();
    }
}