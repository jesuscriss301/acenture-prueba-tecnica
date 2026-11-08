package com.acenture.prueba_tecnica.branch.application.service;

import com.acenture.prueba_tecnica.branch.application.port.in.RenameBranchUseCase.RenameBranchCommand;
import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
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
}
