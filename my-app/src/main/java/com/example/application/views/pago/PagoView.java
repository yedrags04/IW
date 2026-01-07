package com.example.application.views.pago;

import com.example.application.model.Pedido;
import com.example.application.model.Producto;
import com.example.application.services.OrderService;
import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.example.application.views.seguimiento.SeguimientoView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.vaadin.flow.server.StreamResource;

// Imports específicos para PDF para evitar conflictos con los de Vaadin
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PageTitle("Pago | TuFood")
@Route(value = "pago", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.CREDIT_CARD_SOLID)
public class PagoView extends VerticalLayout {

    private final ShoppingCartService cartService;
    private final OrderService orderService;

    private TextField cardNumber, cardholderName, address, city, zipCode;
    private Select<Integer> month, year;
    private PasswordField csc;
    private Button submit;
    
    private VerticalLayout creditCardForm, cashInfo, storeInfo;
    private FormLayout addressLayout;
    
    private boolean isCashSelected = false;
    private boolean isDeliverySelected = true;

    public PagoView(ShoppingCartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout cardContainer = new VerticalLayout();
        cardContainer.setMaxWidth("600px");
        cardContainer.setWidthFull();
        cardContainer.setPadding(true);
        cardContainer.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.MEDIUM);

        H3 paymentTitle = new H3();
        paymentTitle.add(VaadinIcon.WALLET.create(), new Span(" Método de Pago"));
        
        HorizontalLayout methodSelector = createMethodSelector();
        creditCardForm = createCreditCardForm();
        cashInfo = createCashPanel();
        cashInfo.setVisible(false);

        H3 deliveryTitle = new H3();
        deliveryTitle.add(VaadinIcon.MAP_MARKER.create(), new Span(" Modo de Entrega"));
        
        HorizontalLayout deliverySelector = createDeliverySelector();
        addressLayout = createAddressLayout();
        storeInfo = createStorePanel();
        storeInfo.setVisible(false);

        cardContainer.add(
            paymentTitle, methodSelector, creditCardForm, cashInfo,
            deliveryTitle, deliverySelector, addressLayout, storeInfo,
            createButtonLayout()
        );
        
        add(cardContainer);
        setupEvents();
    }

    private void showLoadingAndProcess() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false); dialog.setCloseOnOutsideClick(false);
        ProgressBar pb = new ProgressBar(); pb.setIndeterminate(true);
        VerticalLayout vl = new VerticalLayout(pb, new Span("Validando transacción segura..."));
        vl.setAlignItems(Alignment.CENTER); dialog.add(vl); dialog.open();

        String metodoFinal = isCashSelected ? "Efectivo" : "Tarjeta";
        String direccionFinal = isDeliverySelected ? (address.getValue() + ", " + city.getValue()) : "Recogida en Tienda";
        String idGenerado = UUID.randomUUID().toString().substring(0, 4).toUpperCase(); // Sin # para evitar errores en nombre de archivo
        
        Map<Producto, Integer> productosFactura = new HashMap<>(cartService.getCartContents());
        double totalFactura = cartService.getTotalPrice();

        UI ui = UI.getCurrent();
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                ui.access(() -> {
                    Pedido nuevoPedido = new Pedido("#" + idGenerado, "Cliente Web", isDeliverySelected ? "DOMICILIO" : "TIENDA", direccionFinal);
                    orderService.registrarPedido(nuevoPedido);

                    dialog.close();
                    cartService.clearCart(); 
                    
                    abrirPreguntaFactura(idGenerado, metodoFinal, direccionFinal, productosFactura, totalFactura);
                });
            } catch (Exception ex) { ui.access(dialog::close); }
        }).start();
    }

    private void abrirPreguntaFactura(String id, String metodo, String direccion, Map<Producto, Integer> productos, double total) {
        Dialog questionDialog = new Dialog();
        questionDialog.setHeaderTitle("¡Pedido Realizado!");
        
        VerticalLayout layout = new VerticalLayout(
            new Span("Tu pedido #" + id + " ha sido procesado."),
            new Span("¿Deseas generar la factura detallada ahora mismo?")
        );
        questionDialog.add(layout);

        Button btnSi = new Button("Sí, generar factura", VaadinIcon.FILE_TEXT.create(), e -> {
            questionDialog.close();
            mostrarTicketFactura(id, metodo, direccion, productos, total);
        });
        btnSi.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button btnNo = new Button("No, ir al seguimiento", e -> {
            questionDialog.close();
            irASeguimiento(metodo, direccion);
        });

        questionDialog.getFooter().add(btnNo, btnSi);
        questionDialog.open();
    }

    private void mostrarTicketFactura(String id, String metodo, String direccion, Map<Producto, Integer> productos, double total) {
        Dialog invoiceDialog = new Dialog();
        invoiceDialog.setWidth("450px");

        VerticalLayout ticketLayout = new VerticalLayout();
        ticketLayout.setAlignItems(Alignment.CENTER);
        ticketLayout.setSpacing(false);

        ticketLayout.add(new H2("TuFood S.A."));
        ticketLayout.add(new Span("CIF: B-12345678"));
        ticketLayout.add(new Hr());
        
        ticketLayout.add(new Span("FECHA: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        ticketLayout.add(new Span("PEDIDO: #" + id));
        ticketLayout.add(new Hr());

        VerticalLayout itemsContainer = new VerticalLayout();
        itemsContainer.setPadding(false);
        
        productos.forEach((producto, cantidad) -> {
            HorizontalLayout row = new HorizontalLayout(
                new Span(cantidad + "x " + producto.getNombre()),
                new Span(String.format("%.2f€", producto.getPrecio() * cantidad))
            );
            row.setWidthFull();
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            itemsContainer.add(row);
        });
        
        ticketLayout.add(itemsContainer);
        ticketLayout.add(new Hr());

        double iva = total * 0.10;
        double subtotal = total - iva;

        ticketLayout.add(crearFilaTicket("Base Imponible:", String.format("%.2f€", subtotal)));
        ticketLayout.add(crearFilaTicket("IVA (10%):", String.format("%.2f€", iva)));
        
        H3 totalTxt = new H3(String.format("TOTAL: %.2f€", total));
        totalTxt.addClassName(LumoUtility.TextColor.PRIMARY);
        ticketLayout.add(totalTxt);

        ticketLayout.add(new Hr());
        ticketLayout.add(new Span("MÉTODO: " + metodo.toUpperCase()));
        ticketLayout.add(new Span("ENTREGA: " + direccion));
        ticketLayout.add(new Hr());
        ticketLayout.add(new com.vaadin.flow.component.html.Paragraph("¡Gracias por su compra!"));

        invoiceDialog.add(ticketLayout);

        // --- LÓGICA DE DESCARGA PDF ---
        StreamResource resource = new StreamResource("Factura_TuFood_" + id + ".pdf", () -> {
            return new ByteArrayInputStream(generarPDFBytes(id, metodo, direccion, productos, total));
        });

        Anchor downloadAnchor = new Anchor(resource, "");
        downloadAnchor.getElement().setAttribute("download", true);
        
        Button btnDownload = new Button("Descargar PDF", VaadinIcon.DOWNLOAD.create());
        btnDownload.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnDownload.setWidthFull();
        downloadAnchor.add(btnDownload);

        Button btnClose = new Button("Cerrar y continuar", e -> {
            invoiceDialog.close();
            irASeguimiento(metodo, direccion);
        });
        btnClose.setWidthFull();
        
        invoiceDialog.getFooter().add(downloadAnchor, btnClose);
        invoiceDialog.open();
    }

    private byte[] generarPDFBytes(String id, String metodo, String direccion, Map<Producto, Integer> productos, double total) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            com.lowagie.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            document.add(new com.lowagie.text.Paragraph("FACTURA TUFOOD S.A.", titleFont));
            document.add(new com.lowagie.text.Paragraph("CIF: B-12345678"));
            document.add(new com.lowagie.text.Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new com.lowagie.text.Paragraph("ID Pedido: #" + id));
            document.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));

            productos.forEach((p, cant) -> {
                document.add(new com.lowagie.text.Paragraph(cant + "x " + p.getNombre() + " .... " + String.format("%.2f€", p.getPrecio() * cant)));
            });

            document.add(new com.lowagie.text.Paragraph("------------------------------------------------------------------"));
            document.add(new com.lowagie.text.Paragraph("TOTAL: " + String.format("%.2f€", total), boldFont));
            document.add(new com.lowagie.text.Paragraph("Metodo de pago: " + metodo));
            document.add(new com.lowagie.text.Paragraph("Direccion de entrega: " + direccion));
            document.add(new com.lowagie.text.Paragraph("\n¡Gracias por confiar en nosotros!"));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private HorizontalLayout crearFilaTicket(String label, String valor) {
        HorizontalLayout hl = new HorizontalLayout(new Span(label), new Span(valor));
        hl.setWidthFull();
        hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return hl;
    }

    private void irASeguimiento(String metodo, String direccion) {
        Map<String, List<String>> params = Map.of(
            "metodo", List.of(metodo),
            "direccion", List.of(direccion)
        );
        UI.getCurrent().navigate(SeguimientoView.class, new QueryParameters(params));
    }

    // ... (Mantén tus métodos createMethodSelector, createDeliverySelector, createStorePanel, etc. igual que antes)

    private HorizontalLayout createMethodSelector() {
        Button cardBtn = new Button("Tarjeta", VaadinIcon.CREDIT_CARD.create());
        Button cashBtn = new Button("Efectivo", VaadinIcon.MONEY.create());
        cardBtn.setWidth("140px"); cashBtn.setWidth("140px");
        cardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cardBtn.addClickListener(e -> {
            cardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cashBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            creditCardForm.setVisible(true); cashInfo.setVisible(false);
            isCashSelected = false;
        });
        cashBtn.addClickListener(e -> {
            cashBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cardBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            creditCardForm.setVisible(false); cashInfo.setVisible(true);
            isCashSelected = true;
        });
        HorizontalLayout layout = new HorizontalLayout(cardBtn, cashBtn);
        layout.setWidthFull(); layout.setJustifyContentMode(JustifyContentMode.CENTER);
        return layout;
    }

    private HorizontalLayout createDeliverySelector() {
        Button deliveryBtn = new Button("A Domicilio", VaadinIcon.TRUCK.create());
        Button storeBtn = new Button("Tienda", VaadinIcon.SHOP.create());
        deliveryBtn.setWidth("140px"); storeBtn.setWidth("140px");
        deliveryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deliveryBtn.addClickListener(e -> {
            deliveryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            storeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addressLayout.setVisible(true); storeInfo.setVisible(false);
            isDeliverySelected = true;
        });
        storeBtn.addClickListener(e -> {
            storeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            deliveryBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addressLayout.setVisible(false); storeInfo.setVisible(true);
            isDeliverySelected = false;
        });
        HorizontalLayout layout = new HorizontalLayout(deliveryBtn, storeBtn);
        layout.setWidthFull(); layout.setJustifyContentMode(JustifyContentMode.CENTER);
        return layout;
    }

    private VerticalLayout createStorePanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM, LumoUtility.AlignItems.CENTER);
        layout.add(new Span("📍 Calle Principal 123 (TuFood Central)"), new Paragraph("Listo en 20 min."));
        return layout;
    }

    private VerticalLayout createCreditCardForm() {
        VerticalLayout layout = new VerticalLayout(); layout.setPadding(false);
        FormLayout form = new FormLayout();
        cardNumber = new TextField("Tarjeta"); cardholderName = new TextField("Titular");
        month = new Select<>(); month.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        year = new Select<>(); year.setItems(25, 26, 27, 28, 29, 30);
        csc = new PasswordField("CSC");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        form.add(cardNumber, 2); form.add(cardholderName, 2);
        form.add(new ExpirationDateField("Caducidad", month, year), 1); form.add(csc, 1);
        layout.add(form);
        return layout;
    }

    private VerticalLayout createCashPanel() {
        VerticalLayout layout = new VerticalLayout(); layout.setWidthFull();
        layout.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM, LumoUtility.AlignItems.CENTER);
        layout.add(new Span("Pago elegido: Efectivo"));
        return layout;
    }

    private FormLayout createAddressLayout() {
        FormLayout layout = new FormLayout();
        address = new TextField("Dirección"); city = new TextField("Ciudad"); zipCode = new TextField("C.P.");
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        layout.add(address, 2); layout.add(city, 1); layout.add(zipCode, 1);
        return layout;
    }

    private VerticalLayout createButtonLayout() {
        submit = new Button("Confirmar Pedido", VaadinIcon.CHECK.create());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        submit.setWidthFull();
        return new VerticalLayout(submit);
    }

    private void setupEvents() {
        submit.addClickListener(e -> {
            if (validateForm()) showLoadingAndProcess();
            else Notification.show("Revisa los datos obligatorios", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    private boolean validateForm() {
        boolean addrOk = !isDeliverySelected || (!address.isEmpty() && !city.isEmpty());
        boolean cardOk = isCashSelected || (!cardNumber.isEmpty() && !cardholderName.isEmpty() && month.getValue() != null);
        return addrOk && cardOk;
    }

    private static class ExpirationDateField extends CustomField<String> {
        public ExpirationDateField(String label, Select<Integer> m, Select<Integer> y) {
            setLabel(label); add(new HorizontalLayout(m, y));
        }
        @Override protected String generateModelValue() { return ""; }
        @Override protected void setPresentationValue(String s) {}
    }
}