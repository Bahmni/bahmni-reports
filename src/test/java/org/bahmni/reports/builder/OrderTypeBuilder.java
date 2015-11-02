package org.bahmni.reports.builder;

import org.openmrs.OrderType;

import java.util.Date;
import java.util.UUID;

public class OrderTypeBuilder {
    private OrderType orderType;

    public OrderTypeBuilder() {
        this.orderType = new OrderType();
        this.orderType.setDateCreated(new Date());
        this.orderType.setUuid(UUID.randomUUID().toString());
        this.orderType.setDateCreated(new Date());
    }

    public OrderType build() {
        return orderType;
    }

    public OrderTypeBuilder withJavaClassName(String className) {
        orderType.setJavaClassName(className);
        return this;
    }

    public OrderTypeBuilder withOrderType(String orderTypeName) {
        this.orderType.setName(orderTypeName);
        return this;
    }

    public OrderTypeBuilder withId(Integer id) {
        orderType.setId(id);
        return this;
    }
}
