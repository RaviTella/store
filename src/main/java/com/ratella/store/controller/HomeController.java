package com.ratella.store.controller;

import com.azure.cosmos.implementation.NotFoundException;
import com.ratella.store.model.book.Book;
import com.ratella.store.model.book.BookRepository;
import com.ratella.store.model.cart.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BookRepository bookRepository;
    private CartRepository cartRepository;

    @Autowired
    public HomeController(BookRepository bookRepository,CartRepository cartRepository) {
        this.bookRepository = bookRepository;
        this.cartRepository = cartRepository;
    }

    @GetMapping(value = "/home")
    public String home(Model model, WebSession session ){
        model.addAttribute("books", bookRepository.getBooks());
        model.addAttribute("cartItemCount" ,cartRepository.getCartItemCount(session.getId()));
        //get user info
        return "index";
    }
}
