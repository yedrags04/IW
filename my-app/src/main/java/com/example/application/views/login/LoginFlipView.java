package com.example.application.views.login;

import com.example.application.views.EmptyLayout;
import com.example.application.security.AuthService; 
import com.vaadin.flow.component.UI; 
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

@Route(value = "", layout = EmptyLayout.class) 
@CssImport("./styles/style.css") 
@StyleSheet("https://fonts.googleapis.com/css?family=Montserrat:400,700") 

public class LoginFlipView extends VerticalLayout {

    private final AuthService authService; 

    public class RegistrationForm {
        private String name;
        private String email;
        private String password;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    private static final String SVG_USER_PLUS = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"96\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-user-plus\"><path d=\"M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2\"/><circle cx=\"8.5\" cy=\"7\" r=\"4\"/><line x1=\"20\" y1=\"8\" x2=\"20\" y2=\"14\"/><line x1=\"23\" y1=\"11\" x2=\"17\" y2=\"11\"/></svg>";
    private static final String SVG_ARROW_RIGHT = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-arrow-right-circle\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><polyline points=\"12 16 16 12 12 8\"/><line x1=\"8\" y1=\"12\" x2=\"16\" y2=\"12\"/></svg>";
    private static final String SVG_LOG_IN = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"96\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-log-in\"><path d=\"M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4\"/><polyline points=\"10 17 15 12 10 7\"/><line x1=\"15\" y1=\"12\" x2=\"3\" y2=\"12\"/></svg>";
    private static final String SVG_ARROW_LEFT = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-arrow-left-circle\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><polyline points=\"12 8 8 12 12 16\"/><line x1=\"16\" y1=\"12\" x2=\"8\" y2=\"12\"/></svg>";
    private static final String SVG_SOCIAL_FB = "<a href=\"#\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-facebook\"><path d=\"M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z\"/></svg></a>";
    private static final String SVG_SOCIAL_TWITTER = "<a href=\"#\"><svg class=\"feather feather-twitter\" xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z\"></path></svg></a>";
    private static final String SVG_SOCIAL_GITHUB = "<a href=\"#\"><svg class=\"feather feather-github\" xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22\"></path></svg></a>";
    private static final String SVG_SOCIAL_LINKEDIN = "<a href=\"#\"><svg class=\"feather feather-linkedin\" xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z\"></path><rect x=\"2\" y=\"9\" width=\"4\" height=\"12\"></rect><circle cx=\"4\" cy=\"4\" r=\"2\"></circle></svg></a>";
    private static final String SVG_REG_FB = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-facebook\"><path d=\"M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z\"/></svg>";

    public LoginFlipView(AuthService authService) {
        this.authService = authService; 
        addClassName("login-page-layout"); 
        
        Div container = new Div();
        container.setId("container"); 

        Button registerButton = new Button("Register", new Html(SVG_ARROW_RIGHT));
        registerButton.setId("register"); 
        
        Button flipLoginButton = new Button("Log In", new Html(SVG_ARROW_LEFT));
        flipLoginButton.setId("login"); 

        registerButton.addClickListener(event -> container.setClassName("active"));
        flipLoginButton.addClickListener(event -> container.setClassName("close"));
        
        container.add(
            createLoginPanel(),
            createPageFront(registerButton),
            createPageBack(flipLoginButton),
            createRegisterPanel()
        );
        
        Anchor versionLink = new Anchor("https://codepen.io/eminqasimov/full/KYrVBr", "Rolling Version ");
        versionLink.setClassName("version");
        versionLink.setTarget("_blank");
        
        add(container, versionLink);

        setHeightFull(); 
        setSizeFull(); 
        setAlignItems(Alignment.CENTER); 
        setJustifyContentMode(JustifyContentMode.CENTER); 
        getElement().getStyle().set("perspective", "1500px");
    }
    
    private Div createLoginPanel() {
        Div loginPanel = new Div();
        loginPanel.addClassName("login");

        Div content = new Div();
        content.addClassName("content");

        H1 title = new H1("Log In");
        Div form = new Div(); 
        
        TextField username = new TextField(); 
        username.setPlaceholder("username"); 
        username.setWidthFull(); 
        
        PasswordField password = new PasswordField();
        password.setPlaceholder("password");
        password.setWidthFull(); 
        
        Div errorLabel = new Div("Credenciales incorrectas.");
        errorLabel.getStyle().set("color", "red");
        errorLabel.setVisible(false); 

        Span remember = new Span("Remember me");
        remember.addClassName("remember");
        Span forget = new Span("Forgot password?");
        forget.addClassName("forget");
        
        HorizontalLayout optionsLayout = new HorizontalLayout(remember, forget);
        optionsLayout.setWidthFull();
        optionsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN); 
        
        Span clearfix = new Span();
        clearfix.addClassName("clearfix");
        
        Button loginButton = new Button("Log In");
        
        // --- LÓGICA DE REDIRECCIÓN ACTUALIZADA ---
        loginButton.addClickListener(event -> {
            boolean authenticated = authService.authenticate(
                username.getValue(), 
                password.getValue()
            );

            if (authenticated) {
                errorLabel.setVisible(false);
                // Ahora redirige directamente a la galería de productos
                UI.getCurrent().navigate("image-gallery"); 
            } else {
                errorLabel.setVisible(true);
            }
        });
        
        form.add(username, password, errorLabel, optionsLayout, clearfix, loginButton); 

        Span loginWith = new Span("Or Connect with");
        loginWith.addClassName("loginwith");

Html socialIcons = new Html("<div class='social-icons'>" + SVG_SOCIAL_FB + SVG_SOCIAL_TWITTER + SVG_SOCIAL_GITHUB + SVG_SOCIAL_LINKEDIN + "</div>");        Span copy = new Span("© 2025"); 
        copy.addClassName("copy");

        content.add(title, form, loginWith, socialIcons, copy);
        loginPanel.add(content);
        return loginPanel;
    }
    
    private Div createPageFront(Button registerButton) {
        Div pageFront = new Div();
        pageFront.addClassNames("page", "front");
        Div content = new Div();
        content.addClassName("content");
        Html icon = new Html(SVG_USER_PLUS);
        H1 title = new H1("Hello, friend!");
        Paragraph p = new Paragraph("Enter your personal details and start journey with us");
        content.add(icon, title, p, registerButton);
        pageFront.add(content);
        return pageFront;
    }

    private Div createPageBack(Button loginButton) {
        Div pageBack = new Div();
        pageBack.addClassNames("page", "back");
        Div content = new Div();
        content.addClassName("content");
        Html icon = new Html(SVG_LOG_IN);
        H1 title = new H1("Welcome Back!");
        Paragraph p = new Paragraph("To keep connected with us please login with your personal info");
        content.add(icon, title, p, loginButton);
        pageBack.add(content);
        return pageBack;
    }
    
    private Div createRegisterPanel() {
        Div registerPanel = new Div();
        registerPanel.addClassName("register");
        Div content = new Div();
        content.addClassName("content");
        H1 title = new H1("Sign Up");
        String socialHtmlContent = "<div>" + SVG_REG_FB + SVG_SOCIAL_TWITTER + SVG_SOCIAL_GITHUB + SVG_SOCIAL_LINKEDIN + "</div>";
        Html socialIcons = new Html(socialHtmlContent);        
        Span loginWith = new Span("Or");
        loginWith.addClassName("loginwith");
        
        Binder<RegistrationForm> binder = new Binder<>(RegistrationForm.class);
        Div form = new Div();
        TextField name = new TextField();
        name.setPlaceholder("nombre de usuario");
        name.setWidthFull(); 
        TextField email = new TextField(); 
        email.setPlaceholder("email");
        email.setWidthFull();
        PasswordField password = new PasswordField();
        password.setPlaceholder("contraseña");
        password.setWidthFull();
        Div feedbackLabel = new Div("");
        feedbackLabel.getStyle().set("color", "red");
        feedbackLabel.setVisible(false);

        binder.forField(name)
            .withValidator(value -> value != null && value.length() >= 3, "Mínimo 3 caracteres.")
            .asRequired("Obligatorio.")
            .bind(RegistrationForm::getName, RegistrationForm::setName);

        binder.forField(email)
            .withValidator(value -> value != null && value.matches(".+@.+\\..+"), "Email inválido.")
            .asRequired("Obligatorio.")
            .bind(RegistrationForm::getEmail, RegistrationForm::setEmail);

        binder.forField(password)
            .withValidator(value -> value != null && value.length() >= 6, "Mínimo 6 caracteres.")
            .asRequired("Obligatorio.")
            .bind(RegistrationForm::getPassword, RegistrationForm::setPassword);
        
        Span terms = new Span("I accept terms");
        terms.addClassName("remember");
        Span clearfix = new Span();
        clearfix.addClassName("clearfix");
        Button registerButton = new Button("Register");
        
        registerButton.addClickListener(event -> {
            RegistrationForm formBean = new RegistrationForm();
            feedbackLabel.setVisible(false); 
            try {
                binder.writeBean(formBean); 
                String result = authService.register(formBean.getName(), formBean.getEmail(), formBean.getPassword());
                if ("SUCCESS".equals(result)) {
                    feedbackLabel.setText("¡Éxito! Inicie sesión.");
                    feedbackLabel.getStyle().set("color", "green");
                    feedbackLabel.setVisible(true);
                    binder.readBean(new RegistrationForm()); 
                } else {
                    feedbackLabel.setText(result);
                    feedbackLabel.setVisible(true);
                }
            } catch (ValidationException e) {
                feedbackLabel.setText("Revise los campos.");
                feedbackLabel.setVisible(true);
            }
        });
        
        form.add(name, email, password, feedbackLabel, terms, clearfix, registerButton);
        content.add(title, socialIcons, loginWith, form);
        registerPanel.add(content);
        return registerPanel;
    }
}