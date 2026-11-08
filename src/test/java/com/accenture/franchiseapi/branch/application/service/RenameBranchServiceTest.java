package com.accenture.franchiseapi.branch.application.service;

import com.accenture.franchiseapi.branch.application.port.in.RenameBranchUseCase.RenameBranchCommand;
import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.shared.domain.exception.DomainValidationException;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
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
class RenameBranchServiceTest {

    @Mock
    private BranchRepositoryPort branchRepository;

    private RenameBranchService service;

    @BeforeEach
    void setUp() {
        service = new RenameBranchService(branchRepository);
    }

    @Test
    void shouldRenameBranch() {
        when(branchRepository.findById("f-1", "b-1")).thenReturn(Mono.just(new Branch("b-1", "f-1", "Vieja")));
        when(branchRepository.update(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.rename(new RenameBranchCommand("f-1", "b-1", "Nueva")))
                .assertNext(branch -> assertThat(branch.name()).isEqualTo("Nueva"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        when(branchRepository.findById("f-1", "b-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(new RenameBranchCommand("f-1", "b-x", "Nueva")))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectBlankNameAndNotUpdate() {
        when(branchRepository.findById("f-1", "b-1")).thenReturn(Mono.just(new Branch("b-1", "f-1", "Vieja")));

        StepVerifier.create(service.rename(new RenameBranchCommand("f-1", "b-1", "")))
                .expectError(DomainValidationException.class)
                .verify();

        verify(branchRepository, never()).update(any());
    }

    @Test
    void shouldKeepIdsWhenRenaming() {
        when(branchRepository.findById("f-1", "b-1")).thenReturn(Mono.just(new Branch("b-1", "f-1", "Vieja")));
        when(branchRepository.update(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.rename(new RenameBranchCommand("f-1", "b-1", "Nueva")))
                .assertNext(branch -> {
                    assertThat(branch.id()).isEqualTo("b-1");
                    assertThat(branch.franchiseId()).isEqualTo("f-1");
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(branchRepository.findById("f-1", "b-1")).thenReturn(Mono.just(new Branch("b-1", "f-1", "Vieja")));
        when(branchRepository.update(any(Branch.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.rename(new RenameBranchCommand("f-1", "b-1", "Nueva")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }
}
