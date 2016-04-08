package com.mobycode.ticketproblem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor    //for Jackson serialisation
public class BookingRequest implements Serializable {
    private Long ticketId;
    private Long customerId;
}
