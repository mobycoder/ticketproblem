package com.mobycode.ticketproblem.grid;


public enum GridDataNames {

    GRID("ticketBookingDataGrid"),
    MAP_TICKET("ticketMap"),
    MAP_CUSTOMER("customerMap");

    private final String name;

    GridDataNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
