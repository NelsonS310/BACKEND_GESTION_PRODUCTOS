package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.mapper.ProductMapper;
import com.example.productmanagement.persistence.entities.Product;
import com.example.productmanagement.persistence.repositories.ProductRepository;
import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;
import com.example.productmanagement.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public Mono<ProductResponse> create(CreateProductRequest request) {
        return repository.save(ProductMapper.toEntity(request)).map(ProductMapper::toResponse);
    }

    @Override
    public Flux<ProductResponse> findAll() {
        return repository.findAll().map(ProductMapper::toResponse);
    }

    @Override
    public Mono<ProductResponse> findById(Long id) {
        return requireProduct(id).map(ProductMapper::toResponse);
    }

    @Override
    public Mono<ProductResponse> update(Long id, UpdateProductRequest request) {
        return requireProduct(id)
                .flatMap(product -> repository.save(ProductMapper.toEntity(product.getId(), request)))
                .map(ProductMapper::toResponse);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return requireProduct(id).flatMap(repository::delete);
    }

    private Mono<Product> requireProduct(Long id) {
        return repository.findById(id).switchIfEmpty(Mono.error(() -> new ProductNotFoundException(id)));
    }
}
