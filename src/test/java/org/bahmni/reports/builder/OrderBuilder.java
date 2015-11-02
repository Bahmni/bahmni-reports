package org.bahmni.reports.builder;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderBuilder {
    private Order order;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public OrderBuilder() {
        this.order = new Order();
        this.order.setCareSetting(Context.getOrderService().getCareSetting(3));
    }

    public Order build() {
        return order;
    }

    public OrderBuilder withConcept(Concept concept) {
        order.setConcept(concept);
        return this;
    }

    public OrderBuilder withOrderType(OrderType orderType) {
        order.setOrderType(orderType);
        return this;
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

    public OrderBuilder setDateActivated(String dateActivated) {
        order.setDateActivated(DateUtil.parseDate(dateActivated));
        return this;
    }
}
