package org.bahmni.reports.builder;

import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderBuilder {
    private Order order;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public OrderBuilder() {
        this.order = new Order();
        this.order.setCareSetting(Context.getOrderService().getCareSetting(3));
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

    public OrderBuilder withDateActivated(String dateActivated) {
        try {
            order.setDateActivated(dateFormat.parse(dateActivated));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Order build() {
        return order;
    }

    public OrderBuilder withEncounter(Encounter encounter) {
        order.setEncounter(encounter);
        return this;
    }

    public OrderBuilder withOrderer(Provider orderer) {
        order.setOrderer(orderer);
        return this;
    }

    public OrderBuilder withPatient(Patient patient) {
        order.setPatient(patient);
        return this;
    }
}
