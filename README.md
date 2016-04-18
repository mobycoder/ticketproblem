# Ticket problem

## Problem description

60,000 people want to book one ticket at the same time.  There can only be one winner and the system should handle the implied concurrency.

## Approach

 - Create a backend service which allows tickets to be booked via two separate synchronous, transactional, network addressable interfaces.  

 - Generate requests to this service to examine :
     - performance of each interface type 
     - performance of underlying persistence technology

## Limitations of the approach

 - The service represents only part of a real world system (a single end-to-end booking attempt via a web or mobile app interface would take longer).

 - The service was created to be run and tested on a single machine so: 
    - the usual network hops of client-server architectures are absent (improving performance)
    - both client and server are sharing the same resources (degrading performance)

 -  The test doesn't generate 60,000 simultaneous requests - this isn't really possible on a single machine.  
Instead multiple threads submit requests at the same time with a total of 60,000 submissions

 - There is no security on either interface - adding this would be mean more overhead for each request
 
 - A minimal data model is used - more complex processing for a richer model would take more time per request

## Results

YMMV depending on hardware (see notes below) - with some light testing I got these results with a client - server topology (on the same machine) :

- 60,000 attempts to book the same ticket over HTTP/JSON took between 20-30 seconds   
- 60,000 attempts to book the same ticket using a data grid API took between 7-10 seconds


## Technology

### Languages used

Java 8 (Oracle version)

### Servers

- Jetty - to expose a HTTP/JSON service (via Camel)
- Ignite - an In-Memory Data Grid

### API

Two network addressable interfaces: 

 -  A JCache EntryProcessor interface (accessible via a POJO method).  NB a client needs to be connected to the underlying datagrid to use this API.
 -  An HTTP/JSON interface (which - under the covers - uses the EntryProcessor API via a service layer) 
   

### Core frameworks/libraries 

Spring, Ignite and Camel (the "SIC" stack?)

#### Spring

Spring Boot was the very convenient base application building technology.  A number of Spring "starter" modules were included (see the pom.xml).
   
   Spring also internally glued everything together using its core dependency injection component.   Java based @Configuration + @Autowire annotations were used instead of XML.
   
   Spring's StopWatch util was used for timing test runs.

#### Apache Ignite

Apache Ignite is the open source version of the commercial product GridGain.  Ignite falls into the In-Memory Data Grid (IMDG) category .
  
  In this app only Ignite's distributed, transactional data grid component was used (it has many more).  Ignite's native Java API was used to set up the in-memory data store for each test.  Its implementation of the JCache interface EntryProcessor was used to transactionally attempt to book a ticket for each booking request.
  
 Ignite supports pluggable disk based persistence via native means or through implementing JCache's CacheLoader/CacheWriter interfaces. 
 Both write-through (synchronous) and write-behind (asynchronous) configurations are supported for persistence.  This was not done for this app but the assumption is that if write-behind was configured,  performance would only be minimally impacted.  

#### Apache Camel

Apache Camel is Java's swiss army knife of routing, content mediation and integration.  

By including the camel-spring-boot-starter dependency, Spring automatically instantiates a CamelContext object and makes it available for injection.  As an added convenience any Spring managed class that extends RouteBuilder has its configure() method called, adding any/all of its Java DSL routes 

For this app I used Camel's very concise Rest DSL to create and expose the the HTTP/JSON service and auto generate its API doc
  
For the client test I used its Apache Http4 component to create a client for the HTTP/JSON service.  

### Secondary frameworks/libraries

- Lombok - for auto generation of getter/setter/construction/logger creation code
- Logback (for logging)
- Apache Http4 - for the client
- Jetty - for the underlying implementation of the HTTP service
- Swagger - to generate API docs

## Building and running the app/tests
NB  - this example uses multicast for server discovery - some network admins may not like it.  
Another option is to set up an IP cluster group - please follow the Ignite docs if you'd like to do that.

### Installation

Clone the github project using your preferred tool/IDE.  Command line would look like this: 

``$ git clone https://github.com/mobycoder/ticketproblem.git``

   
### Build and run in a Server + Client topology


#### Prerequisites

- Oracle Java 8 installed
- Maven 3.04 installed
- Git command line installed (if cloning from the command line)

#### Overview 

This will run the Spring boot application (TicketProblemApplication) as a server in one JVM,  then two tests in a separate JVM as a client

####Steps
1 - Run the maven command to build and run the Spring boot app

``$ mvn clean spring-boot:run``

2 - Activate and run the Client test program

1. Go into the test class RemoteClientTest.java
2. Remove or comment out the class annotation  "@Ignore" to activate the test
3. Run RemoteClientTest.java as a JUnit test in your IDE or on the command line with:

`` mvn -Dtest=RemoteClientTest test``

Both tests use a Completion Service (ExecutorCompletionService) to submit booking requests in multiple threads.  This also serves as a queue to pull results off as they become available 

   Output from the Entry processor based test should look like this:

```
2016-04-18 00:58:11 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Start clearing grid
2016-04-18 00:58:11 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Finished clearing grid
2016-04-18 00:58:11 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Start filling grid
2016-04-18 00:58:19 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Finished filling grid
2016-04-18 00:58:26 [main] INFO  c.m.ticketproblem.RemoteClientTest - StopWatch 'How long for 60000 customers to attempt to book a ticket': running time (millis) = 6769
-----------------------------------------
ms     %     Task name
-----------------------------------------
06769  100%  all customers attempt to book one ticket in parallel using an entry procesor

2016-04-18 00:58:26 [main] INFO  c.m.t.model.ResultRecorder - Result for Ticket booked was 1
2016-04-18 00:58:26 [main] INFO  c.m.t.model.ResultRecorder - Result for Ticket not available was 59999
2016-04-18 00:58:26 [main] INFO  c.m.t.model.ResultRecorder - Result for Error while booking was 0
```

Output from the HTTP/JSON  based test should look like this:

```
2016-04-18 00:58:26 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Finished clearing grid
2016-04-18 00:58:26 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Start filling grid
2016-04-18 00:58:31 [main] DEBUG c.m.ticketproblem.RemoteClientTest - Finished filling grid
2016-04-18 00:58:53 [main] INFO  c.m.ticketproblem.RemoteClientTest - StopWatch 'How long for 60000 customers to attempt to book a ticket': running time (millis) = 21659
-----------------------------------------
ms     %     Task name
-----------------------------------------
21659  100%  all customers attempt to book one ticket in parallel using a HTTP/JSON service

2016-04-18 00:58:53 [main] INFO  c.m.t.model.ResultRecorder - Result for Ticket booked was 1
2016-04-18 00:58:53 [main] INFO  c.m.t.model.ResultRecorder - Result for Ticket not available was 59999
2016-04-18 00:58:53 [main] INFO  c.m.t.model.ResultRecorder - Result for Error while booking was 0
```

### Build and run the app embedded tests in a single JVM

#### Prerequisites

- Oracle Java 8 installed
- Maven 3.04 installed
- Git command line installed (if cloning from the command line)

#### Overview 

Doing this will build, run the app and then tests in the same JVM.  There are two tests and both use the EventProcessor API .  

Being both in-memory and in the same JVM - these tests run very quickly (eg this generally took 1-2 seconds to attempt to book a ticket for all 60,000 customers - both serially and with multiple threads submitting requests)

NB - the above Client-Server tests are likely to be more useful for you - unless you are planning on co-locating your app and IMDG server instance. 

Be aware the Client-Server topology was used for the Results section.

####Steps
1 - Run maven command to build the app and run the internal tests:

``$ mvn clean install``

There are two tests (in the class BookingServiceTest). 

The first tests submits booking requests to the grid for processing serially.  

The other uses a Completion Service (ExecutorCompletionService) to submit booking requests in multiple threads 

   Output from the first test should look like this:
   
```   
2016-04-10 17:07:43 [main] INFO  c.m.t.service.BookingServiceTest - StopWatch 'How long for all customers to attempt to book a ticket': running time (millis) = 1466
-----------------------------------------
ms     %     Task name
-----------------------------------------
01466  100%  all customers (60000) attempt to book one ticket serially
```
   
and output from the second test should look like this:
   
```
2016-04-10 15:57:12 [main] INFO  c.m.t.service.BookingServiceTest - StopWatch 'How long for all customers to attempt to book a ticket': running time (millis) = 1434
-----------------------------------------
ms     %     Task name
-----------------------------------------
01434  100%  all customers attempt to book one ticket in parallel
```


## Notes

### Creating a HTTP/JSON endpoint and client with Camel

#### Endpoint creation

Creating and exposing HTTP/JSON endpoints is very straight forward with the Camel Rest DSL - a fluent Java API 

The server level configuration is done thusly:

```
    restConfiguration()
                .component("jetty")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .host("127.0.0.1")
                .port(8080)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "").apiProperty("api.version", "0.1")
                .apiProperty("cors", "true");

```

(this is in com.mobycode.ticketproblem.camel.CamelRoutes)

There are a few optional things in here:

- JSON pretty printing to make debugging and log reading easier i.e.

```
                .dataFormatProperty("prettyPrint", "true")

```

- auto-generation of swagger documentation:


```
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Ticket Booking API").apiProperty("api.version", "0.1")
```

- and finally allowing cross origin requests with:


```
                .apiProperty("cors", "true");
```

So the most minimal/streamlined, working, configuration would be 

```
       restConfiguration()
                .component("jetty")
                .bindingMode(RestBindingMode.json)
                .host("127.0.0.1")
                .port(8080) ;
```

Line by line this does the following:

```
       restConfiguration()
```
- initiates the configuration of rest endpoint with Camel's fluent DSL

```
                .component("jetty")
```

- configures the Jetty servlet container as the engine behind the rest service.  This is one of a few options (see below)

```
                .bindingMode(RestBindingMode.json)
```

- object serialisation/deserialisation format set to JSON


```
                .host("127.0.0.1")
                .port(8080) ;
```

- host and port configuration

Next the service context path and specific operation configuration - in this case a PUT operation - is set up like this:

```
rest("/ticket")
                .description("operations on tickets")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .put("/book")
                .type(BookingRequest.class)
                .route()
                .bean(bookingService);
```

Lets break this down starting with the service context and description 


```
rest("/ticket")
                .description("operations on tickets")
```

- sets up "/ticket" as the URL path prefix for this service and its operations.  The description is used by the auto generated swagger documentation.

```
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
```

- configures all operations under "/ticket" path to both consume and produce JSON.  Both of these methods take a String - here I've used a constant from the org.springframework.http.MediaType class as the value.

The main alternative is XML so you could, for example, consume JSON and produce XML (or the other way round).  

```
                .put("/book")
```

- creates our first operation which takes an HTTP PUT under the sub path "/book"  so the full URL path to this operation would be: http://localhost:8080/ticket/book"

```
                .type(BookingRequest.class)
                .outType(BookingResult.class)
```

- specifies the Java type expected by the operation in this case the BookingRequest object, while the output type is BookingResult

```
                .route()
```

Starting from the rest("/ticket") call,  we are creating a Camel "Route".  

This is kind of like a method in the Camel world. The big difference from regular Java methods is that routes specify their own protocol (ie HTTP, SSH, FTP etc) and technology to implement the protocol.
 
At this point we are in the middle of creating a rest endpoint route so we use the route() method call to break out into a nested route where we can use things like the bean() method. 

```
                .bean(bookingService);
```

At the top of the relevant class (com.mobycode.ticketproblem.RemoteClientTest) we used Spring injection to pull in the BookingService so now we can just refer to it.

Camel matches the input object for the endpoint (BookingRequest) with the signature of a method in the bookingService bean to invoke it and return the response.

As we have already configured JSON as the output format, conversion of the output type (BookingResult) is done automatically


##### Client creation

This is done in the client test class and takes the form of another Camel route.  It looks like this:

```
from("direct:bookTicketOverHttp")
                        .marshal()
                        .json(JsonLibrary.Jackson, BookingRequest.class)
                        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                        .setHeader("CamelHttpMethod", constant("PUT"))
                        .to("http4://localhost:8080/ticket/book")
                        .unmarshal()
                        .json(JsonLibrary.Jackson, BookingResult.class);
```

(from com.mobycode.ticketproblem.RemoteClientTest)

Step by step we start with 

```
from("direct:bookTicketOverHttp")
```

Use of the "direct" component tells Camel to create a route exposed only to other Camel routes inside this camel context (i.e. not a network addressable interface).  

The part after the colon ("bookTicketOverHttp") is the internal ID for this endpoint.

```
                        .marshal()
                        .json(JsonLibrary.Jackson, BookingRequest.class)
```

Here we configure this route to expect the BookingRequest object as input to this route and to marshal the object to the JSON format using the Jackson library 

```
                        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
```

as it suggests this sets a header in Camel's underlying message object (Exchange).  

As the final destination in the route is the http4 component,  Camel knows to translate this into the HTTP content type header.

```
   .setHeader("CamelHttpMethod", constant("PUT"))
```

- we set the HTTP method to be used to call the remote service - an HTTP PUT in this case
  
```
                        .to("http4://localhost:8080/ticket/book")
```  

http4 refers to the Apache HttpClient (v4) which Camel can use as the component to call HTTP services.  The rest of the URI is the address of the booking service 

```
                        .unmarshal()
                        .json(JsonLibrary.Jackson, BookingResult.class);
```

This configures Camel to expect a JSON response from the URL call and to configure Jackson to unmarshall the JSON to the BookingResult type
                       
Use of this "client" is via a Camel ProducerTemplate.  This gets a little confusing.  

In Camel "Consumers" are the endpoints (i.e. they consume messages from other systems) and Camel "Producers" are the clients/consumers (i.e. they produce messsages to be sent to endpoints)

ProducerTemplate are created from the core Camel object the CamelContext.
                        
```
                       camelContext = new DefaultCamelContext();
                               camelContext.addRoutes(createRoutes());
                               camelContext.start();
                               producerTemplate = camelContext.createProducerTemplate();
```
                               
Here we create a new CamelContext (in the Spring app this is done automagically by Spring Boot), add the client route to it (with createRoutes()), start Camel (with start()),  then create a ProducerTemplate using the started CamelContext object.
                               
In the test method we use the ProducerTemplate like this:

```
   producerTemplate.requestBody("direct:bookTicketOverHttp", new BookingRequest(ticketId, entry), BookingResult.class)
```

There are many different ways of calling Camel with the ProducerTemplate - in our case we pass in the route we want to call ("direct:bookTicketOverHttp"), the input to the route (a BookingRequest object) and the expected result type (a BookingResult object).
 
                               


### Changing the underlying HTTP implementation for the HTTP/JSON interface

Swapping out Jetty for a different underlying HTTP implementation is as easy as including a different HTTP component in Camel's Rest DSL. 

In the class that creates the Camel route providing the HTTP/JSON  endpoint you'll notice a couple of commented lines:

```
restConfiguration()
                .component("jetty")
                //.component("netty4-http")
                //.component("restlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .host("127.0.0.1")
                .port(8080)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "").apiProperty("api.version", "0.1")
                .apiProperty("cors", "true");

```

As this suggests if you comment the line pointing to the Jetty component and uncomment one of the commented lines you'll get that implementation.  

For example to try the netty4 implementation it would look like this:

```
restConfiguration()
                   //.component("jetty")
                   .component("netty4-http")
                   //.component("restlet")
                   .bindingMode(RestBindingMode.json)
                   .dataFormatProperty("prettyPrint", "true")
                   .host("127.0.0.1")
                   .port(8080)
                   .apiContextPath("/api-doc")
                   .apiProperty("api.title", "").apiProperty("api.version", "0.1")
                   .apiProperty("cors", "true");
```

This is particularly useful if you are interested in finding out the performance characteristics of each implementation.  


### Auto generated Swagger docs

If you just run the app with: 

``$ mvn clean spring-boot:run``

you'll find the api docs here: 

http://localhost:8080/api-doc

It should look like this:

```
{
  "swagger" : "2.0",
  "info" : {
    "version" : "0.1",
    "title" : "Ticket Booking API"
  },
  "host" : "127.0.0.1:8080",
  "tags" : [ {
    "name" : "ticket",
    "description" : "operations on tickets "
  } ],
  "schemes" : [ "http" ],
  "paths" : {
    "/ticket/book" : {
      "put" : {
        "tags" : [ "ticket" ],
        "consumes" : [ "application/json" ],
        "produces" : [ "application/json" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "body",
          "description" : "",
          "required" : true
        } ],
        "responses" : {
          "200" : {
            "description" : "Output type",
            "schema" : {
              "$ref" : "#/definitions/BookingResult"
            }
          }
        },
        "x-camelContextId" : "camel-1",
        "x-routeId" : "route1"
      }
    }
  },
  "definitions" : {
    "BookingRequest" : {
      "type" : "object",
      "properties" : {
        "ticketId" : {
          "type" : "integer",
          "format" : "int64"
        },
        "customerId" : {
          "type" : "integer",
          "format" : "int64"
        }
      }
    },
    "BookingResult" : {
      "type" : "object",
      "properties" : {
        "bookingRequest" : {
          "$ref" : "#/definitions/BookingRequest"
        },
        "bookTicketResult" : {
          "type" : "string",
          "enum" : [ "TICKET_BOOKED", "TICKET_NOT_AVAILABLE", "BOOKING_ERROR" ]
        }
      },
      "x-className" : {
        "type" : "string",
        "format" : "com.mobycode.ticketproblem.model.BookingResult"
      }
    }
  }
}
```

### Dev/Test system specs

This code was run/tested on a pretty old machine (relative to when the code was written i.e. April 2016). 

Specs were:

  - CPU: Intel Core I7-2620m (64bit dual core)
  - RAM: 8GB
 
### A few possible development TODOs

  - Plug in a disk based data store to allow system state to survive crashes and restarts
  - Create a simple web UI to flesh out the system then (combined with a disk based persistence store) use Selenium to test end-to-end
  - Streamline the number of dependencies pulled down via maven using exclusions and/or use of specific modules (vs Spring Boot starter packs)
  - Create camelcontext wide exception handler so HTTP interface consumers get something polite and useful back on failure
   
### Improving performance    

Most likely performance will be improved on three fronts:
 1. Configuring, tweaking Ignite with different options/parameters and retesting
 2. Making use of Ignite's distributed compute capabilities (e.g. if you had six server nodes each could process 10,000 simultaneously vs one node processing all 60,000)
 2. Using better hardware and testing with different client/server topologies and ratios of clients to servers (e.g. how many server nodes do you need to service 20 client nodes)

### README TODOs

- Add links