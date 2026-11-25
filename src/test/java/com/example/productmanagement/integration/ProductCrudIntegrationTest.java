package com.example.productmanagement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.example.productmanagement.presentation.request.CreateProductRequest;
import com.example.productmanagement.presentation.request.UpdateProductRequest;
import com.example.productmanagement.presentation.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductCrudIntegrationTest {

    private static final String PRODUCTS = "/api/products";

    @Autowired
    private WebTestClient client;

    @Test
    void returnsFieldErrorsWithProblemDetailsEnabled() {
        client.post().uri(PRODUCTS)
                .bodyValue(new CreateProductRequest("Monitor", BigDecimal.ZERO, null))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.errors[0].field").isEqualTo("price");
    }

    @Test
    void loadsSeedDataAndCompletesACrudCycle() {
        client.get().uri(PRODUCTS).exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .value(products -> assertThat(products).extracting(ProductResponse::name)
                        .contains("Mechanical Keyboard", "Wireless Mouse", "USB-C Hub"));

        var newProduct = new CreateProductRequest("Monitor", new BigDecimal("179.90"), "24-inch monitor");
        ProductResponse created = client.post().uri(PRODUCTS).bodyValue(newProduct).exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class).returnResult().getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isPositive();
        String uri = PRODUCTS + "/" + created.id();
        client.get().uri(uri).exchange().expectStatus().isOk()
                .expectBody(ProductResponse.class).isEqualTo(created);

        var update = new UpdateProductRequest("Updated Monitor", new BigDecimal("159.90"), null);
        client.put().uri(uri).bodyValue(update).exchange().expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .isEqualTo(new ProductResponse(created.id(), update.name(), update.price(), null));

        client.get().uri(uri).exchange().expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .isEqualTo(new ProductResponse(created.id(), update.name(), update.price(), null));

        client.delete().uri(uri).exchange().expectStatus().isNoContent();
        client.get().uri(uri).exchange().expectStatus().isNotFound();
        client.delete().uri(uri).exchange().expectStatus().isNotFound();
    }
}
