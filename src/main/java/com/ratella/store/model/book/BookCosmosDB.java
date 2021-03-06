package com.ratella.store.model.book;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookCosmosDB {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String endpoint;
    private String key;
    private String databaseName;
    private String containerName;
    private List<String> locations;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public BookCosmosDB(@Value("${database.endpoint}") final String endpoint, @Value("${database.key}") final String key,
                        @Value("${database.databaseName}") final String databaseName, @Value("${database.containerName.book}") final String containerName,
                        @Value("#{'${database.locations}'.split(',')}") final List<String> locations) {
        this.endpoint = endpoint;
        this.key = key;
        this.databaseName = databaseName;
        this.containerName = containerName;
        this.locations = locations;
        cosmosSetup();
        loadBooks();
    }


    private void cosmosSetup() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/category");
        buildAndGetClient()
                .createDatabaseIfNotExists(databaseName)
                .flatMap(databaseResponse -> {
                    database = client.getDatabase(databaseResponse
                            .getProperties()
                            .getId());
                    return database
                            .createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400));
                })
                .flatMap(containerResponse -> {
                    container = database.getContainer(containerResponse
                            .getProperties()
                            .getId());
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.elastic())
                .block();
    }


    private CosmosAsyncClient buildAndGetClient() {
        if (client == null) {
            logger.info(endpoint);
            client = new CosmosClientBuilder()
                    .endpoint(endpoint)
                    .key(key)
                    .preferredRegions(locations)
                    .consistencyLevel(ConsistencyLevel.SESSION)
                    .buildAsyncClient();
            return client;
        }
        return client;
    }

    public CosmosAsyncContainer getContainer() {
        return container;
    }

    public void loadBooks() {
        List<Book> books = new ArrayList<Book>();
        books.add(new Book("1", "Software Engineering", "01234", "Getting Started with kubernetes", "Jonathan Baier", "Learn Kubernetes the right way", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/Kubernetes.jpg"));
        books.add(new Book("2", "Software Engineering", "95201", "Learning Docker Networking", "Rajdeep Das", "Docker networking deep dive", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/DockerNetworking.jpg"));
        books.add(new Book("3", "Software Engineering", "95298", "Spring Microservices", "Rajesh RV", "Build scalable microservices with Spring and Docker", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/SpringMicroServices.jpg"));
        books.add(new Book("4", "Software Engineering", "01264", "Learning Concurrent Programming in Scala", "Aleksandar Prokopec", "Learn the art of building concurrent applications", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/Scala.jpg"));
        books.add(new Book("5", "Software Engineering", "23123", "Modern Authentication with AzureAD ", "Vittorio Bertocci", "Azure active directory capabilities", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/AzureAD.jpg"));
        books.add(new Book("6", "Software Engineering", "11201", "Microsoft Azure SQL", "Leonard G.Lobel", "Setp by step guide for developers", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/AzureSQL.jpg"));
        books.add(new Book("7", "Software Engineering", "28526", "The Pragmatic Programmer", "Andrew Hunt", "Your Journey To Mastery", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/pragmaticProgrammer.jpg"));
        books.add(new Book("8", "Software Engineering", "95298", "Programming Microsoft Azure Service fabric", "Haishi Bai", "Service fabric for developers", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/ServiceFabric.jpg"));
        books.add(new Book("9", "Software Engineering", "95233", "Become An Awesome Software Architect", "Anatoly Volkhover", "Software architecture for developers", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/softwareArchitect.jpg"));
        books.add(new Book("10", "Software Engineering", "65433", "Designing Data-Intensive Applications", "Martin Kleppmann", "The Big Ideas Behind Reliable, Scalable, and Maintainable Systems", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/dataIntensiveApplication.jpg"));
        books.add(new Book("11", "Software Engineering", "75493", "Effective Java", "Joshua Bloch", "Best practices for the java platform", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/effectivejava.jpg"));
        books.add(new Book("12", "Software Engineering", "75493", "Clean Code", "Robert C. Martin", "A Handbook of Agile Software Craftsmanship", new BigDecimal(40.00), new BigDecimal(30.00), "https://mtchouimages.blob.core.windows.net/books/cleanCode.jpg"));

        books.add(new Book("13", "Software Engineering", "76993", "Black Hat Go", "Tom Steele", "Go Programming for Hackers and Presenters", new BigDecimal(60.00), new BigDecimal(50.00), "https://mtchouimages.blob.core.windows.net/books/blackHatGo.jpg"));
        books.add(new Book("14", "Software Engineering", "76093", "C# 7.0 in a Nutshell", "Joseph Albahari", "The Definitive Reference", new BigDecimal(70.00), new BigDecimal(60.00), "https://mtchouimages.blob.core.windows.net/books/csharpInANutshell.jpg"));
        books.add(new Book("15", "Software Engineering", "16093", "Get Programming With Go", "Nathan Youngman", "Learn GO Programming Fast", new BigDecimal(66.00), new BigDecimal(50.00), "https://mtchouimages.blob.core.windows.net/books/getProgrammingWithGo.jpg"));
        books.add(new Book("16", "Software Engineering", "13093", "Go In Practice", "Matt Butcher", "Includes 70 Techniques", new BigDecimal(52.00), new BigDecimal(40.00), "https://mtchouimages.blob.core.windows.net/books/GoInPractice.jpg"));

        books.add(new Book("17", "Software Engineering", "63692", "High Performance MySQL", "Baron Schwartz", "Optimization, Backups, and Replication", new BigDecimal(67.00), new BigDecimal(52.00), "https://mtchouimages.blob.core.windows.net/books/highPerformaceMysql.jpg"));
        books.add(new Book("18", "Software Engineering", "27692", "Mastering Kotlin", "Nate Ebel", "Learn advanced Kotlin programming techniques to build apps for Android, iOS, and the web", new BigDecimal(77.00), new BigDecimal(59.00), "https://mtchouimages.blob.core.windows.net/books/masterring-kotlin.jpg"));
        books.add(new Book("19", "Software Engineering", "30697", "Node Cookbook", "David Mark Clements", "Actionable solutions for the full spectrum of Node.js 8 development", new BigDecimal(40.00), new BigDecimal(38.00), "https://mtchouimages.blob.core.windows.net/books/nodeCookBook.jpg"));
        books.add(new Book("20", "Software Engineering", "76297", "Node.js Design Patterns", "Mario Casciaro", "Master best practices to build modular and scalable server-side web applications", new BigDecimal(60.00), new BigDecimal(58.00), "https://mtchouimages.blob.core.windows.net/books/node-design-patterns.jpg"));
        books.add(new Book("21", "Software Engineering", "96297", "Patterns of Enterprise Application Architecture", "Martin Fowler", "Designing, Building, and Deploying Enterprice Solutions", new BigDecimal(65.00), new BigDecimal(58.00), "https://mtchouimages.blob.core.windows.net/books/patternsOfEAA.jpg"));
        books.add(new Book("22", "Software Engineering", "96297", "Programming C# 8.0", "Ian Griffiths", "Build Cloud, Web, and Desktop Applications", new BigDecimal(75.00), new BigDecimal(59.00), "https://mtchouimages.blob.core.windows.net/books/programmingcsharp8.0.jpg"));
        books.add(new Book("23", "Software Engineering", "88297", "The Rust Programming Language", "Steve Klabnik", "The official book on the Rust programming language", new BigDecimal(55.00), new BigDecimal(49.00), "https://mtchouimages.blob.core.windows.net/books/RustProgrammingLanguage.jpg"));
        books.add(new Book("24", "Software Engineering", "68597", "Unit Testing", "Vladimir Khorikov", "Principles, Practices, and Patterns 1st Edition", new BigDecimal(55.00), new BigDecimal(49.00), "https://mtchouimages.blob.core.windows.net/books/unitTesting.jpg"));

        books.add(new Book("25", "Software Engineering", "67597", "Cassandra, The Definitive Guide", "Jeff Carpenter", "Distributed Data at Web Scale 3rd Edition", new BigDecimal(65.00), new BigDecimal(59.00), "https://mtchouimages.blob.core.windows.net/books/CassandraDefinitiveguide.jpg"));
        books.add(new Book("26", "Software Engineering", "67587", "Clean Code", "Robert C. Martin", "A Handbook of Agile Software Craftsmanship 1st Edition", new BigDecimal(49.00), new BigDecimal(35.00), "https://mtchouimages.blob.core.windows.net/books/cleanCode.jpg"));
        books.add(new Book("27", "Software Engineering", "23587", "Docker in Practice", "Ian Miell", "2nd Edition", new BigDecimal(49.00), new BigDecimal(44.00), "https://mtchouimages.blob.core.windows.net/books/DockerInPractice.jpg"));
        books.add(new Book("28", "Software Engineering", "23227", "Elasticsearch in Action", "Ian Miell", "1st Edition", new BigDecimal(44.00), new BigDecimal(42.00), "https://mtchouimages.blob.core.windows.net/books/ElasticSearchInAction.jpg"));
        books.add(new Book("29", "Software Engineering", "24697", "Graph Algorithms", "Mark Needham", "Practical Examples in Apache Spark and Neo4j 1st Edition", new BigDecimal(45.00), new BigDecimal(41.00), "https://mtchouimages.blob.core.windows.net/books/GraphAlgorithms.jpg"));
        books.add(new Book("30", "Software Engineering", "44897", "The Practitioner's Guide to Graph Data", "Denise Gosnell", "Applying Graph Thinking and Graph Technologies to Solve Complex Problems 1st Edition", new BigDecimal(45.00), new BigDecimal(35.00), "https://mtchouimages.blob.core.windows.net/books/GuideToGraphDatabases.jpg"));
        books.add(new Book("31", "Software Engineering", "43147", "Java Concurrency in Practice", "Brian Goetz", "1st Edition", new BigDecimal(59.00), new BigDecimal(19.00), "https://mtchouimages.blob.core.windows.net/books/JavaConcurrency.jpg"));
        books.add(new Book("32", "Software Engineering", "53147", "Kafka, The Definitive Guide", "Neha Narkhede", "Real-Time Data and Stream Processing at Scale", new BigDecimal(54.00), new BigDecimal(35.00), "https://mtchouimages.blob.core.windows.net/books/kaafkDefnitiveGuide.jpg"));
        books.add(new Book("33", "Software Engineering", "58147", "MongoDB, The Definitive Guide", "Shannon Bradshaw", "Powerful and Scalable Data Storage 3rd Edition", new BigDecimal(35.00), new BigDecimal(28.00), "https://mtchouimages.blob.core.windows.net/books/mongodb.jpg"));
        books.add(new Book("34", "Software Engineering", "77147", "Programming Ruby 1.9 & 2.0", "Dave Thomas", "The Pragmatic Programmers' Guide (The Facets of Ruby) 4th Edition", new BigDecimal(30.00), new BigDecimal(18.00), "https://mtchouimages.blob.core.windows.net/books/ProgrammingInRuby.jpg"));
        books.add(new Book("35", "Software Engineering", "79847", "RabbitMQ Cookbook", "Sigismondo Boschi", "Over 70 practical recipes to help you build messaging applications", new BigDecimal(44.00), new BigDecimal(39.00), "https://mtchouimages.blob.core.windows.net/books/rabbitMQCookBook.jpg"));
        books.add(new Book("36", "Software Engineering", "99847", "Seven Databases in Seven Weeks", "Luc Perkins", "A Guide to Modern Databases and the NoSQL Movement", new BigDecimal(47.00), new BigDecimal(43.00), "https://mtchouimages.blob.core.windows.net/books/SevenDatabasesInSevenWeeks.jpg"));


        Flux
                .fromIterable(books)
                .flatMap(this.container::upsertItem)
                .blockLast();

    }


}
