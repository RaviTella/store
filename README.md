# Overview
This sample ecommerce application(sells only a few books:) for now) is built with Spring Boot 2.3.1 on Spring WebFlux stack. This application uses Azure Cosmos DB SQL API with the Java SDK 4.0, whish is based on the reactor core.
The current version has the following capabilities:
* Display catalog
* Add items to cart
* Remove items from cart
* Check out
* Order creation and confirmation

I will be adding additional capabilities as time permits. Following are a few I shortlisted:
* Stored procedures 
* Optimistic concurrency 
* Multi-Master
* View and edit order
* Catalog search
* Customer registration (currently I have 2 default user accounts :))

Additionally, I plan to decompose the application into microservices and leverage spring cloud capabilities to address some of the distributed system challenges.

# Instructions

## First:
 * Java 8
 * Maven
 * Create a Cosmos DB SQL API Account. 
 * Clone the repo

## Then:
* Update the cosmos DB endpoint and key information in application.properties
* mvn spring-boot:run from the project base
* On straup the application creates 3 cosmos collections, namely book, order, cart. Each one will be configured with 400 RUs.
* Access the WebApp at http://localhost/ebooks/index
* Default user accounts customer1/customer1 and customer2/customer2
* If you prefer docker:  
  * mvnw package 
  * docker build -t <YOUR REPO>/store .
  * docker run -p 80:80 -e database.endpoint=<URI> -e database.key=<PRIMARY KEY> -t <YOUR REPO>/store
  * Access the WebApp at http://localhost/ebooks/index with the default accounts mentioned previously
* If you just want to run a container, you could use mine:
  * docker run -p 80:80 -e database.endpoint=<URI> -e database.key=<> -t ravitella/store
  * Access the WebApp at http://localhost/ebooks/index with the default accounts mentioned previously 

