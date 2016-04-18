package com.mobycode.ticketproblem;

import com.mobycode.ticketproblem.grid.GridDataNames;
import com.mobycode.ticketproblem.grid.entryprocessor.BookTicket;
import com.mobycode.ticketproblem.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;
import java.util.stream.LongStream;

@Slf4j
@Ignore  //remove or comment this to activate these tests
public class RemoteClientTest {

    private static ProducerTemplate producerTemplate;
    private static CamelContext camelContext;
    private final Long ticketId = 500L;
    private static Ignite ignite;
    private IgniteCache<Long, Ticket> ticketMap;
    private IgniteCache<Long, Customer> customerMap;


    //number of threads to run for parallel tests
    private static final int NUMBER_OF_THREADS = 6;

    //executor for parallel processing
    private final Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    //completion service
    private final CompletionService<BookingResult> completionService = new ExecutorCompletionService<>(executor);

    @BeforeClass
    public static void setupOnce() throws Exception {
        Ignition.setClientMode(true);
        ignite = Ignition.start();
        camelContext = new DefaultCamelContext();
        camelContext.setAllowUseOriginalMessage(false);
        camelContext.addRoutes(createRoutes());
        camelContext.start();
        producerTemplate = camelContext.createProducerTemplate();
    }

    @Before
    public void setup() throws Exception {
        //clear and repopulate grid before starting
        clearAndFillGrid();
    }


    @Test
    public void bookParallelWithJsonOverHTTP() {
        final Long numberOfCustomers = customerMap.sizeLong();
        StopWatch stopWatch = new StopWatch("How long for "+ numberOfCustomers +" customers to attempt to book a ticket");
        stopWatch.start("all customers attempt to book one ticket in parallel using a HTTP/JSON service");
        ResultRecorder recorder = new ResultRecorder();
        LongStream.rangeClosed(1, numberOfCustomers).forEach(entry -> {
                    completionService.submit(
                            () -> producerTemplate.requestBody(
                                    "direct:bookTicketOverHttp",
                                    new BookingRequest(ticketId, entry),
                                    BookingResult.class)
                    );
        });
        LongStream.rangeClosed(1, numberOfCustomers)
                .forEach(num -> {
                    try {
                        BookingResult bookingResult = completionService.take().get();
                        recorder.recordResult(bookingResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        recorder.printResults();
        recorder.checkResults();
    }


    @Test
    public void bookParallelWithClientsideEntryProcessor() {
        final Long numberOfCustomers = customerMap.sizeLong();
        StopWatch stopWatch = new StopWatch("How long for "+ numberOfCustomers +" customers to attempt to book a ticket");
        stopWatch.start("all customers attempt to book one ticket in parallel using an entry processor");
        ResultRecorder recorder = new ResultRecorder();
        LongStream.rangeClosed(1, numberOfCustomers).forEach(customerKey -> {
            BookingRequest bookingRequest = new BookingRequest(ticketId, customerKey);
            completionService.submit(
                    () -> new BookTicket(ticketMap).book(bookingRequest)
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

    /*
        Create local rest client
     */
    private static RoutesBuilder createRoutes() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:bookTicketOverHttp")
                        .marshal()
                        .json(JsonLibrary.Jackson, BookingRequest.class)
                        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                        .setHeader("CamelHttpMethod", constant("PUT"))
                        .to("http4://localhost:8080/ticket/book")
                        .unmarshal()
                        .json(JsonLibrary.Jackson, BookingResult.class);
            }
        };
    }



    private void clearAndFillGrid() {

        ticketMap = ignite.getOrCreateCache(GridDataNames.MAP_TICKET.getName());
        customerMap = ignite.getOrCreateCache(GridDataNames.MAP_CUSTOMER.getName());
        log.debug("Start clearing grid");
        ticketMap.clear();
        customerMap.clear();
        log.debug("Finished clearing grid");
        log.debug("Start filling grid");
        LongStream.rangeClosed(1, 1000).parallel().forEach(key -> ticketMap.put(key, new Ticket(key)));
        LongStream.rangeClosed(1, 60000).parallel().forEach(key -> customerMap.put(key, new Customer(key)));
        log.debug("Finished filling grid");
    }
}
