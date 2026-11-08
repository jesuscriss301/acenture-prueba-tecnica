package com.acenture.prueba_tecnica.product.domain.model;

import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void createShouldBuildValidProduct() {
        Product product = Product.create("f-1", "b-1", " Café ", 10);

        assertThat(product.id()).isNotBlank();
        assertThat(product.name()).isEqualTo("Café");
        assertThat(product.stock()).isEqualTo(10);
    }

    @Test
    void createShouldRejectNegativeStock() {
        assertThatThrownBy(() -> Product.create("f-1", "b-1", "Café", -1))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    @Test
    void createShouldRejectMissingStock() {
        assertThatThrownBy(() -> Product.create("f-1", "b-1", "Café", null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("El stock es obligatorio");
    }
}
