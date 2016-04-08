package com.mobycode.ticketproblem.grid;

/**
 * User: osupit
 * Date: 4/04/2016
 * Time: 7:23 AM
 */
public enum GridDataNames {

    GRID("ticketBookingDataGrid"),
    MAP_TICKET("ticketMap"),
    MAP_CUSTOMER("customerMap");

    private String name;

    GridDataNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
