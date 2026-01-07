package com.example.application.services;

import com.example.application.model.Producto;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/* * SERVICIO DE CARRITO DE COMPRA
 * Gestiona los productos que el cliente selecciona en la web.
 * A diferencia de otros servicios, el estado se guarda en la VaadinSession 
 * para que cada cliente tenga su propio carrito independiente.
 */
@Service
public class ShoppingCartService implements Serializable {

    private static final String CART_SESSION_KEY = "shoppingCart";

    // Recupera el carrito de la sesión actual o crea uno nuevo si no existe
    @SuppressWarnings("unchecked")
    private Map<Producto, Integer> getCart() {
        Map<Producto, Integer> cart = (Map<Producto, Integer>) VaadinSession.getCurrent().getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>(); // Usamos LinkedHashMap para mantener el orden de inserción
            VaadinSession.getCurrent().setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    // Añade un producto o incrementa la cantidad si ya existía
    public void addProduct(Producto producto) {
        Map<Producto, Integer> cart = getCart();
        cart.merge(producto, 1, Integer::sum);
    }

    // Reduce la cantidad de un plato o lo quita del carrito si queda solo uno
    public void removeProduct(Producto producto) {
        Map<Producto, Integer> cart = getCart();
        if (cart.containsKey(producto)) {
            int cantidad = cart.get(producto);
            if (cantidad > 1) {
                cart.put(producto, cantidad - 1);
            } else {
                cart.remove(producto);
            }
        }
    }

    // Devuelve el contenido del carrito (Solo lectura para evitar modificaciones externas)
    public Map<Producto, Integer> getCartContents() {
        return Collections.unmodifiableMap(getCart());
    }

    // Calcula el importe total de la compra multiplicando precio por cantidad
    public double getTotalPrice() {
        return getCartContents().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrecio() * entry.getValue())
                .sum();
    }

    // Retorna la cantidad total de artículos en el carrito
    public int getItemCount() {
        return getCartContents().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
    
    // Resetea el carrito de la sesión (se usa tras finalizar un pago con éxito)
    public void clearCart() {
        VaadinSession.getCurrent().setAttribute(CART_SESSION_KEY, new LinkedHashMap<Producto, Integer>());
    }
}