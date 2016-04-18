package com.mobycode.ticketproblem.model;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

@Slf4j
public class ResultRecorder {
    private final Map<BookTicketResult, Long> results = new HashMap<>(BookTicketResult.values().length);

    private void recordResult(BookTicketResult bookTicketResult) {
        if (results.containsKey(bookTicketResult)) {
            Long oldCount = results.get(bookTicketResult);
            results.replace(bookTicketResult, ++oldCount);
        } else {
            results.put(bookTicketResult, 1L);
        }
    }
    public void recordResult(BookingResult bookingResult){
        recordResult(bookingResult.getBookTicketResult());
    }

    public void printResults() {
        for (BookTicketResult result : BookTicketResult.values()) {
            String resultNum = Objects.isNull(results.get(result)) ? "0" : results.get(result).toString();
            log.info("Result for {} was {}", result.getLabel(), resultNum);
        }
    }

    private long getCountForResultType(BookTicketResult bookTicketResult){
        return results.containsKey(bookTicketResult) ? results.get(bookTicketResult) : 0;
    }

    public void checkResults(){
        assertTrue(getCountForResultType(BookTicketResult.BOOKING_ERROR)==0);
        assertTrue(getCountForResultType(BookTicketResult.TICKET_NOT_AVAILABLE)==59999);
        assertTrue(getCountForResultType(BookTicketResult.TICKET_BOOKED)==1);
    }
}