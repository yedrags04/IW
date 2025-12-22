package com.example.application.views.pago;

import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Pago")
@Route(value = "credit-card-form", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.CREDIT_CARD)
public class PagoView extends Div {

    private final ShoppingCartService cartService;
    private TextField cardNumber, cardholderName, address, city, zipCode;
    private Select<Integer> month, year;
    private PasswordField csc;
    private Button cancel, submit;

    public PagoView(ShoppingCartService cartService) {
        this.cartService = cartService;
        
        // --- CONTENEDOR DE FONDO ---
        getElement().getStyle().set("margin-top", "var(--lumo-size-xl)");
        // Flex para centrar la tarjeta en la pantalla
        getStyle().set("display", "flex").set("justify-content", "center").set("padding", "20px");

        // --- TARJETA DE PAGO ---
        Div cardContainer = new Div();
        cardContainer.getStyle()
            .set("background-color", "white")
            .set("padding", "2.5rem")
            .set("border-radius", "24px")
            .set("box-shadow", "0 12px 30px rgba(0,0,0,0.12)")
            .set("max-width", "500px")
            .set("width", "100%");

        cardContainer.add(new H3("💳 Información de Pago"));
        cardContainer.add(createFormLayout());
        
        H3 deliveryTitle = new H3("📍 Dirección de Envío");
        deliveryTitle.addClassNames(LumoUtility.Margin.Top.XLARGE);
        cardContainer.add(deliveryTitle);
        
        cardContainer.add(createAddressLayout());
        cardContainer.add(createButtonLayout());

        add(cardContainer);
        setupEvents();
    }

    private Component createFormLayout() {
        cardNumber = new TextField("Número de tarjeta");
        cardNumber.setRequired(true);
        cardholderName = new TextField("Nombre del titular");
        cardholderName.setRequired(true);
        month = new Select<>();
        month.setPlaceholder("Mes");
        month.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        year = new Select<>();
        year.setPlaceholder("Año");
        year.setItems(25, 26, 27, 28, 29);
        csc = new PasswordField("CSC");
        csc.setRequired(true);

        FormLayout layout = new FormLayout();
        layout.add(cardNumber, 2);
        layout.add(cardholderName, 2);
        layout.add(new ExpirationDateField("Caducidad", month, year), 1);
        layout.add(csc, 1);
        return layout;
    }

    private Component createAddressLayout() {
        address = new TextField("Dirección");
        city = new TextField("Ciudad");
        zipCode = new TextField("C.P.");
        FormLayout layout = new FormLayout();
        layout.add(address, 2);
        layout.add(city, 1);
        layout.add(zipCode, 1);
        return layout;
    }

    private Component createButtonLayout() {
        submit = new Button("Confirmar Pago");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        submit.setWidthFull();

        cancel = new Button("Cancelar", e -> UI.getCurrent().navigate("carrito"));
        cancel.setWidthFull();

        // Corrección del error getMargin(): Usamos setMargin(true) en su lugar
        VerticalLayout layout = new VerticalLayout(submit, cancel);
        layout.setPadding(false);
        layout.setMargin(true); 
        layout.setSpacing(true);
        return layout;
    }

    private void setupEvents() {
        submit.addClickListener(e -> {
            if (validateFields()) {
                cartService.clearCart();
                Notification n = Notification.show("Pago procesado");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("seguimiento");
            }
        });
    }

    private boolean validateFields() {
        return !cardNumber.isEmpty() && !cardholderName.isEmpty() && month.getValue() != null && !address.isEmpty();
    }

    private class ExpirationDateField extends CustomField<String> {
        public ExpirationDateField(String label, Select<Integer> month, Select<Integer> year) {
            setLabel(label);
            add(new HorizontalLayout(month, year));
        }
        @Override protected String generateModelValue() { return ""; }
        @Override protected void setPresentationValue(String s) {}
    }
}