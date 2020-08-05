package com.ratella.store.model.book;

import reactor.core.publisher.Flux;

import java.util.List;

public class Response {
    private String continuationToken;
    private List<Book> books;

    public Response(String continuationToken, List<Book> books) {
        this.continuationToken = continuationToken;
        this.books = books;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public List<Book> getBooks() {
        return books;
    }

    @Override
    public String toString() {
        return "Response{" +
                "continuationToken='" + continuationToken + '\'' +
                '}';
    }
}
