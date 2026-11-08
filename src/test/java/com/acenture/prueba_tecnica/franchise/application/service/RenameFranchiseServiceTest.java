package com.acenture.prueba_tecnica.franchise.application.service;

import com.acenture.prueba_tecnica.franchise.application.port.in.RenameFranchiseUseCase.RenameFranchiseCommand;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
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
class RenameFranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    private RenameFranchiseService service;

    @BeforeEach
    void setUp() {
        service = new RenameFranchiseService(franchiseRepository);
    }

    @Test
    void shouldRenameFranchise() {
        when(franchiseRepository.findById("f-1")).thenReturn(Mono.just(new Franchise("f-1", "Vieja")));
        when(franchiseRepository.update(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.rename(new RenameFranchiseCommand("f-1", "Nueva")))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isEqualTo("f-1");
                    assertThat(franchise.name()).isEqualTo("Nueva");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("f-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(new RenameFranchiseCommand("f-x", "Nueva")))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectBlankNameAndNotUpdate() {
        when(franchiseRepository.findById("f-1")).thenReturn(Mono.just(new Franchise("f-1", "Vieja")));

        StepVerifier.create(service.rename(new RenameFranchiseCommand("f-1", " ")))
                .expectError(DomainValidationException.class)
                .verify();

        verify(franchiseRepository, never()).update(any());
    }

    @Test
    void shouldTrimNewName() {
        when(franchiseRepository.findById("f-1")).thenReturn(Mono.just(new Franchise("f-1", "Vieja")));
        when(franchiseRepository.update(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.rename(new RenameFranchiseCommand("f-1", "  Nueva  ")))
                .assertNext(franchise -> assertThat(franchise.name()).isEqualTo("Nueva"))
                .verifyComplete();
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(franchiseRepository.findById("f-1")).thenReturn(Mono.just(new Franchise("f-1", "Vieja")));
        when(franchiseRepository.update(any(Franchise.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.rename(new RenameFranchiseCommand("f-1", "Nueva")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }
}
