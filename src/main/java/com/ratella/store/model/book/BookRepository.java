package com.ratella.store.model.book;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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
import java.util.List;


@Service
public class BookRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CartCosmosDB cosmosDB;

    @Autowired
   public BookRepository(CartCosmosDB cosmosDB){
        this.cosmosDB = cosmosDB;
    }

    public Flux<Book> getReadingList(String reader) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        String query = "SELECT * FROM ReadingList r WHERE r.reader = " + "'" + reader + "'";
        CosmosPagedFlux<Book> pagedFluxResponse = cosmosDB.getContainer().queryItems(
                query, queryOptions, Book.class);
        return pagedFluxResponse
                .byPage()
                .flatMap(page -> Flux.fromIterable(page.getElements()));
    }

    public Mono<Integer> createBook(Book book) {
        return cosmosDB.getContainer()
                .createItem(book)
                .map(CosmosItemResponse::getStatusCode);

    }

    public Mono<Integer> updateBook(Book book) {
        return cosmosDB.getContainer()
                .replaceItem(book, book.getId(), new PartitionKey(book.getCategory()))
                .map(CosmosItemResponse::getStatusCode);
    }

    public Mono<Book> findBookByID(String id, String partitionKey) {
        return cosmosDB.getContainer()
                .readItem(id, new PartitionKey(partitionKey), Book.class)
                .map(CosmosItemResponse::getItem);
    }

    public Mono<Integer> deleteBookByID(String id, String partitionKey) {
        return cosmosDB.getContainer()
                .deleteItem(id, new PartitionKey(partitionKey))
                .map(CosmosItemResponse::getStatusCode);
    }

    public Flux<Book> getBooks(){
        List<Book> books = new ArrayList<Book>();
        books.add(new Book("1","Software Engineering", "01234", "Getting Started with kubernetes", "Jonathan Baier", "Learn Kubernetes the right way", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/Kubernetes.jpg"));
        books.add(new Book("2", "Software Engineering","95201", "Learning Docker Networking", "Rajdeep Das", "Docker networking deep dive", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/DockerNetworking.jpg"));
        books.add(new Book("6", "Software Engineering","95298", "Spring Microservices", "Rajesh RV", "Build scalable microservices with Spring and Docker", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/SpringMicroServices.jpg"));
        books.add(new Book("5", "Software Engineering","01264", "Learning Concurrent Programming in Scala", "Aleksandar Prokopec", "Learn the art of building concurrent applications", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/Scala.jpg"));
        books.add(new Book("3", "Software Engineering","23123", "Modern Authentication with AzureAD ", "Vittorio Bertocci", "Azure active directory capabilities", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/AzureAD.jpg"));
        books.add(new Book("4", "Software Engineering","11201", "Microsoft Azure SQL", "Leonard G.Lobel", "Setp by step guide for developers", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/AzureSQL.jpg"));
        books.add(new Book("7", "Software Engineering","28526", "Developing Azure and Web Services", "Rajdeep Das", "Exam Ref 70-487", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/AzureCert.jpg"));
        books.add(new Book("8", "Software Engineering","95298", "Programming Microsoft Azure Service fabric", "Haishi Bai", "Service fabric for developers", new BigDecimal(40.00), new BigDecimal(30.00),"https://mtchouimages.blob.core.windows.net/books/ServiceFabric.jpg"));
        return Flux.fromIterable(books);
    }

}
