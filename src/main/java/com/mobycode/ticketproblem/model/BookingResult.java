package com.mobycode.ticketproblem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor //for Jackson serialisation
public class BookingResult implements Serializable {
    private BookingRequest bookingRequest;
    private BookTicketResult bookTicketResult;
}
