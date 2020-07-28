package com.ratella.store.model.order;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.ThroughputProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderCosmosDB {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String endpoint;
    private String key;
    private String databaseName;
    private List<String> containerNames;
    private List<String> locations;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private Map<String, CosmosAsyncContainer> containerNameToContainer;


    public OrderCosmosDB(@Value("${database.endpoint}") final String endpoint, @Value("${database.key}") final String key,
                         @Value("${database.databaseName}") final String databaseName, @Value("#{'${database.containerName.order}'.split(',')}") final List<String> containerNames,
                         @Value("#{'${database.locations}'.split(',')}") final List<String> locations) {
        this.endpoint = endpoint;
        this.key = key;
        this.databaseName = databaseName;
        this.containerNames = containerNames;
        this.locations = locations;
        this.containerNameToContainer = new HashMap<>();
        cosmosSetup();
    }

    private void cosmosSetup() {
        logger.info("CREATING COSMOS CONTAINER ");
        containerNames
                .forEach(containerName -> {
                    CosmosAsyncContainer cosmosAsyncContainer = cosmosCreateResources(containerName);
                    containerNameToContainer.put(cosmosAsyncContainer.getId(), cosmosAsyncContainer);
                    logger.info("CREATED CONTAINER: "+ containerName);
                });

    }

    private CosmosAsyncContainer cosmosCreateResources(String containerName) {
        logger.info("CREATING COSMOS CONTAINER " + containerName);
        CosmosContainerProperties containerProperties;
        if (containerName.contains("-leas")) {
            containerProperties = new CosmosContainerProperties(containerName,"/id");
        } else{containerProperties = new CosmosContainerProperties(containerName, "/customerId");}

        return buildAndGetClient()
                .createDatabaseIfNotExists(databaseName)
                .flatMap(databaseResponse -> {
                    database = client.getDatabase(databaseResponse
                            .getProperties()
                            .getId());
                    return database
                            .createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400));
                })
                .map(containerResponse -> {
                    return database.getContainer(containerResponse
                            .getProperties()
                            .getId());
                })
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
                    .contentResponseOnWriteEnabled(true)
                    .buildAsyncClient();
            return client;
        }
        return client;
    }

    public CosmosAsyncContainer getContainer(String name) {
        return this.containerNameToContainer.get(name);
    }


}
