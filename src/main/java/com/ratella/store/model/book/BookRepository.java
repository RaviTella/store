package com.ratella.store.model.book;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.ratella.store.model.cart.CartCosmosDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class BookRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BookCosmosDB cosmosDB;

    @Autowired
    public BookRepository(BookCosmosDB cosmosDB) {
        this.cosmosDB = cosmosDB;
    }

    public Flux<Response> getBooks() {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM Book";
        CosmosPagedFlux<Book> pagedFluxResponse = cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Book.class);
        return pagedFluxResponse
                .byPage(18)
                .take(1)
                .map(page -> {
                return new Response(page.getContinuationToken(),page.getResults());
                });
    }


    public Flux<Response>  getBooksPage(String continuationToken) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM Book";
        CosmosPagedFlux<Book> pagedFluxResponse = cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Book.class);
        return pagedFluxResponse
                .byPage(continuationToken,6)
                .take(1)
                .map(page -> {
                    return new Response(page.getContinuationToken(),page.getResults());
                });
    }

}
