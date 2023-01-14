package com.example.productservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    
  //Inject the jmsTemplate
    @Autowired
    private JmsTemplate jmsTemplate;

    // Set the value from the properties file
    @Value("${product.jms.destination}")
    private String jmsQueue;

  //Send a product to the message queue
    @GetMapping("/sendToCart/{id}")
    public ResponseEntity<Product> sendToCart(@PathVariable long id) {
        Optional<Product> product = productRepository.findById(id);
        if(!product.isPresent()) {
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            
            ObjectMapper mapper = new ObjectMapper();    
            //Convert the object to String
            String jsonInString = mapper.writeValueAsString(product.get());
            //Send the data to the message queue
            jmsTemplate.convertAndSend(jmsQueue,jsonInString);
            return  new ResponseEntity<>(product.get(), HttpStatus.OK);
            
        }catch (JsonProcessingException e){
            e.printStackTrace();
            return  new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/addOne")
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PostMapping("/addList")
    public List<Product> addProductList (@RequestBody  List<Product> products) {
        return productRepository.saveAll(products);
    }

    @GetMapping("/getAll")
    public List<Product> getAllProduct () {
        return productRepository.findAll();
    }
}