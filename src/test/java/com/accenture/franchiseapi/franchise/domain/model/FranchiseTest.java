package com.accenture.franchiseapi.franchise.domain.model;

import com.accenture.franchiseapi.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranchiseTest {

    @Test
    void createShouldGenerateIdAndTrimName() {
        Franchise franchise = Franchise.create("  Franquicia Norte  ");

        assertThat(franchise.id()).isNotBlank();
        assertThat(franchise.name()).isEqualTo("Franquicia Norte");
    }

    @Test
    void createShouldGenerateDifferentIds() {
        assertThat(Franchise.create("A").id()).isNotEqualTo(Franchise.create("A").id());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createShouldRejectBlankName(String name) {
        assertThatThrownBy(() -> Franchise.create(name))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("El nombre de la franquicia es obligatorio");
    }

    @Test
    void createShouldRejectNameLongerThanMax() {
        String longName = "a".repeat(Franchise.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> Franchise.create(longName))
                .isInstanceOf(DomainValidationException.class);
    }
}
