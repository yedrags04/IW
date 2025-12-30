package com.example.application.views.pago;

import com.example.application.services.ShoppingCartService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
@Route(value = "pago", layout = MainLayout.class) // Asegúrate de que esto sea "pago"
@Menu(order = 1, icon = LineAwesomeIconUrl.CREDIT_CARD_SOLID)
public class PagoView extends VerticalLayout {

    private final ShoppingCartService cartService;
    private TextField cardNumber, cardholderName, address, city, zipCode;
    private Select<Integer> month, year;
    private PasswordField csc;
    private Button cancel, submit;

    public PagoView(ShoppingCartService cartService) {
        this.cartService = cartService;
        
        addClassName("pago-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout cardContainer = new VerticalLayout();
        cardContainer.addClassName("pago-card");
        cardContainer.setPadding(false);
        cardContainer.setSpacing(true);

        H3 paymentTitle = new H3();
        paymentTitle.add(VaadinIcon.CREDIT_CARD.create(), new Span(" Información de Pago"));
        
        H3 deliveryTitle = new H3();
        deliveryTitle.add(VaadinIcon.MAP_MARKER.create(), new Span(" Dirección de Envío"));
        deliveryTitle.addClassNames(LumoUtility.Margin.Top.LARGE);

        cardContainer.add(paymentTitle, createFormLayout(), deliveryTitle, createAddressLayout(), createButtonLayout());

        add(cardContainer);
        setupEvents();
    }

    private Component createFormLayout() {
        FormLayout layout = new FormLayout();
        
        cardNumber = new TextField("Número de tarjeta");
        cardNumber.setPrefixComponent(VaadinIcon.CREDIT_CARD.create());
        cardNumber.getElement().setAttribute("theme", "large");

        cardholderName = new TextField("Nombre del titular");
        cardholderName.setPrefixComponent(VaadinIcon.USER.create());
        cardholderName.getElement().setAttribute("theme", "large");

        month = new Select<>();
        month.setPlaceholder("Mes");
        month.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        year = new Select<>();
        year.setPlaceholder("Año");
        year.setItems(25, 26, 27, 28, 29, 30);

        csc = new PasswordField("CSC");
        csc.setPrefixComponent(VaadinIcon.LOCK.create());
        csc.getElement().setAttribute("theme", "large");

        // Responsivo: 1 columna en móvil, 2 en escritorio
        layout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        layout.add(cardNumber, 2);
        layout.add(cardholderName, 2);
        // IMPORTANTE: El CustomField ahora tiene ancho completo
        layout.add(new ExpirationDateField("Caducidad", month, year), 1);
        layout.add(csc, 1);
        
        return layout;
    }

    private Component createAddressLayout() {
        FormLayout layout = new FormLayout();
        address = new TextField("Dirección");
        address.getElement().setAttribute("theme", "large");
        city = new TextField("Ciudad");
        zipCode = new TextField("C.P.");

        layout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        layout.add(address, 2);
        layout.add(city, 1);
        layout.add(zipCode, 1);
        return layout;
    }

    private Component createButtonLayout() {
        submit = new Button("Confirmar Pago", VaadinIcon.CHECK.create());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        submit.setWidthFull();
        submit.setHeight("3.5rem");

        cancel = new Button("Cancelar", e -> UI.getCurrent().navigate("carrito"));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        return new VerticalLayout(submit, cancel);
    }

    private void setupEvents() {
        submit.addClickListener(e -> {
            if (validateFields()) {
                cartService.clearCart();
                Notification.show("¡Pago procesado!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("tufood");
            }
        });
    }

    private boolean validateFields() {
        return !cardNumber.isEmpty() && !cardholderName.isEmpty() && month.getValue() != null;
    }

    // CLASE CORREGIDA PARA EVITAR QUE SE PISEN LOS ELEMENTOS
    private class ExpirationDateField extends CustomField<String> {
        public ExpirationDateField(String label, Select<Integer> month, Select<Integer> year) {
            setLabel(label);
            setWidthFull(); // Vital para que FormLayout lo calcule bien
            
            month.setWidthFull();
            year.setWidthFull();
            
            HorizontalLayout layout = new HorizontalLayout(month, year);
            layout.setWidthFull();
            layout.setSpacing(true);
            add(layout);
        }
        @Override protected String generateModelValue() { return ""; }
        @Override protected void setPresentationValue(String s) {}
    }
}