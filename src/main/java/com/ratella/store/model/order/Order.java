package com.ratella.store.model.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private String id;
    private String status;
    private String customerId;
    private BigDecimal subTotal;
    private List<LineItem> lineItems = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public String getCustomerId() { return customerId;}

    public void setCustomerId(String customerId) { this.customerId = customerId;}

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }
}
