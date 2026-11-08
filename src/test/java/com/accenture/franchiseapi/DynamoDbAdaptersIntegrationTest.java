package com.accenture.franchiseapi;

import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.branch.infrastructure.out.persistence.BranchDynamoDbAdapter;
import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import com.accenture.franchiseapi.franchise.infrastructure.out.persistence.FranchiseDynamoDbAdapter;
import com.accenture.franchiseapi.product.domain.model.Product;
import com.accenture.franchiseapi.product.infrastructure.out.persistence.ProductDynamoDbAdapter;
import com.accenture.franchiseapi.shared.infrastructure.persistence.CatalogItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integración de los adaptadores contra un DynamoDB Local real (Testcontainers).
 * Verifican claves, condiciones de escritura y consultas de la tabla única.
 * Si la máquina no tiene Docker, la clase completa se omite.
 */
class DynamoDbAdaptersIntegrationTest {

    private static GenericContainer<?> dynamoDb;
    private static DynamoDbAsyncClient client;
    private static FranchiseDynamoDbAdapter franchises;
    private static BranchDynamoDbAdapter branches;
    private static ProductDynamoDbAdapter products;

    @BeforeAll
    static void startDynamoDbLocal() throws Exception {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker no disponible: se omiten las pruebas de integración");

        dynamoDb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local:latest"))
                .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
                .withExposedPorts(8000);
        dynamoDb.start();

        client = DynamoDbAsyncClient.builder()
                .endpointOverride(URI.create("http://" + dynamoDb.getHost() + ":" + dynamoDb.getMappedPort(8000)))
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
                .build();

        DynamoDbAsyncTable<CatalogItem> table = DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build()
                .table("franchises", TableSchema.fromBean(CatalogItem.class));
        table.createTable().get();

        franchises = new FranchiseDynamoDbAdapter(table);
        branches = new BranchDynamoDbAdapter(table);
        products = new ProductDynamoDbAdapter(table);
    }

    @AfterAll
    static void stopDynamoDbLocal() {
        if (client != null) {
            client.close();
        }
        if (dynamoDb != null) {
            dynamoDb.stop();
        }
    }

    @Test
    void franchiseCanBeSavedFoundAndRenamed() {
        Franchise franchise = Franchise.create("Franquicia Integración");

        StepVerifier.create(franchises.save(franchise)).expectNext(franchise).verifyComplete();
        StepVerifier.create(franchises.existsById(franchise.id())).expectNext(true).verifyComplete();
        StepVerifier.create(franchises.findById(franchise.id())).expectNext(franchise).verifyComplete();

        Franchise renamed = new Franchise(franchise.id(), "Nuevo nombre");
        StepVerifier.create(franchises.update(renamed)).expectNext(renamed).verifyComplete();
        StepVerifier.create(franchises.findById(franchise.id()))
                .assertNext(found -> assertThat(found.name()).isEqualTo("Nuevo nombre"))
                .verifyComplete();
    }

    @Test
    void unknownFranchiseDoesNotExist() {
        String unknownId = UUID.randomUUID().toString();

        StepVerifier.create(franchises.existsById(unknownId)).expectNext(false).verifyComplete();
        StepVerifier.create(franchises.findById(unknownId)).verifyComplete();
    }

    @Test
    void savingTheSameFranchiseTwiceIsRejected() {
        Franchise franchise = Franchise.create("Duplicada");
        franchises.save(franchise).block();

        StepVerifier.create(franchises.save(franchise))
                .expectErrorMatches(error -> error instanceof ConditionalCheckFailedException
                        || error.getCause() instanceof ConditionalCheckFailedException)
                .verify();
    }

    @Test
    void branchesAreListedPerFranchiseWithoutProducts() {
        Franchise franchise = Franchise.create("Con sucursales");
        franchises.save(franchise).block();
        Branch centro = Branch.create(franchise.id(), "Centro");
        Branch norte = Branch.create(franchise.id(), "Norte");
        branches.save(centro).block();
        branches.save(norte).block();
        products.save(Product.create(franchise.id(), centro.id(), "Café", 5)).block();

        StepVerifier.create(branches.existsById(franchise.id(), centro.id())).expectNext(true).verifyComplete();
        StepVerifier.create(branches.findByFranchiseId(franchise.id()).collectList())
                .assertNext(list -> assertThat(list).containsExactlyInAnyOrder(centro, norte))
                .verifyComplete();
    }

    @Test
    void productLifecycleWorks() {
        Franchise franchise = Franchise.create("Ciclo de producto");
        franchises.save(franchise).block();
        Branch branch = Branch.create(franchise.id(), "Centro");
        branches.save(branch).block();
        Product product = Product.create(franchise.id(), branch.id(), "Café", 10);

        StepVerifier.create(products.save(product)).expectNext(product).verifyComplete();

        Product updated = product.withStock(42);
        StepVerifier.create(products.update(updated)).expectNext(updated).verifyComplete();
        StepVerifier.create(products.findById(franchise.id(), branch.id(), product.id()))
                .assertNext(found -> assertThat(found.stock()).isEqualTo(42))
                .verifyComplete();

        StepVerifier.create(products.deleteById(franchise.id(), branch.id(), product.id()))
                .expectNext(updated)
                .verifyComplete();
        StepVerifier.create(products.findById(franchise.id(), branch.id(), product.id())).verifyComplete();
        StepVerifier.create(products.deleteById(franchise.id(), branch.id(), product.id())).verifyComplete();
    }

    @Test
    void productsAreIsolatedPerFranchise() {
        Franchise first = Franchise.create("Primera");
        Franchise second = Franchise.create("Segunda");
        franchises.save(first).block();
        franchises.save(second).block();
        Branch firstBranch = Branch.create(first.id(), "Sucursal A");
        Branch secondBranch = Branch.create(second.id(), "Sucursal B");
        branches.save(firstBranch).block();
        branches.save(secondBranch).block();
        Product cafe = Product.create(first.id(), firstBranch.id(), "Café", 3);
        Product te = Product.create(first.id(), firstBranch.id(), "Té", 8);
        products.save(cafe).block();
        products.save(te).block();
        products.save(Product.create(second.id(), secondBranch.id(), "Pan", 99)).block();

        StepVerifier.create(products.findByFranchiseId(first.id()).collectList())
                .assertNext(list -> assertThat(list).containsExactlyInAnyOrder(cafe, te))
                .verifyComplete();
    }
}
