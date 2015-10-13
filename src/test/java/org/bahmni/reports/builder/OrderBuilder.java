package org.bahmni.reports.builder;

import java.util.Date;
import java.util.UUID;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.OrderType;

public class OrderBuilder {
    private Order order;

    public OrderBuilder() {
        this.order = new Order();
        this.order.setDateCreated(new Date());
        this.order.setUuid(UUID.randomUUID().toString());
        this.order.setDateCreated(new Date());
    }

    public OrderBuilder withUuid(String uuid) {
        order.setUuid(uuid);
        return this;
    }

    public OrderBuilder withId(Integer id) {
        order.setId(id);
        return this;
    }

    public OrderBuilder withConcept(Concept concept) {
        order.setConcept(concept);
        return this;
    }

    public OrderBuilder withOrderType(OrderType orderType) {
        order.setOrderType(orderType);
        return this;
    }

    public Order build() {
        return order;
    }
}
