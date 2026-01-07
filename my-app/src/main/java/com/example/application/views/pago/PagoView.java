package com.example.application.views.pago;

import com.example.application.model.Producto;
import com.example.application.services.*;
import com.example.application.repository.ProductoRepository;
import com.example.application.views.MainLayout;
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
 * VISTA DE PAGO Y FACTURACIÓN
 * Esta vista gestiona el proceso final de compra, permitiendo elegir método de entrega
 * y pago, simulando una pasarela segura y generando una factura legal en PDF.
 */
@PageTitle("Pago | TuFood")
@Route(value = "pago", layout = MainLayout.class)
public class PagoView extends VerticalLayout implements HasUrlParameter<String> {

    // Servicios necesarios para procesar la venta y consultar datos
    private final ShoppingCartService cartService;
    private final MesaService mesaService;
    private final ProductoRepository repo;

    // Componentes de la interfaz
    private VerticalLayout deliverySection, paymentSection, creditCardForm;
    private TextField addressField, cardNum, cardHolder, cardExp;
    private PasswordField cardCvv;
    private Button btnCard, btnCash, btnHome, btnStore, submit;
    
    // Variables de estado del proceso
    private boolean isCash = false;
    private boolean isDelivery = true;
    private double total = 0;
    private int mesaNum = -1;
    private boolean esMesa = false;

    public PagoView(ShoppingCartService cartService, MesaService mesaService, ProductoRepository repo) {
        this.cartService = cartService;
        this.mesaService = mesaService;
        this.repo = repo;

        // Estilo visual de la vista
        addClassName("perfil-view"); 
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Tarjeta contenedora principal
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("perfil-card", "perfil-card-content");
        card.setMaxWidth("600px");

        // --- SECCIÓN 1: MODO DE ENTREGA ---
        deliverySection = new VerticalLayout();
        deliverySection.setPadding(false);
        deliverySection.add(new H3("1. Modo de Entrega"));
        
        btnHome = new Button("A Domicilio", VaadinIcon.TRUCK.create(), e -> selectDelivery(true));
        btnStore = new Button("Recoger en Tienda", VaadinIcon.SHOP.create(), e -> selectDelivery(false));
        btnHome.setWidthFull(); btnStore.setWidthFull();
        btnHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Por defecto seleccionado
        
        addressField = new TextField("Dirección de entrega");
        addressField.setPlaceholder("Calle, Número, Piso...");
        addressField.setWidthFull();

        deliverySection.add(new HorizontalLayout(btnHome, btnStore), addressField);

        // --- SECCIÓN 2: MÉTODO DE PAGO ---
        paymentSection = new VerticalLayout();
        paymentSection.setPadding(false);
        paymentSection.add(new H3("2. Método de Pago"));
        
        btnCard = new Button("Tarjeta", VaadinIcon.CREDIT_CARD.create(), e -> selectPayment(false));
        btnCash = new Button("Efectivo", VaadinIcon.MONEY.create(), e -> selectPayment(true));
        btnCard.setWidthFull(); btnCash.setWidthFull();
        btnCard.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Formulario de tarjeta de crédito
        cardNum = new TextField("Número de Tarjeta");
        cardHolder = new TextField("Titular");
        cardExp = new TextField("Expiración (MM/YY)");
        cardCvv = new PasswordField("CVV");
        
        FormLayout form = new FormLayout(cardNum, cardHolder, cardExp, cardCvv);
        creditCardForm = new VerticalLayout(form);
        paymentSection.add(new HorizontalLayout(btnCard, btnCash), creditCardForm);

        // --- SECCIÓN 3: ACCIÓN FINAL ---
        submit = new Button("Confirmar Pago", e -> validarYProcesar());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        submit.setWidthFull();
        submit.setHeight("50px");

        card.add(deliverySection, new Hr(), paymentSection, new Hr(), submit);
        add(card);
    }

    /**
     * Alterna visualmente entre entrega a domicilio o recogida.
     */
    private void selectDelivery(boolean delivery) {
        this.isDelivery = delivery;
        btnHome.setThemeName(delivery ? "primary" : "");
        btnStore.setThemeName(delivery ? "" : "primary");
        addressField.setVisible(delivery);
    }

    /**
     * Alterna visualmente entre pago con tarjeta o efectivo.
     */
    private void selectPayment(boolean cash) {
        this.isCash = cash;
        btnCard.setThemeName(cash ? "" : "primary");
        btnCash.setThemeName(cash ? "primary" : "");
        creditCardForm.setVisible(!cash);
    }

    /**
     * Valida que los campos obligatorios estén rellenos antes de pagar.
     */
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
        process(); // Si todo es correcto, procesa el pago
    }

    private void notificarError(String mensaje) {
        Notification n = Notification.show(mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.setPosition(Notification.Position.MIDDLE);
    }

    /**
     * Captura los parámetros de la URL (Total, Mesa, Origen).
     * Esto permite que la vista funcione tanto para el Carrito como para la Sala.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        var params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("origen")) {
            // Caso: Venimos de Gestión de Mesas
            esMesa = true;
            total = Double.parseDouble(params.get("total").get(0));
            mesaNum = Integer.parseInt(params.get("mesa").get(0));
            deliverySection.setVisible(false); // No hay entrega si es en mesa
            submit.setText("Cobrar Mesa " + mesaNum + " (" + String.format("%.2f", total) + "€)");
        } else {
            // Caso: Venimos del Carrito de compras
            esMesa = false;
            total = cartService.getTotalPrice();
            deliverySection.setVisible(true);
            submit.setText("Pagar Pedido (" + String.format("%.2f", total) + "€)");
        }
    }

    /**
     * Simula el procesamiento del pago con un hilo secundario y barra de progreso.
     */
    private void process() {
        Dialog loading = new Dialog();
        ProgressBar pb = new ProgressBar(); pb.setIndeterminate(true);
        loading.add(new VerticalLayout(pb, new Span("Procesando pago seguro...")));
        loading.setCloseOnEsc(false); loading.setCloseOnOutsideClick(false);
        loading.open();
        
        // Identificamos los productos comprados según el origen
        Map<Producto, Integer> items = esMesa ? mesaService.obtenerDetalleProductosMesa(mesaNum, repo) : new HashMap<>(cartService.getCartContents());
        String mPago = isCash ? "EFECTIVO" : "TARJETA";
        String mEntrega = esMesa ? "MESA " + mesaNum : (isDelivery ? "A DOMICILIO" : "TIENDA");

        UI ui = UI.getCurrent();
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {} // Simulación de espera bancaria
            ui.access(() -> {
                // Finalizamos la transacción en la base de datos o servicio
                if (esMesa) mesaService.actualizarEstado(mesaNum, "LIBRE");
                else cartService.clearCart();
                
                loading.close();
                showInvoice(mPago, mEntrega, items); // Mostramos el ticket final
            });
        }).start();
    }

    /**
     * Muestra el ticket visual en pantalla y prepara el botón de descarga PDF.
     */
    private void showInvoice(String metodo, String modo, Map<Producto, Integer> items) {
        Dialog d = new Dialog();
        d.setWidth("450px");
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);

        // --- DISEÑO DEL TICKET VISUAL (HTML) ---
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

        // Listado dinámico de productos en el ticket
        VerticalLayout listado = new VerticalLayout();
        listado.setPadding(false);
        items.forEach((p, q) -> {
            HorizontalLayout row = new HorizontalLayout(new Span(q + "x " + p.getNombre()), new Span(String.format("%.2f€", p.getPrecio() * q)));
            row.setWidthFull(); row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            listado.add(row);
        });
        ticket.add(listado, new Hr());

        // Cálculo de impuestos (IVA 10%)
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

        // --- LÓGICA DE GENERACIÓN DE PDF PROFESIONAL ---
        StreamResource res = new StreamResource("Factura_TuFood.pdf", () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(); // Uso de OpenPDF (iText)
            try {
                PdfWriter.getInstance(doc, out); 
                doc.open();
                
                // Estilos de fuente
                com.lowagie.text.Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                
                // Cabecera PDF
                com.lowagie.text.Paragraph p1 = new com.lowagie.text.Paragraph("TUFOOD S.A. - FACTURA SIMPLIFICADA", bold);
                p1.setAlignment(Element.ALIGN_CENTER);
                doc.add(p1);
                doc.add(new com.lowagie.text.Paragraph("CIF: B-12345678 | Calle Principal 123\n\n"));
                
                doc.add(new com.lowagie.text.Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                doc.add(new com.lowagie.text.Paragraph("Referencia: " + (esMesa ? "Mesa " + mesaNum : "Web Order")));
                doc.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));

                // Cuerpo de la factura PDF
                items.forEach((p,q) -> { 
                    try { doc.add(new com.lowagie.text.Paragraph(q + "x " + p.getNombre() + " ................. " + String.format("%.2f", p.getPrecio()*q) + "€")); } 
                    catch(Exception ex) {} 
                });

                // Pie de factura
                doc.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));
                doc.add(new com.lowagie.text.Paragraph("TOTAL (IVA Incluido): " + String.format("%.2f", total) + "€"));
                doc.add(new com.lowagie.text.Paragraph("Metodo de pago: " + metodo));
                doc.add(new com.lowagie.text.Paragraph("\n¡Gracias por su visita!"));
                
                doc.close();
            } catch (Exception e) {}
            return new ByteArrayInputStream(out.toByteArray());
        });

        // Enlace de descarga para el PDF generado
        Anchor downloadLink = new Anchor();
        downloadLink.setHref(res);
        downloadLink.getElement().setAttribute("download", true);
        Button btnPdf = new Button("Descargar Factura PDF", VaadinIcon.DOWNLOAD.create());
        btnPdf.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnPdf.setWidthFull();
        downloadLink.add(btnPdf);

        // Botón para finalizar y navegar a la siguiente pantalla
        Button btnFinalizar = new Button("Continuar", e -> {
            d.close();
            if (esMesa) {
                UI.getCurrent().navigate("mesas"); // Vuelve al plano de sala
            } else {
                // Navega al seguimiento de pedido pasando datos por parámetros
                String dirFinal = isDelivery ? addressField.getValue() : "Recogida en Tienda";
                Map<String, List<String>> pMap = new HashMap<>();
                pMap.put("direccion", Collections.singletonList(dirFinal));
                pMap.put("metodo", Collections.singletonList(metodo));
                UI.getCurrent().navigate("seguimiento", new QueryParameters(pMap));
            }
        });
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFinalizar.setWidthFull();

        d.add(ticket);
        d.getFooter().add(btnFinalizar, downloadLink);
        d.open();
    }

    /**
     * Crea una fila alineada para el ticket de compra.
     */
    private HorizontalLayout crearFilaTicket(String etiqueta, String valor) {
        HorizontalLayout hl = new HorizontalLayout(new Span(etiqueta), new Span(valor));
        hl.setWidthFull();
        hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return hl;
    }
}