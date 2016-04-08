package com.mobycode.ticketproblem.model;

import java.io.Serializable;

public enum BookTicketResult implements Serializable {

    TICKET_BOOKED("Ticket booked"),
    TICKET_NOT_AVAILABLE("Ticket not available"),
    BOOKING_ERROR("Error while booking");

    private String label;

    BookTicketResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
