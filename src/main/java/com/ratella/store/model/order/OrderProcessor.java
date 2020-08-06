package com.ratella.store.model.order;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
public class OrderProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OrderCosmosDB cosmosDB;

    @Autowired
    public OrderProcessor(OrderCosmosDB orderCosmosDB) {
        this.cosmosDB = orderCosmosDB;
    }

    @PostConstruct
    public void start() {
        ChangeFeedProcessor changeFeedProcessorInstance = getChangeFeedProcessor("StoreOrderHot_1", cosmosDB.getContainer("order"), cosmosDB.getContainer("order-lease"));
        changeFeedProcessorInstance
                .start()
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public ChangeFeedProcessor getChangeFeedProcessor(String hostName, CosmosAsyncContainer feedContainer, CosmosAsyncContainer leaseContainer) {
        ChangeFeedProcessorOptions changeFeedOptions = new ChangeFeedProcessorOptions();
        changeFeedOptions.setFeedPollDelay(Duration.ofSeconds(20));
        changeFeedOptions.setStartFromBeginning(true);
        return new ChangeFeedProcessorBuilder()
                .options(changeFeedOptions)
                .hostName(hostName)
                .feedContainer(feedContainer)
                .leaseContainer(leaseContainer)
                .handleChanges((List<JsonNode> docs) -> {
                    for (JsonNode document : docs) {
                        ObjectMapper mapper = new ObjectMapper();
                        Order order = null;
                        try {
                            order = mapper.convertValue(document, Order.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (order
                                .getStatus()
                                .equals("RECEIVED")) {
                            order.setStatus("SHIPPED");
                            logger.info("Processing Order # " + order.getId());
                            this.cosmosDB
                                    .getContainer("order")
                                    .upsertItem(order)
                                    .subscribe();
                        }
                    }

                })
                .buildChangeFeedProcessor();
    }


}
