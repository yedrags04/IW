package com.example.application.services;


import com.example.application.model.Producto;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio de carrito de compras que mantiene el estado en la sesión de Vaadin.
 */
@Service
public class ShoppingCartService implements Serializable {

    private static final String CART_SESSION_KEY = "shoppingCart";

    /**
     * Obtiene el mapa del carrito de la sesión. Si no existe, crea uno nuevo.
     * El mapa almacena Producto y su cantidad (Integer).
     */
    @SuppressWarnings("unchecked")
    private Map<Producto, Integer> getCart() {
        Map<Producto, Integer> cart = (Map<Producto, Integer>) VaadinSession.getCurrent().getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>(); // Usamos LinkedHashMap para mantener el orden de inserción
            VaadinSession.getCurrent().setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    /**
     * Añade un producto al carrito o incrementa su cantidad.
     * @param producto Producto a añadir.
     */
    public void addProduct(Producto producto) {
        Map<Producto, Integer> cart = getCart();
        cart.merge(producto, 1, Integer::sum);
    }

    /**
     * Devuelve una vista inmutable del contenido del carrito.
     */
    public Map<Producto, Integer> getCartContents() {
        return Collections.unmodifiableMap(getCart());
    }

    /**
     * Calcula el precio total de todos los artículos en el carrito.
     */
    public double getTotalPrice() {
        return getCartContents().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrecio() * entry.getValue())
                .sum();
    }

    /**
     * Obtiene el número total de artículos (unidades) en el carrito.
     */
    public int getItemCount() {
        return getCartContents().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
    
    /**
     * Vacía completamente el carrito.
     */
    public void clearCart() {
        VaadinSession.getCurrent().setAttribute(CART_SESSION_KEY, new LinkedHashMap<Producto, Integer>());
    }
}