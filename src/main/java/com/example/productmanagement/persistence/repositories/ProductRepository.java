package com.example.productmanagement.persistence.repositories;

import com.example.productmanagement.persistence.entities.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
