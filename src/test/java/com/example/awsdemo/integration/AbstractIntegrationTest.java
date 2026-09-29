package com.example.awsdemo.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

/**
 * Shared base for integration tests. Starts one Postgres and one LocalStack
 * container for the whole test run (singleton-container pattern), creates the
 * S3 bucket and SQS queue, and points the application at them.
 * Requires a running Docker daemon.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    protected static final String TEST_BUCKET = "test-attachments";
    protected static final String TEST_QUEUE = "test-order-events";

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    private static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.7"))
                    .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

    static {
        POSTGRES.start();
        LOCALSTACK.start();
        awslocal("s3", "mb", "s3://" + TEST_BUCKET);
        awslocal("sqs", "create-queue", "--queue-name", TEST_QUEUE);
    }

    private static void awslocal(String... args) {
        String[] command = new String[args.length + 1];
        command[0] = "awslocal";
        System.arraycopy(args, 0, command, 1, args.length);
        try {
            ExecResult result = LOCALSTACK.execInContainer(command);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException(
                        "awslocal " + String.join(" ", args) + " failed: " + result.getStderr());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not run awslocal in LocalStack", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while configuring LocalStack", e);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.cloud.aws.region.static", LOCALSTACK::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", LOCALSTACK::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", LOCALSTACK::getSecretKey);
        registry.add("spring.cloud.aws.s3.endpoint", () -> LOCALSTACK.getEndpoint().toString());
        registry.add("spring.cloud.aws.s3.path-style-access-enabled", () -> "true");
        registry.add("spring.cloud.aws.sqs.endpoint", () -> LOCALSTACK.getEndpoint().toString());

        registry.add("app.s3.bucket", () -> TEST_BUCKET);
        registry.add("app.sqs.order-queue", () -> TEST_QUEUE);
    }
}
