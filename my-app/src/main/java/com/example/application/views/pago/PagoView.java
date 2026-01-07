package com.example.application.views.pago;

import com.example.application.model.Producto;
import com.example.application.services.*;
import com.example.application.repository.ProductoRepository;
import com.example.application.views.MainLayout;
import com.example.application.views.main.HomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * VISTA DE PAGO Y FACTURACIÓN (ACTUALIZADA)
 * Gestiona el proceso de compra con botones optimizados para evitar desbordamientos.
 */
@PageTitle("Pago | TuFood")
@Route(value = "pago", layout = MainLayout.class)
public class PagoView extends VerticalLayout implements HasUrlParameter<String> {

    private final ShoppingCartService cartService;
    private final MesaService mesaService;
    private final ProductoRepository repo;

    private VerticalLayout deliverySection, paymentSection, creditCardForm;
    private TextField addressField, cardNum, cardHolder, cardExp;
    private PasswordField cardCvv;
    private Button btnCard, btnCash, btnHome, btnStore, submit, btnCancelar;
    
    private boolean isCash = false;
    private boolean isDelivery = true;
    private double total = 0;
    private int mesaNum = -1;
    private boolean esMesa = false;

    public PagoView(ShoppingCartService cartService, MesaService mesaService, ProductoRepository repo) {
        this.cartService = cartService;
        this.mesaService = mesaService;
        this.repo = repo;

        addClassName("perfil-view"); 
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content");
        card.setMaxWidth("600px");
        card.setPadding(true);

        // --- 1. MODO DE ENTREGA ---
        deliverySection = new VerticalLayout();
        deliverySection.setPadding(false);
        deliverySection.add(new H3("1. Modo de Entrega"));
        
        btnHome = new Button("A Domicilio", VaadinIcon.TRUCK.create(), e -> selectDelivery(true));
        btnStore = new Button("Recoger en Tienda", VaadinIcon.SHOP.create(), e -> selectDelivery(false));
        btnHome.setWidthFull(); btnStore.setWidthFull();
        btnHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        addressField = new TextField("Dirección de entrega");
        addressField.setPlaceholder("Calle, Número, Piso...");
        addressField.setWidthFull();

        deliverySection.add(new HorizontalLayout(btnHome, btnStore), addressField);

        // --- 2. MÉTODO DE PAGO ---
        paymentSection = new VerticalLayout();
        paymentSection.setPadding(false);
        paymentSection.add(new H3("2. Método de Pago"));
        
        btnCard = new Button("Tarjeta", VaadinIcon.CREDIT_CARD.create(), e -> selectPayment(false));
        btnCash = new Button("Efectivo", VaadinIcon.MONEY.create(), e -> selectPayment(true));
        btnCard.setWidthFull(); btnCash.setWidthFull();
        btnCard.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        cardNum = new TextField("Número de Tarjeta");
        cardHolder = new TextField("Titular");
        cardExp = new TextField("MM/YY");
        cardCvv = new PasswordField("CVV");
        
        FormLayout form = new FormLayout(cardNum, cardHolder, cardExp, cardCvv);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2)); 
        creditCardForm = new VerticalLayout(form);
        paymentSection.add(new HorizontalLayout(btnCard, btnCash), creditCardForm);

        // --- 3. ACCIÓN FINAL (BOTONES PEQUEÑOS Y ALINEADOS) ---
        // Botón de Confirmar Pago (Tamaño reducido)
        submit = new Button("Confirmar Pago", e -> validarYProcesar());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        // Botón de Cancelar Pedido (Tamaño reducido)
        btnCancelar = new Button("Cancelar", VaadinIcon.CLOSE.create(), e -> {
            UI.getCurrent().navigate(HomeView.class);
        });
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        // Layout horizontal para evitar desbordamientos
        HorizontalLayout actionsLayout = new HorizontalLayout(btnCancelar, submit);
        actionsLayout.setWidthFull();
        actionsLayout.setJustifyContentMode(JustifyContentMode.END); // Alineados a la derecha
        actionsLayout.setSpacing(true);

        card.add(deliverySection, new Hr(), paymentSection, new Hr(), actionsLayout);
        add(card);
    }

    private void selectDelivery(boolean delivery) {
        this.isDelivery = delivery;
        btnHome.setThemeName(delivery ? "primary" : "");
        btnStore.setThemeName(delivery ? "" : "primary");
        addressField.setVisible(delivery);
    }

    private void selectPayment(boolean cash) {
        this.isCash = cash;
        btnCard.setThemeName(cash ? "" : "primary");
        btnCash.setThemeName(cash ? "primary" : "");
        creditCardForm.setVisible(!cash);
    }

    private void validarYProcesar() {
        if (!esMesa && isDelivery && addressField.getValue().trim().isEmpty()) {
            notificarError("Por favor, introduce una dirección de entrega");
            return;
        }
        if (!isCash) {
            if (cardNum.getValue().trim().isEmpty() || cardHolder.getValue().trim().isEmpty() || 
                cardExp.getValue().trim().isEmpty() || cardCvv.getValue().trim().isEmpty()) {
                notificarError("Completa todos los datos de la tarjeta");
                return;
            }
        }
        process(); 
    }

    private void notificarError(String mensaje) {
        Notification n = Notification.show(mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.setPosition(Notification.Position.MIDDLE);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        var params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("origen")) {
            esMesa = true;
            total = Double.parseDouble(params.get("total").get(0));
            mesaNum = Integer.parseInt(params.get("mesa").get(0));
            deliverySection.setVisible(false);
            submit.setText("Cobrar Mesa " + mesaNum + " (" + String.format("%.2f", total) + "€)");
        } else {
            esMesa = false;
            total = cartService.getTotalPrice();
            deliverySection.setVisible(true);
            submit.setText("Pagar (" + String.format("%.2f", total) + "€)");
        }
    }

    private void process() {
        Dialog loading = new Dialog();
        ProgressBar pb = new ProgressBar(); pb.setIndeterminate(true);
        loading.add(new VerticalLayout(pb, new Span("Procesando pago seguro...")));
        loading.setCloseOnEsc(false); loading.setCloseOnOutsideClick(false);
        loading.open();
        
        Map<Producto, Integer> items = esMesa ? mesaService.obtenerDetalleProductosMesa(mesaNum, repo) : new HashMap<>(cartService.getCartContents());
        String mPago = isCash ? "EFECTIVO" : "TARJETA";
        String mEntrega = esMesa ? "MESA " + mesaNum : (isDelivery ? "A DOMICILIO" : "TIENDA");

        UI ui = UI.getCurrent();
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            ui.access(() -> {
                if (esMesa) mesaService.actualizarEstado(mesaNum, "LIBRE");
                else cartService.clearCart();
                
                loading.close();
                showInvoice(mPago, mEntrega, items);
            });
        }).start();
    }

    private void showInvoice(String metodo, String modo, Map<Producto, Integer> items) {
        Dialog d = new Dialog();
        d.setWidth("450px");
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);

        VerticalLayout ticket = new VerticalLayout();
        ticket.setAlignItems(Alignment.CENTER);
        ticket.setSpacing(false);
        ticket.setPadding(true);

        H2 marca = new H2("TuFood S.A.");
        marca.getStyle().set("margin-bottom", "0");
        ticket.add(marca, new Span("CIF: B-12345678"), new Span("Calle Principal 123, Jerez"), new Hr());
        
        ticket.add(new Span("FECHA: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        ticket.add(new Span(esMesa ? "SERVICIO DE MESA: " + mesaNum : "PEDIDO ONLINE"));
        ticket.add(new Hr());

        VerticalLayout listado = new VerticalLayout();
        listado.setPadding(false);
        items.forEach((p, q) -> {
            HorizontalLayout row = new HorizontalLayout(new Span(q + "x " + p.getNombre()), new Span(String.format("%.2f€", p.getPrecio() * q)));
            row.setWidthFull(); row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            listado.add(row);
        });
        ticket.add(listado, new Hr());

        double baseImponible = total / 1.10;
        double iva = total - baseImponible;

        ticket.add(crearFilaTicket("Base Imponible:", String.format("%.2f€", baseImponible)));
        ticket.add(crearFilaTicket("IVA (10%):", String.format("%.2f€", iva)));
        
        H3 totalTxt = new H3("TOTAL: " + String.format("%.2f", total) + "€");
        totalTxt.getStyle().set("color", "var(--lumo-primary-color)");
        ticket.add(totalTxt, new Hr());

        ticket.add(new Span("MÉTODO DE PAGO: " + metodo));
        if (!esMesa) ticket.add(new Span("ENTREGA: " + modo));
        ticket.add(new Hr(), new Paragraph("¡Gracias por confiar en nosotros!"));

        StreamResource res = new StreamResource("Factura_TuFood.pdf", () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document();
            try {
                PdfWriter.getInstance(doc, out); 
                doc.open();
                com.lowagie.text.Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                com.lowagie.text.Paragraph p1 = new com.lowagie.text.Paragraph("TUFOOD S.A. - FACTURA SIMPLIFICADA", bold);
                p1.setAlignment(Element.ALIGN_CENTER);
                doc.add(p1);
                doc.add(new com.lowagie.text.Paragraph("CIF: B-12345678 | Calle Principal 123\n\n"));
                doc.add(new com.lowagie.text.Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                doc.add(new com.lowagie.text.Paragraph("Referencia: " + (esMesa ? "Mesa " + mesaNum : "Web Order")));
                doc.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));
                items.forEach((p,q) -> { 
                    try { doc.add(new com.lowagie.text.Paragraph(q + "x " + p.getNombre() + " ................. " + String.format("%.2f", p.getPrecio()*q) + "€")); } 
                    catch(Exception ex) {} 
                });
                doc.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));
                doc.add(new com.lowagie.text.Paragraph("TOTAL (IVA Incluido): " + String.format("%.2f", total) + "€"));
                doc.add(new com.lowagie.text.Paragraph("Metodo de pago: " + metodo));
                doc.add(new com.lowagie.text.Paragraph("\n¡Gracias por su visita!"));
                doc.close();
            } catch (Exception e) {}
            return new ByteArrayInputStream(out.toByteArray());
        });

        Anchor downloadLink = new Anchor();
        downloadLink.setHref(res);
        downloadLink.getElement().setAttribute("download", true);
        Button btnPdf = new Button("Factura PDF", VaadinIcon.DOWNLOAD.create());
        btnPdf.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        downloadLink.add(btnPdf);

        Button btnFinalizar = new Button("Continuar", e -> {
            d.close();
            UI.getCurrent().navigate(HomeView.class);
        });
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        d.add(ticket);
        d.getFooter().add(btnFinalizar, downloadLink);
        d.open();
    }

    private HorizontalLayout crearFilaTicket(String etiqueta, String valor) {
        HorizontalLayout hl = new HorizontalLayout(new Span(etiqueta), new Span(valor));
        hl.setWidthFull();
        hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return hl;
    }
}