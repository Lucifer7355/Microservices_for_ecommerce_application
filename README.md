# High Level Design
![HLD](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/HLD.jpg)
# Microservices_for_ecommerce_application
Ecommerce Application Backend (Focussing on Cart and Product part only) using SpringBoot
It consists of five parts : 
1. product-service
2. cart-service
3. eureka-server (Discovery of microservices)
4. api-gateway-server
5. ActiveMQ Broker (Download and run as per the given instruction in this readme)

# Demonstration : 

Application has the following scenarios :

- Add one or many products to the application’s database
- Get product list
- Add product to cart
- Get cart products
- Delete product from the cart
- Delete all products from the cart
## Adding list of products
![Adding list of products](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(277).png)
## Adding one product
![Adding one product](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(278).png)
## Get request to check all the products present in the products database
![Get request to check all the products present in the products database](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(279).png)
## Sending product id having 1 to Cart microsevice via Active MQ
![Sending product id having 1 to Cart microsevice via Active MQ](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(280).png)
## Get request to get all the products present inside the cart database.
![Get request to get all the products present inside the cart database.](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(281).png)
## Get all the products inside the product datatabase
![Get all the products inside the product datatabase](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(282).png)
## Accessing the same get all products from cart database request using API gateway 
![Accessing the same getting all products from cart database using API gateway request](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(283).png)
## Eureka Server up and running, which registers all the available microservices
![Eureka Server up and running, which registers all the available microservices](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(284).png)
## Active messgae queue up and running to ingest and forward data as required
![Active messgae queue up and running to ingest and forward data as required](https://github.com/Lucifer7355/Microservices_for_ecommerce_application/blob/main/demonstration_images/Screenshot%20(286).png)
# For product-cart microservice :  
Application will have 5 dependencies:
- Lombok
- Spring web
- Spring Data JPA
- Apache ActiveMQ
- H2 Database

Inside the microservice, Created Product Entity model,ProductRepository and ProductController.
The End points exposed in the Controller are:-
```
"/product/addOne" :---> To save one product object.
"/product/addList" :---> To save a list of product objects.
"/product/addgetAll" :---> To get all the available product objects from in memory database.
```
The microservice is running at "8045".


Downloaded ActiveMQ from ---> https://activemq.apache.org/components/classic/download/ as per OS.

Once downloaded, unzip the file. then go to the bin folder and run activemq start .
Two important piece of information is : 
- Activemq is listening on port 61616 for TCP protocol.
- Activemq WebConsole is available at http://127.0.0.1:8161/

To access the web console, we need to authenticate using :

👨 user: admin

🔑 password: admin

Inside the application.properties file of product-service, added the following details to 
connect service with MQ.
```
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
product.jms.destination=product
```

Now sendToCart method is added in ProductController .
Inside this method the route asks for product id which needs to be sent to Cart.
It takes out that product object from database if present,Now JSON object is converted as String and using jmsTemplate it is sent to the MQ.
else if any of the condition is not met,an exception is thrown.

# For cart microservice : 
Application will have 5 dependencies:
- Lombok
- Spring web
- Spring Data JPA
- Apache ActiveMQ
- H2 Database

Inside this service, all the things will be similar to product-service. Model for Product class entity is created and repository as well.
Now JmsConsumer class is created, which will listen to ActiveMQ. Once there is new data, will consume it.
It basically converts back JSON to product object and saves inside the DB.


It Consists of Following End-points:
```
"/cart/getProducts" : ----> returns all the available products in the cart DB.
"/cart/deleteOne/{id}" : ----> deletes the product with id="id" passed in pathvariable. 
"/cart/deleteAll" : ----> deletes all the available products inside the cart DB.
"/cart/info" : ----> it returns the info about cart microservice.
```
Now this service will run on port "8050".

For connecting this microservice with MQ, the following lines are added to application.properties.

```
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
product.jms.destination=product
```


Now the two services can communicate via message queue.

To hide our architecture, a gateway has been used.
```
An API Gateway is the single entry point for defined back-end APIs and microservices. Sitting in front of the APIs, the API Gateway acts as a protector, enhancing security and ensuring scalability and high availability.
```
# Gateway service:
Application will have only one dependency this time :

- Gateway

Its for routing of extenal requests to required microservices.

The following details have been added in applications.yml file to setup routing through API-gateway.

```
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id : product-service
          uri: http://localhost:8045
          predicates:
            - Path=/product/**
        - id: cart-service
          uri: http://localhost:8050
          predicates:
            - Path=/cart/**
```
So here our api-gateway will be listening on 8080 port. Every call to the path /productwill be redirected to the 8045 port and the same thing for /cart to the 8050.
- Now, our microservices can be called without knowing which port number they are working on just by using gateway IP and port number. Rest information about microservices
is known to gateway.

Now, what happens if the cart-service or the product-service changes address or port?

We will have to change the gateway’s configuration too. But as we said before, the µservice should be independent of others.

So there is a solution for this using Spring Cloud Netflix — Eureka
```
Eureka is a client-side service discovery allows services to search and communicate with each other without hard-coding the hostname and port. The only “fixed point” in such an architecture is a service registry that each service must register with.
```
# Eureka Server : 

Our microservice will have only one dependency :
- Eureka Server

Inside the EurekaServerApplication.java we need to enable the Eureka server using @EnableEurekaServer

The Eureka service is running at "9000". Inside the applications.properties file, the following configurations are setup to 
make it active.
```
server.port=9000

#Do not register yourself
eureka.client.fetch-registry=false
#Do not register on another server
eureka.client.register-with-eureka=false
eureka.client.service-url.default-zone = http://localhost:${server.port}/eureka
#The domain we manage
eureka.instance.hostname=localhost
#Refresh rate < 1s
eureka.server.renewal-percent-threshold=0.85
```
For each microservice, we need to enable the Eureka client using the following annotation @EnableEurekaClient.
```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  <version>3.0.1</version>
</dependency>
```
This dependency should also be added to product-service,API-gateway and cart-service's POM.XML file.

Now for each of the services to discover and register themsevles with Eureka client, the following properties has been added to microservices.

For Product-service and Card-service, these modification needs to be made for them to identify Eureka.

```
#App name
eureka.instance.appname=${spring.application.name}
#Register the service to the Eureka server
eureka.client.fetch-registry=true
#In the following server
eureka.client.service-url.defaultZone=http://localhost:9000/eureka
```

The following lines for the API-gateway inside application.yml:
```
eureka:
  instance:
    appname: ${spring.application.name}
  client:
    service-url:
      defaultZone: http://localhost:9000/eureka
```
after all these configurations, start the services in the following order: -

1. Eureka Client Server (Note that when starting services the Eureka Server must be already up else microservices will fail to register and will show error in logs.)
2. API-Gateway
3. Product or Cart microservice, any of them can be started before any one. It causes no issue.

Now inside the API gateway application.properties file just a minute modification has to be done, so that, when addresses and ports change the API-gateway doesn’t have to worry about forwarding requests 
```
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id : product-service
          #uri: http://localhost:8045
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/product/**
        - id: cart-service
          #uri: http://localhost:8050
          uri: lb://CART-SERVICE
          predicates:
            - Path=/cart/**
```

## Now the microservice endpoints are open to use and can be used to request data in the form of JSON from frontend.