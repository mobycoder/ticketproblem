package com.mobycode.ticketproblem.grid.configuration;

import com.mobycode.ticketproblem.grid.GridDataNames;
import com.mobycode.ticketproblem.model.Customer;
import com.mobycode.ticketproblem.model.Ticket;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*

Configure and start a partitioned data grid + provide grid objects for Spring injection

 */
@Configuration
public class GridConfiguration {

    @Autowired
    Ignite ignite;

    @Bean
    Ignite ignite (){
        CacheConfiguration cacheCfg = new CacheConfiguration(GridDataNames.GRID.getName());
        cacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cacheCfg.setBackups(1);
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setCacheConfiguration(cacheCfg);
        Ignite ignite = Ignition.start(cfg);
        IgniteCache<Long, Ticket> ticketMap = ignite.getOrCreateCache(GridDataNames.MAP_TICKET.getName());
        IgniteCache<Long, Customer> customerMap = ignite.getOrCreateCache(GridDataNames.MAP_CUSTOMER.getName());
        return ignite;
    }

    @Bean
    IgniteCache<Long, Ticket> ticketMap (){
        return ignite.getOrCreateCache(GridDataNames.MAP_TICKET.getName());
    }


    @Bean
    IgniteCache<Long, Customer> customerMap (){
        return ignite.getOrCreateCache(GridDataNames.MAP_CUSTOMER.getName());
    }

}
