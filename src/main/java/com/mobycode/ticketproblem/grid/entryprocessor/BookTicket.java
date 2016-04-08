package com.mobycode.ticketproblem.grid.entryprocessor;

import com.mobycode.ticketproblem.model.BookTicketResult;
import com.mobycode.ticketproblem.model.BookingRequest;
import com.mobycode.ticketproblem.model.BookingResult;
import com.mobycode.ticketproblem.model.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
    Entry processor to book one ticket for one customer - the ticket is locked for the duration of the invocation
 */

@Slf4j
@Component
public class BookTicket {


    private IgniteCache<Long, Ticket> ticketMap;

    @Autowired
    public BookTicket(IgniteCache<Long, Ticket> ticketMap) {
        this.ticketMap = ticketMap;
    }

    public BookingResult book(BookingRequest bookingRequest){
        return book(bookingRequest.getTicketId(),bookingRequest.getCustomerId());
    };

    public BookingResult book(Long ticketId, Long customerId){
        try {
            return ticketMap.invoke(ticketId, (mutableEntry, objects) -> {
                Ticket ticket = mutableEntry.getValue();
                if (ticket.getAvailable()) {
                    ticket.setAvailable(false);
                    ticket.setCustomerReference(customerId);
                    mutableEntry.setValue(ticket);
                    log.info("ticket booked successful - ticket id was: {}, customer id was: {}",ticketId,customerId);
                    return new BookingResult(new BookingRequest(ticketId,customerId),BookTicketResult.TICKET_BOOKED);
                }
                return new BookingResult(new BookingRequest(ticketId,customerId),BookTicketResult.TICKET_NOT_AVAILABLE);
            });
        } catch (Exception e){
            log.error("An occurred attempting to book a ticket with ticketid:{} and customerid:{}", ticketId, customerId, e);
            return new BookingResult(new BookingRequest(ticketId,customerId),BookTicketResult.BOOKING_ERROR);
        }
    }


}
