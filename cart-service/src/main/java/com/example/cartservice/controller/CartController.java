package com.example.cartservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cartservice.model.Product;
import com.example.cartservice.repository.ProductRepository;

import java.util.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    ProductRepository productRepository;

    @GetMapping("/getProducts")
    List<Product> getCartProduct() {
        return productRepository.findAll();
    }

    @GetMapping("/deleteOne/{id}")
    void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }

    @GetMapping("/deleteAll")
    void deleteProducts() {
        productRepository.deleteAll();
    }

    @GetMapping("/info")
    String getInfo() {
        return "cart microservice";
    }
}