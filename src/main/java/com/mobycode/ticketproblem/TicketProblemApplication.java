package com.mobycode.ticketproblem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class TicketProblemApplication {


    public static void main(String[] args) {

        new SpringApplicationBuilder(TicketProblemApplication.class)
                .web(false)
                .run(args);
    }




}
