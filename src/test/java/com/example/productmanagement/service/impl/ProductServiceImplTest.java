package com.example.productmanagement.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.persistence.entities.Product;
import com.example.productmanagement.persistence.repositories.ProductRepository;
import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final long ID = 1L;
    private static final BigDecimal PRICE = new BigDecimal("59.90");
    private static final Product PRODUCT = new Product(ID, "Keyboard", PRICE, null);
    private static final ProductResponse RESPONSE = new ProductResponse(ID, "Keyboard", PRICE, null);

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void createsProductAndReturnsGeneratedId() {
        when(repository.save(any(Product.class))).thenReturn(Mono.just(PRODUCT));

        StepVerifier.create(service.create(new CreateProductRequest("Keyboard", PRICE, null)))
                .expectNext(RESPONSE)
                .verifyComplete();

        ArgumentCaptor<Product> product = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(product.capture());
        assertThat(product.getValue().getId()).isNull();
        assertThat(product.getValue().getName()).isEqualTo("Keyboard");
        assertThat(product.getValue().getPrice()).isEqualByComparingTo(PRICE);
    }

    @Test
    void listsProducts() {
        when(repository.findAll()).thenReturn(Flux.just(PRODUCT));

        StepVerifier.create(service.findAll()).expectNext(RESPONSE).verifyComplete();
    }

    @Test
    void findsProductById() {
        when(repository.findById(ID)).thenReturn(Mono.just(PRODUCT));

        StepVerifier.create(service.findById(ID)).expectNext(RESPONSE).verifyComplete();
    }

    @Test
    void updatesExistingProductWithoutChangingItsId() {
        var request = new UpdateProductRequest("Mouse", PRICE, "Wireless");
        when(repository.findById(ID)).thenReturn(Mono.just(PRODUCT));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.update(ID, request))
                .expectNext(new ProductResponse(ID, "Mouse", PRICE, "Wireless"))
                .verifyComplete();
    }

    @Test
    void deletesExistingProduct() {
        when(repository.findById(ID)).thenReturn(Mono.just(PRODUCT));
        when(repository.delete(PRODUCT)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(ID)).verifyComplete();
        verify(repository).delete(PRODUCT);
    }

    @Test
    void rejectsMissingProductWithoutWritingToTheRepository() {
        when(repository.findById(ID)).thenReturn(Mono.empty());
        var update = new UpdateProductRequest("Mouse", PRICE, null);

        StepVerifier.create(service.findById(ID)).verifyError(ProductNotFoundException.class);
        StepVerifier.create(service.update(ID, update)).verifyError(ProductNotFoundException.class);
        StepVerifier.create(service.delete(ID)).verifyError(ProductNotFoundException.class);

        verify(repository, times(3)).findById(ID);
        verifyNoMoreInteractions(repository);
    }
}
