package com.example.productmanagement.service.interfaces;

import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<ProductResponse> create(CreateProductRequest request);

    Flux<ProductResponse> findAll();

    Mono<ProductResponse> findById(Long id);

    Mono<ProductResponse> update(Long id, UpdateProductRequest request);

    Mono<Void> delete(Long id);
}
