package com.example.application.views.pago;

import com.example.application.model.Pedido;
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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@PageTitle("Pago | TuFood")
@Route(value = "pago", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.CREDIT_CARD_SOLID)
public class PagoView extends VerticalLayout {

    private final ShoppingCartService cartService;
    private final OrderService orderService; // Inyección del servicio de pedidos

    private TextField cardNumber, cardholderName, address, city, zipCode;
    private Select<Integer> month, year;
    private PasswordField csc;
    private Button submit;
    
    private VerticalLayout creditCardForm, cashInfo;
    private FormLayout addressLayout;
    private VerticalLayout storeInfo;
    
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

        // --- SECCIÓN PAGO ---
        H3 paymentTitle = new H3();
        paymentTitle.add(VaadinIcon.WALLET.create(), new Span(" Método de Pago"));
        
        HorizontalLayout methodSelector = createMethodSelector();
        creditCardForm = createCreditCardForm();
        cashInfo = createCashPanel();
        cashInfo.setVisible(false);

        // --- SECCIÓN ENTREGA ---
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

    private void showLoadingAndProcess() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false); dialog.setCloseOnOutsideClick(false);
        ProgressBar pb = new ProgressBar(); pb.setIndeterminate(true);
        VerticalLayout vl = new VerticalLayout(pb, new Span("Procesando pedido..."));
        vl.setAlignItems(Alignment.CENTER); dialog.add(vl); dialog.open();

        // 1. Capturar datos actuales
        String metodoFinal = isCashSelected ? "Efectivo" : "Tarjeta";
        String direccionFinal = isDeliverySelected ? (address.getValue() + ", " + city.getValue()) : "Recogida en Tienda";
        String idGenerado = "#" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        UI ui = UI.getCurrent();
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                ui.access(() -> {
                    // 2. REGISTRAR PEDIDO REAL EN EL SISTEMA
                    Pedido nuevoPedido = new Pedido(idGenerado, "Cliente Web", isDeliverySelected ? "DOMICILIO" : "TIENDA", direccionFinal);
                    orderService.registrarPedido(nuevoPedido);

                    dialog.close();
                    cartService.clearCart();
                    
                    Map<String, List<String>> params = Map.of(
                        "metodo", List.of(metodoFinal),
                        "direccion", List.of(direccionFinal)
                    );
                    ui.navigate(SeguimientoView.class, new QueryParameters(params));
                });
            } catch (Exception ex) { ui.access(dialog::close); }
        }).start();
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