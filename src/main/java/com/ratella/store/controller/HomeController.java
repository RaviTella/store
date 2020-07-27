package com.ratella.store.controller;

import com.ratella.store.model.book.BookRepository;
import com.ratella.store.model.cart.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.WebSession;

import java.security.Principal;

@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BookRepository bookRepository;
    private  final CartService cartService;

    @Autowired
    public HomeController(BookRepository bookRepository, CartService cartService) {
        this.bookRepository = bookRepository;
        this.cartService= cartService;
    }

    @RequestMapping(value = "/ebooks/index", method = RequestMethod.GET)
    public String home(Model model, WebSession session, Principal principal) {
        model.addAttribute("books", bookRepository.getBooks());
        model.addAttribute("cartItemCount", cartService.getNumberOfItemsInTheCart(session.getId()));
        model.addAttribute("customerId", principal.getName());
        logger.info(principal.getName());
        return "index";
    }

    @RequestMapping(value = "/ebooks/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
    }
}
