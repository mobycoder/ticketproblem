package com.mobycode.ticketproblem.camel;

import com.mobycode.ticketproblem.model.BookingRequest;
import com.mobycode.ticketproblem.model.BookingResult;
import com.mobycode.ticketproblem.service.BookingService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class CamelRoutes extends RouteBuilder {

    @Autowired
    private BookingService bookingService;



    @Override
    public void configure() throws Exception {

        /*
            uncomment different underlying http components to test them out as desired
            -all support the camel rest DSL
         */
        restConfiguration()
                .component("jetty")
                //.component("netty4-http")
                //.component("restlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .host("127.0.0.1")
                .port(8080)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Ticket Booking API").apiProperty("api.version", "0.1")
                .apiProperty("cors", "true");


    /*
        This camel route provides the HTTP/JSON interface for the booking service
     */
        rest("/ticket")
                .description("operations on tickets ")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .put("/book")
                .type(BookingRequest.class)
                .outType(BookingResult.class)
                .route()
                .bean(bookingService);

    }
}
