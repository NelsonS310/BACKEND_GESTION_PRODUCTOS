package com.example.productmanagement.persistence.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@AllArgsConstructor
@Table("products")
public class Product {

    @Id
    private Long id;

    private final String name;
    private final BigDecimal price;
    private final String description;
}
