package com.ratella.store.controller;

import com.ratella.store.model.cart.Cart;
import com.ratella.store.model.cart.CartService;
import com.ratella.store.model.order.LineItem;
import com.ratella.store.model.order.Order;
import com.ratella.store.model.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private OrderRepository orderRepository;
    private final CartService cartService;

    @Autowired
    public OrderController(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @RequestMapping(value = "/ebooks/order/create", method = RequestMethod.POST)
    public Mono<String> createOrder(Order order, WebSession session) {
        order.setId(UUID
                .randomUUID()
                .toString());
        return orderRepository
                .createOrder(order)
                .flatMap(responseStatusCode -> cartService.deleteCart(session.getId(), session.getId()))
                .thenReturn("orderconfirmation");

    }

    @RequestMapping(value = "/ebooks/order/checkout", method = RequestMethod.POST)
    public String checkOut(@ModelAttribute Cart cart, Model model, WebSession session, Principal principal) {
        model.addAttribute("order", getOrder(cart, principal.getName()));
        model.addAttribute("cartItemCount", cartService.getNumberOfItemsInTheCart(session.getId()));
        return "checkout";
    }

/*    @RequestMapping(value = "/ebooks/order/customer/{customerId}", method = RequestMethod.GET)
    public Mono<String> getCustomerOrders(@PathVariable String customerId, Model model, WebSession session) {
        return orderRepository
                .getOrders(customerId)
                .map(order -> {
                    logger.info(order.toString());
                    return model.addAttribute("orders", order);
                }).then(Mono.just("orders"));

    }*/

    @RequestMapping(value = "/ebooks/order/customer/{customerId}", method = RequestMethod.GET)
    public String getCustomerOrders(@PathVariable String customerId, Model model, WebSession session) {
        model.addAttribute("orders", orderRepository.getOrders(customerId));
        return "orders";

    }

    private Order getOrder(Cart cart, String customerId) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus("RECEIVED");
        order.setSubTotal(cart.getSubTotal());
        List<LineItem> lineItems = new ArrayList<>();
        order.setLineItems(lineItems);
        cart
                .getItems()
                .forEach(cartItem -> {
                    LineItem lineItem = new LineItem();
                    lineItem.setBookId(cartItem.getBookId());
                    lineItem.setTitle(cartItem.getTitle());
                    lineItem.setDescription(cartItem.getDescription());
                    lineItem.setAuthor(cartItem.getAuthor());
                    lineItem.setImage(cartItem.getImage());
                    lineItem.setQuantity(cartItem.getQuantity());
                    lineItem.setPrice(cartItem.getPrice());
                    lineItems.add(lineItem);
                });
        return order;
    }

}
