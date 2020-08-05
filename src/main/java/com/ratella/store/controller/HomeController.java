package com.ratella.store.controller;

import com.ratella.store.model.book.Book;
import com.ratella.store.model.book.BookRepository;
import com.ratella.store.model.book.Response;
import com.ratella.store.model.cart.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BookRepository bookRepository;
    private final CartService cartService;

    @Autowired
    public HomeController(BookRepository bookRepository, CartService cartService) {
        this.bookRepository = bookRepository;
        this.cartService = cartService;
    }


    @RequestMapping(value = "/ebooks/index", method = RequestMethod.GET)
    public String home(Model model, WebSession session, Principal principal) {
        List<String> token = new ArrayList<>();
        model.addAttribute("books", bookRepository.getBooks().flatMap(response -> Flux.fromIterable(response.getBooks())));
        model.addAttribute("continuationToken",bookRepository.getBooks().map(Response::getContinuationToken));
        model.addAttribute("cartItemCount", cartService.getNumberOfItemsInTheCart(session.getId()));
        model.addAttribute("customerId", principal.getName());

        return "index";
    }

    @PostMapping(value = "/ebooks/next")
    @ResponseBody
    public Mono<Response> next(@RequestBody String continuationToken, WebSession session, Principal principal) {
        logger.info("CALLING NEXT ACTION");
        Flux<Response> result=bookRepository.getBooksPage(continuationToken);
        bookRepository.getBooksPage(continuationToken).map(Response::getBooks);
        return result.single();
    }


    @RequestMapping(value = "/ebooks/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
    }
}
