package com.accenture.franchiseapi.franchise.application.service;

import com.accenture.franchiseapi.franchise.application.port.in.CreateFranchiseUseCase.CreateFranchiseCommand;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import com.accenture.franchiseapi.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    private CreateFranchiseService service;

    @BeforeEach
    void setUp() {
        service = new CreateFranchiseService(franchiseRepository);
    }

    @Test
    void shouldCreateAndSaveFranchise() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.create(new CreateFranchiseCommand("Franquicia Norte")))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isNotBlank();
                    assertThat(franchise.name()).isEqualTo("Franquicia Norte");
                })
                .verifyComplete();

        verify(franchiseRepository).save(any(Franchise.class));
    }

    @Test
    void shouldFailAndNotSaveWhenNameIsBlank() {
        StepVerifier.create(service.create(new CreateFranchiseCommand(" ")))
                .expectError(DomainValidationException.class)
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.create(new CreateFranchiseCommand("Franquicia Norte")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }
}
