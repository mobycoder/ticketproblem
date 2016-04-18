package com.mobycode.ticketproblem.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Ticket implements Serializable {


    public Ticket(Long id) {
        this.id = id;
        this.available = true;
    }

    private final Long id;
    private Boolean available;
    private Long customerReference;

}
