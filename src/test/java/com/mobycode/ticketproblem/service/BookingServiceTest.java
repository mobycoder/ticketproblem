package com.mobycode.ticketproblem.service;

import com.mobycode.ticketproblem.TicketProblemApplication;
import com.mobycode.ticketproblem.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;
import java.util.stream.LongStream;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TicketProblemApplication.class)
public class BookingServiceTest {

    @Autowired
    private IgniteCache<Long, Ticket> ticketMap;

    @Autowired
    private IgniteCache<Long, Customer> customerMap;

    @Autowired
    private BookingService bookingService;

    private Executor executor;
    private CompletionService<BookingResult> completionService;

    private final Long ticketId = 500L;

    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(6);
        completionService = new ExecutorCompletionService<>(executor);
        clearAndFillGrid();
    }


    private void clearAndFillGrid() {
        ticketMap.clear();
        customerMap.clear();
        StopWatch stopWatch = new StopWatch("Time to fill grid objects");
        //create 1000 tickets
        stopWatch.start("time to fill ticket map with 1000 tickets");
        LongStream.rangeClosed(1, 1000).parallel().forEach(key -> ticketMap.put(key, new Ticket(key)));
        stopWatch.stop();
        //create 60000 customers
        stopWatch.start("time to fill customer map with 60000 customers");
        LongStream.rangeClosed(1, 60000).parallel().forEach(key -> customerMap.put(key, new Customer(key)));
        stopWatch.stop();
        log.debug(stopWatch.prettyPrint());
    }

    @Test
    public void bookSerially() throws Exception {
        StopWatch stopWatch = new StopWatch("How long for all customers to attempt to book a ticket");
        stopWatch.start("all customers (" + customerMap.size() + ") attempt to book one ticket serially");
        ResultRecorder recorder = new ResultRecorder();
        customerMap.forEach(entry -> {
            customerMap.invoke(entry.getKey(), (customerEntry, object) -> {
                        BookingResult bookingResult = bookingService.book(new BookingRequest(ticketId, customerEntry.getValue().getId()));
                        recorder.recordResult(bookingResult);
                        return null;
                    }
            );
        });
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        log.info("Results were: ");
        recorder.printResults();
        recorder.checkResults();
    }


    @Test
    public void bookParallel() {
        final Long numberOfCustomers = customerMap.sizeLong();
        Ticket ticket = ticketMap.get(ticketId);
        log.info("Pre booking ticket details - available:{}, customer ref {}",
                ticket.getAvailable(),
                ticket.getCustomerReference());
        StopWatch stopWatch = new StopWatch("How long for all customers to attempt to book a ticket");
        stopWatch.start("all customers attempt to book one ticket in parallel");
        ResultRecorder recorder = new ResultRecorder();
        customerMap.forEach(entry -> {
            completionService.submit(
                    () -> bookingService.book(new BookingRequest(ticketId, entry.getKey()))
            );
        });
        LongStream.rangeClosed(1, numberOfCustomers)
                .forEach(num -> {
                    try {
                        recorder.recordResult(completionService.take().get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        recorder.printResults();
        recorder.checkResults();
    }

}

