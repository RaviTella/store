package com.ratella.store.controller;

import com.ratella.store.model.cart.Cart;
import com.ratella.store.model.cart.CartRepository;
import com.ratella.store.model.order.LineItem;
import com.ratella.store.model.order.Order;
import com.ratella.store.model.order.OrderCosmosDB;
import com.ratella.store.model.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private OrderRepository orderRepository;
    private CartRepository cartRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @RequestMapping(value = "/ebooks/order/create", method = RequestMethod.POST)
    public Mono<String> createOrder(Order order, WebSession session) {
        order.setId(UUID
                .randomUUID()
                .toString());
        return orderRepository
                .createOrder(order)
                .flatMap(responseStatusCode -> cartRepository.deleteCart(session.getId(), session.getId()))
                .map(responseStatusCode -> {
                    return "orderconfirmation";
                });
    }

    @RequestMapping(value = "/ebooks/order/checkout", method = RequestMethod.POST)
    public String checkOut(@ModelAttribute Cart cart, Model model) {
        model.addAttribute("order", getOrder(cart));
        return "checkout";
    }

    @RequestMapping(value = "/ebooks/orders/user", method = RequestMethod.GET)
    public String getOrders(){
        return "orders";

    }

    private Order getOrder(Cart cart) {
        Order order = new Order();
        order.setCustomerId("customer1");
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
