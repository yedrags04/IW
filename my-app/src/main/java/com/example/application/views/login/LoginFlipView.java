package com.example.application.views.login;

import com.example.application.views.EmptyLayout;
import com.example.application.security.AuthService; 
import com.vaadin.flow.component.UI; 
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/* * VISTA DE AUTENTICACIÓN DINÁMICA
 * Implementa un diseño de "tarjeta giratoria" que contiene el Login y el Registro.
 * Utiliza CSS personalizado para la animación 3D.
 */
@PageTitle("Acceso | TuFood")
@Route(value = "login", layout = EmptyLayout.class) 
@AnonymousAllowed // Permite que usuarios no logueados vean esta página
@CssImport("./styles/style.css") 
@StyleSheet("https://fonts.googleapis.com/css?family=Montserrat:400,700") 
public class LoginFlipView extends VerticalLayout {

    private final AuthService authService; 
    private Div container; // Contenedor que rota mediante CSS

    // Clase interna (DTO) para mapear los datos del formulario de registro
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

    // Definiciones SVG para iconos personalizados sin dependencias externas
    private static final String SVG_USER_PLUS = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"96\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2\"/><circle cx=\"8.5\" cy=\"7\" r=\"4\"/><line x1=\"20\" y1=\"8\" x2=\"20\" y2=\"14\"/><line x1=\"23\" y1=\"11\" x2=\"17\" y2=\"11\"/></svg>";
    private static final String SVG_ARROW_RIGHT = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><polyline points=\"12 16 16 12 12 8\"/><line x1=\"8\" y1=\"12\" x2=\"16\" y2=\"12\"/></svg>";
    private static final String SVG_LOG_IN = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"96\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4\"/><polyline points=\"10 17 15 12 10 7\"/><line x1=\"15\" y1=\"12\" x2=\"3\" y2=\"12\"/></svg>";
    private static final String SVG_ARROW_LEFT = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><polyline points=\"12 8 8 12 12 16\"/><line x1=\"16\" y1=\"12\" x2=\"8\" y2=\"12\"/></svg>";
    private static final String SVG_SOCIAL_FB = "<a href=\"#\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z\"/></svg></a>";
    private static final String SVG_SOCIAL_TWITTER = "<a href=\"#\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z\"></path></svg></a>";
    private static final String SVG_SOCIAL_GITHUB = "<a href=\"#\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22\"></path></svg></a>";
    private static final String SVG_SOCIAL_LINKEDIN = "<a href=\"#\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z\"></path><rect x=\"2\" y=\"9\" width=\"4\" height=\"12\"></rect><circle cx=\"4\" cy=\"4\" r=\"2\"></circle></svg></a>";

    public LoginFlipView(AuthService authService) {
        this.authService = authService; 
        addClassName("login-page-layout"); 
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        container = new Div(); 
        container.setId("container"); 

        // Botón para alternar al panel de registro
        Button registerButton = new Button("Registrarse", new Html(SVG_ARROW_RIGHT));
        registerButton.setId("register"); 
        registerButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        // Botón para alternar al panel de login
        Button flipLoginButton = new Button("Iniciar sesión", new Html(SVG_ARROW_LEFT));
        flipLoginButton.setId("login"); 
        flipLoginButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Disparadores de la animación CSS (añaden/quitan la clase 'active')
        registerButton.addClickListener(event -> container.addClassName("active"));
        flipLoginButton.addClickListener(event -> container.removeClassName("active")); 
        
        container.add(
            createLoginPanel(),
            createRegisterPanel(),
            createPageFront(registerButton),
            createPageBack(flipLoginButton)
        );
        
        add(container);
        // Perspectiva para el efecto 3D
        getElement().getStyle().set("perspective", "1500px");
    }
    
    // Crea el formulario de inicio de sesión
    private Div createLoginPanel() {
        Div loginPanel = new Div();
        loginPanel.addClassName("login");

        H1 title = new H1("¡Bienvenido!");
        title.getStyle().set("margin-bottom", "20px");
        
        TextField username = new TextField("Usuario"); 
        username.setWidthFull(); 
        PasswordField password = new PasswordField("Contraseña");
        password.setWidthFull(); 

        Div errorLabel = new Div("Credenciales incorrectas.");
        errorLabel.getStyle().set("color", "red").set("margin", "10px 0");
        errorLabel.setVisible(false); 

        Button loginBtn = new Button("Entrar");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginBtn.setWidthFull();
        
        loginBtn.addClickListener(e -> {
            // Lógica de autenticación mediante el AuthService
            if (authService.authenticate(username.getValue(), password.getValue())) {
                UI.getCurrent().navigate(""); // Redirige a la raíz (HomeView)
            } else {
                errorLabel.setVisible(true);
            }
        });

        Html socialIcons = new Html("<div class='social-icons'>" + SVG_SOCIAL_FB + SVG_SOCIAL_TWITTER + SVG_SOCIAL_GITHUB + SVG_SOCIAL_LINKEDIN + "</div>");
        loginPanel.add(title, username, password, errorLabel, loginBtn, socialIcons);
        return loginPanel;
    }

    // Crea el panel de información frontal (Giro)
    private Div createPageFront(Button btn) {
        Div pageFront = new Div();
        pageFront.addClassNames("page", "front");
        Div content = new Div();
        content.addClassName("content");
        content.add(new Html(SVG_USER_PLUS), new H1("¿Eres nuevo?"), new Paragraph("Regístrate y empieza hoy."), btn);
        pageFront.add(content);
        return pageFront;
    }

    // Crea el panel de información trasero (Giro)
    private Div createPageBack(Button btn) {
        Div pageBack = new Div();
        pageBack.addClassNames("page", "back");
        Div content = new Div();
        content.addClassName("content");
        content.add(new Html(SVG_LOG_IN), new H1("¡Bienvenido de nuevo!"), new Paragraph("Por favor, inicia sesión con tu cuenta."), btn);
        pageBack.add(content);
        return pageBack;
    }
    
    // Crea el formulario de registro de nuevos usuarios
    private Div createRegisterPanel() {
        Div registerPanel = new Div();
        registerPanel.addClassName("register");
        
        VerticalLayout content = new VerticalLayout();
        content.getStyle().set("background", "transparent");
        content.setHeightFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER); 
        content.setAlignItems(Alignment.CENTER);

        H1 title = new H1("Sign Up");

        TextField name = new TextField("Nombre");
        name.setPrefixComponent(VaadinIcon.USER.create());
        name.setWidthFull();
        TextField email = new TextField("Email");
        email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        email.setWidthFull();
        PasswordField password = new PasswordField("Contraseña");
        password.setPrefixComponent(VaadinIcon.LOCK.create());
        password.setWidthFull();

        Div feedbackLabel = new Div();
        feedbackLabel.setVisible(false);

        Button signUpBtn = new Button("Crear Cuenta");
        signUpBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        signUpBtn.setWidthFull();

        // El Binder sincroniza el formulario con el objeto Java de forma automática
        Binder<RegistrationForm> binder = new Binder<>(RegistrationForm.class);

        signUpBtn.addClickListener(event -> {
            RegistrationForm formBean = new RegistrationForm();
            try {
                binder.writeBean(formBean);
                String result = authService.register(name.getValue(), email.getValue(), password.getValue());
                if ("SUCCESS".equals(result)) {
                    Notification.show("¡Éxito! Inicie sesión.");
                    container.removeClassName("active"); // Vuelve al panel frontal
                    binder.readBean(new RegistrationForm()); 
                } else {
                    feedbackLabel.setText(result);
                    feedbackLabel.getStyle().set("color", "red");
                    feedbackLabel.setVisible(true);
                }
            } catch (ValidationException e) {
                feedbackLabel.setText("Revisa los errores.");
                feedbackLabel.setVisible(true);
            }
        });

        Html socialIcons = new Html("<div class='social-icons'>" + SVG_SOCIAL_FB + SVG_SOCIAL_TWITTER + SVG_SOCIAL_GITHUB + SVG_SOCIAL_LINKEDIN + "</div>");
        content.add(title, name, email, password, signUpBtn, feedbackLabel, socialIcons);
        registerPanel.add(content);
        return registerPanel;
    }
}