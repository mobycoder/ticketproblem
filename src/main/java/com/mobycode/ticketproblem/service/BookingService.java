package com.mobycode.ticketproblem.service;

import com.mobycode.ticketproblem.model.BookingRequest;
import com.mobycode.ticketproblem.model.BookingResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {

public BookingResult book(BookingRequest bookingRequest);


}
