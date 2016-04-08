package com.mobycode.ticketproblem.service;

import com.mobycode.ticketproblem.grid.entryprocessor.BookTicket;
import com.mobycode.ticketproblem.model.BookingRequest;
import com.mobycode.ticketproblem.model.BookingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class BookingServiceImpl implements  BookingService {

    @Autowired
    private BookTicket bookTicket;

    @Override
    public BookingResult book(BookingRequest bookingRequest) {
        return  bookTicket.book(bookingRequest);
    }
}
