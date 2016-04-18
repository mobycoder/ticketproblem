package com.mobycode.ticketproblem.service;

import com.mobycode.ticketproblem.model.BookingRequest;
import com.mobycode.ticketproblem.model.BookingResult;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {

 BookingResult book(BookingRequest bookingRequest);


}
