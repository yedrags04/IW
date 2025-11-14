package com.example.application.views.productos;

import com.example.application.model.Producto;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

public class ProductosViewCard extends ListItem {

    // Constructor modificado para aceptar un objeto Producto
    public ProductosViewCard(Producto producto) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        Image image = new Image();
        image.setWidth("100%");
        // Como no tienes URL en la BD, usamos una genérica de comida
        image.setSrc("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80");
        image.setAlt(producto.getNombre());

        div.add(image);

        // --- DATOS DINÁMICOS ---
        Span header = new Span();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(producto.getNombre()); // <-- DATO REAL

        Span subtitle = new Span();
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        subtitle.setText(String.format("%.2f €", producto.getPrecio())); // <-- DATO REAL

        Paragraph description = new Paragraph(
                "Aquí puedes poner una descripción más larga si la tuvieras en la BD.");
        description.addClassName(Margin.Vertical.MEDIUM);

        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText(producto.getCategoria()); // <-- DATO REAL

        add(div, header, subtitle, description, badge);
    }
}