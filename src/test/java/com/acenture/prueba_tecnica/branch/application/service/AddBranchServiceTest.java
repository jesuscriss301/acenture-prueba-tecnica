package com.acenture.prueba_tecnica.branch.application.service;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase.AddBranchCommand;
import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddBranchServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;
    @Mock
    private BranchRepositoryPort branchRepository;

    private AddBranchService service;

    @BeforeEach
    void setUp() {
        service = new AddBranchService(franchiseRepository, branchRepository);
    }

    @Test
    void shouldAddBranchWhenFranchiseExists() {
        when(franchiseRepository.existsById("f-1")).thenReturn(Mono.just(true));
        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.add(new AddBranchCommand("f-1", " Sucursal Centro ")))
                .assertNext(branch -> {
                    assertThat(branch.id()).isNotBlank();
                    assertThat(branch.franchiseId()).isEqualTo("f-1");
                    assertThat(branch.name()).isEqualTo("Sucursal Centro");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseRepository.existsById("f-x")).thenReturn(Mono.just(false));

        StepVerifier.create(service.add(new AddBranchCommand("f-x", "Sucursal Centro")))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithoutQueryingWhenNameIsBlank() {
        StepVerifier.create(service.add(new AddBranchCommand("f-1", "")))
                .expectError(DomainValidationException.class)
                .verify();

        verify(franchiseRepository, never()).existsById(anyString());
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(franchiseRepository.existsById("f-1")).thenReturn(Mono.just(true));
        when(branchRepository.save(any(Branch.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.add(new AddBranchCommand("f-1", "Sucursal Centro")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }

    @Test
    void shouldRejectNameLongerThanMax() {
        StepVerifier.create(service.add(new AddBranchCommand("f-1", "a".repeat(101))))
                .expectError(DomainValidationException.class)
                .verify();

        verify(branchRepository, never()).save(any());
    }
}
