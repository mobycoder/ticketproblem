package com.mobycode.ticketproblem.model;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ResultRecorder {
    Map<BookTicketResult, Long> results = new HashMap<>(BookTicketResult.values().length);

    public void recordResult(BookTicketResult bookTicketResult) {
        if (results.containsKey(bookTicketResult)) {
            Long oldCount = results.get(bookTicketResult);
            results.replace(bookTicketResult, ++oldCount);
        } else {
            results.put(bookTicketResult, 1l);
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

    public long getCountForResultType(BookTicketResult bookTicketResult){
        return results.containsKey(bookTicketResult) ? results.get(bookTicketResult) : 0;
    }
}