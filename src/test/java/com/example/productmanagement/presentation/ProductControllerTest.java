package com.example.productmanagement.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;
import com.example.productmanagement.service.interfaces.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(ProductController.class)
class ProductControllerTest {

    private static final String PRODUCTS = "/api/products";
    private static final BigDecimal PRICE = new BigDecimal("24.99");
    private static final ProductResponse PRODUCT = new ProductResponse(7L, "Mouse", PRICE, null);

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private ProductService service;

    @Test
    void createsProductAndReturnsItsLocation() {
        var request = new CreateProductRequest("Mouse", PRICE, null);
        when(service.create(request)).thenReturn(Mono.just(PRODUCT));

        client.post().uri(PRODUCTS).bodyValue(request).exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", PRODUCTS + "/7")
                .expectBody(ProductResponse.class).isEqualTo(PRODUCT);
    }

    @Test
    void listsProducts() {
        when(service.findAll()).thenReturn(Flux.just(PRODUCT));

        client.get().uri(PRODUCTS).exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class).contains(PRODUCT);
    }

    @Test
    void findsProductById() {
        when(service.findById(7L)).thenReturn(Mono.just(PRODUCT));

        client.get().uri(PRODUCTS + "/7").exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class).isEqualTo(PRODUCT);
    }

    @Test
    void updatesProduct() {
        when(service.update(any(Long.class), any(UpdateProductRequest.class))).thenReturn(Mono.just(PRODUCT));

        client.put().uri(PRODUCTS + "/7").bodyValue(new UpdateProductRequest("Mouse", PRICE, null)).exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class).isEqualTo(PRODUCT);
    }

    @Test
    void deletesProductWithoutReturningABody() {
        when(service.delete(7L)).thenReturn(Mono.empty());

        client.delete().uri(PRODUCTS + "/7").exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void returnsNotFoundAsProblemDetail() {
        when(service.findById(7L)).thenReturn(Mono.error(new ProductNotFoundException(7L)));

        client.get().uri(PRODUCTS + "/7").exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.detail").isEqualTo("Product with id 7 was not found.");
    }

    @Test
    void rejectsInvalidProductFields() {
        client.post().uri(PRODUCTS)
                .bodyValue(new CreateProductRequest(" ", BigDecimal.ZERO, null))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.errors.length()").isEqualTo(2);

        verifyNoInteractions(service);
    }

    @Test
    void rejectsMalformedJsonAsProblemDetail() {
        client.post().uri(PRODUCTS).contentType(MediaType.APPLICATION_JSON).bodyValue("{")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.status").isEqualTo(400);

        verifyNoInteractions(service);
    }
}
