package com.example.productmanagement.mapper;

import com.example.productmanagement.persistence.entities.Product;
import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request) {
        return new Product(null, request.name(), request.price(), request.description());
    }

    public static Product toEntity(Long id, UpdateProductRequest request) {
        return new Product(id, request.name(), request.price(), request.description());
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getDescription());
    }
}
