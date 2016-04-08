package com.mobycode.ticketproblem.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Customer implements Serializable {


    public Customer(Long id) {
        this.id = id;
    }

    private Long id;
    private String displayName;
}
