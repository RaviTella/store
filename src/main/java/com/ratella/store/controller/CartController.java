package com.ratella.store.controller;

import com.ratella.store.model.cart.CartItem;
import com.ratella.store.model.cart.CartRepository;
import com.ratella.store.model.cart.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Controller
public class CartController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private  final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @RequestMapping(value = "/ebooks/cart/item/add", method = RequestMethod.POST)
    public Mono<String> addItem(CartItem item, WebSession session) {
        logger.info("Session ID in /cart/add" + this.getClass() + ": " + session.getId());
        item.setQuantity(1);
        logger.info("adding item to cart");
        return cartService
                .addItemToCart(session.getId(), item)
                .thenReturn("redirect:/ebooks/index");

    }

    @RequestMapping(value = "/ebooks/cart", method = RequestMethod.GET)
    public Mono<String> getCart(Model model, WebSession session, Principal principal) {
        logger.info("Session ID in /cart" + this.getClass() + ": " + session.getId());
        return cartService
                .getCart(session.getId())
                .map(cart -> {
                    model.addAttribute("customerId", principal.getName());
                    model.addAttribute("cart", cart);
                    model.addAttribute("cartItemCount", cart
                            .getItems()
                            .size());
                    return "cart";
                });
    }

    @RequestMapping(value = "/ebooks/cart/delete/item/{id}", method = RequestMethod.GET)
    public Mono<String> deleteItem(@PathVariable String id, WebSession session) {
        logger.info("deleting cart item with id" + id);
        return cartService
                .removeItemFromCart(session.getId(), id)
                .thenReturn("redirect:/ebooks/cart");
    }

}
