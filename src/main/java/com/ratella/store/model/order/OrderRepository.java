package com.ratella.store.model.order;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.ratella.store.model.cart.CartCosmosDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OrderCosmosDB cosmosDB;

    @Autowired
    public OrderRepository(OrderCosmosDB cosmosDB) {
        this.cosmosDB = cosmosDB;
    }

    public Mono<Integer> createOrder(Order order) {
        return cosmosDB
                .getContainer("order")
                .createItem(order)
                .map(CosmosItemResponse::getStatusCode);
    }

    public Flux<Order> getOrders(String customerId) {
        String query = "SELECT * FROM o WHERE o.customerId =  @customerId ORDER BY o._ts DESC";
        SqlParameter parameter = new SqlParameter("@customerId", customerId);
        List<SqlParameter> sqlParameters = new ArrayList<>();
        sqlParameters.add(parameter);
        SqlQuerySpec querySpec = new SqlQuerySpec(query, sqlParameters);
        return cosmosDB
                .getContainer("order")
                .queryItems(querySpec, Order.class)
                .byPage()
                .flatMap(page -> Flux.fromIterable(page.getElements()));
    }
}
