# AWS Java — Interview Revision Sheet

## 1. AWS SDK for Java
- Library added via Maven/Gradle to call AWS services from Java code
- Current version: **SDK v2** — builder patterns, supports async calls
- Spring Cloud AWS wraps this SDK with Spring-friendly beans (`S3Template`, `SqsTemplate`, etc.)

## 2. Core Services
| Service | What it's for | Java dev relevance |
|---|---|---|
| EC2 | Virtual servers | Run your app directly |
| S3 | File/object storage | Upload/download via `S3Template` |
| RDS | Managed relational DB | Drop-in replacement for local Postgres/MySQL — JPA/Hibernate code unchanged |
| Lambda | Run code without a server | Event-driven functions, cold-start trade-offs |
| SQS / SNS | Messaging & notifications | Decoupling microservices, like Kafka |
| DynamoDB | NoSQL database | Different mindset from relational DBs |
| ECS / EKS | Containers | Deploy Spring Boot as Docker containers |
| IAM | Access control | Roles instead of hardcoded credentials |

## 3. Spring Cloud AWS — the bridge
```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-s3</artifactId>
    <version>3.1.1</version>
</dependency>
```
- Per-service starters: `-s3`, `-sqs`, `-secrets-manager`, etc.
- Config via `application.yml`, same pattern as datasources/Kafka
- Production credentials: use an **IAM role** attached to EC2/ECS, not hardcoded keys

## 4. S3 — File Storage
```java
@RestController
@RequestMapping("/files")
public class FileUploadController {
    private final S3Template s3Template;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            s3Template.upload(bucketName, file.getOriginalFilename(), in);
        }
        return "Uploaded: " + file.getOriginalFilename();
    }
}
```
- `S3Template` = auto-configured bean, no manual client setup
- Same shape as `JdbcTemplate` — inject and call

## 5. SQS — Messaging
```java
// Sending
sqsTemplate.send("order-events-queue", orderId);

// Listening
@SqsListener("order-events-queue")
public void handleOrderEvent(String orderId) {
    orderService.markAsProcessed(orderId);
}
```
- `SqsTemplate.send()` ≈ `KafkaTemplate.send()`
- `@SqsListener` ≈ `@KafkaListener`
- Use case: decouple order placement from downstream processing (email, inventory, etc.)

## 6. RDS / JPA
- **No code changes** — just point the connection string at the RDS endpoint
- Password → **Secrets Manager**, not plain config:
  ```yaml
  spring:
    config:
      import: aws-secretsmanager:my-db-secret
  ```
- **IAM DB auth** — short-lived tokens instead of passwords (advanced, know the term)
- **HikariCP** pool sizing matters more against RDS connection limits
- **Multi-AZ** (failover standby) vs **Read Replicas** (scale reads)
- **Flyway/Liquibase** for migrations — `ddl-auto: validate`, not `update`

## 7. Lambda
- Runs only on trigger (API Gateway, S3 event, SQS, EventBridge schedule) — pay per invocation
- **Cold starts are the big Java-specific issue** (1–3s+ on a cold JVM)
  - **Provisioned Concurrency** — keep instances warm (costs more)
  - **GraalVM native image** — ~100ms cold start, added build complexity
  - **SnapStart** — snapshot/restore a warm JVM, modern default fix
- Plain Java handler:
  ```java
  public class OrderHandler implements RequestHandler<OrderRequest, OrderResponse> {
      public OrderResponse handleRequest(OrderRequest input, Context context) {
          return new OrderResponse(input.getOrderId(), "PROCESSED");
      }
  }
  ```
- Spring Cloud Function = thinner Spring runtime for Lambda, still slower to cold-start than plain Java
- **Lambda vs ECS**: Lambda for bursty/event-driven/short tasks; ECS/EC2 for steady traffic, long-running, latency-critical, full Spring Boot

## 8. Testing Strategy (the test pyramid)
| Layer | Tool | Tests |
|---|---|---|
| Unit | Mockito (`@Mock`, `@InjectMocks`) | Business logic in isolation, no AWS calls |
| Integration | **LocalStack** + Testcontainers | Real S3/SQS/Lambda calls against a local fake AWS |
| Integration (DB) | **Testcontainers** (real Postgres/MySQL) | RDS/JPA — LocalStack doesn't fake RDS well |
| End-to-end | Real AWS dev/staging | Run less often, e.g. pre-deploy in CI |

**Unit test example (S3):**
```java
@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {
    @Mock private S3Template s3Template;
    @InjectMocks private FileUploadController controller;

    @Test
    void uploadFile_shouldCallS3Template() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        controller.uploadFile(file);
        verify(s3Template).upload(anyString(), eq("test.pdf"), any());
    }
}
```

**LocalStack integration test setup:**
```java
@Testcontainers
@SpringBootTest
class S3IntegrationTest {
    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.7")).withServices(S3);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.s3.endpoint",
            () -> localstack.getEndpointOverride(S3).toString());
    }
}
```

## 9. Certification Path
Given AZ-900 (Azure) background:
1. **AWS Certified Cloud Practitioner** — foundational, non-technical
2. **AWS Certified Developer – Associate** — the Java/developer-focused one for the CV

## 10. Key Interview Soundbites
- **RDS/JPA**: "My Hibernate code doesn't know it's talking to RDS — the change is entirely config and infrastructure: connection string, Secrets Manager, connection pool tuning."
- **Lambda**: "Lambda fits event-driven, bursty workloads well, but Java's cold-start cost means I'd reach for SnapStart or provisioned concurrency for latency-sensitive paths, or keep that service on ECS as a normal Spring Boot app."
- **Testing**: "I use LocalStack specifically to avoid AWS costs and flakiness in CI, and Testcontainers with a real Postgres image for RDS/JPA integration tests."
- **Deployment**: "Spring Boot containers on ECS/EKS, RDS for the database, S3/SQS for storage and messaging — pretty transferable from my OpenShift/Docker background."

## 11. Bash commands for the running project:
```bash

mkdir -p /home/claude/aws-java-demo/src/main/java/com/example/awsdemo/{controller,service,repository,entity,listener,dto}
mkdir -p /home/claude/aws-java-demo/src/main/resources
mkdir -p /home/claude/aws-java-demo/src/test/java/com/example/awsdemo/{controller,service,listener,integration}
echo "created"

mkdir -p /home/claude/aws-java-demo/src/main/resources/db/migration
```

## Here are all the endpoints currently defined across your two controllers:

### OrderController (/orders)

Method	URL	Body	Notes
POST	http://localhost:8080/orders	{"product": "Widget", "quantity": 3}	Creates an order (RDS/JPA), sends SQS message → returns 201 with orderId + status

### FileUploadController (/files)

Method	URL	Body	Notes
POST	http://localhost:8080/files/upload	form-data, key file (type: File)	Uploads to S3 (LocalStack)
GET	http://localhost:8080/files/download/{key}	—	e.g. http://localhost:8080/files/download/README.md


## Upload URL:

POST http://localhost:8080/files/upload

Where the files come from (before uploading):
Same answer as before — it can be any folder on your computer. There's no required location; you pick the file yourself when sending the request.

### How to set it in Postman:

Method: POST, URL: http://localhost:8080/files/upload
Go to the Body tab → select form-data
Add a key named exactly file
On that row, change the type dropdown from Text to File (it's a small dropdown on the right side of the key field)
Click Select Files → browse to wherever the file actually is (Desktop, Downloads, project folder — doesn't matter) → pick it
Hit Send

You should get back a 200 response body like:

Uploaded: yourfilename.pdf
For the GET /files/download/{key} request:

The {key} in the URL = the exact filename you used when uploading (case-sensitive). So if you uploaded README.md, you'd request:
Check whether file uploaded in terminal
```bash
docker exec -it awsdemo-localstack awslocal s3 ls s3://order-attachments
```

http://localhost:8080/files/download/README.md

Where the downloaded file lands on your computer:
Postman doesn't auto-save it — by default it just shows the raw content in the response panel. To actually save it as a file:

Send the GET request
In the response panel, click the "Save Response" dropdown (next to Send/Save, top right of the response) → "Save to a file"
Postman opens a normal file save dialog — pick any folder you want (Downloads, Desktop, wherever)

If you're testing with curl instead of Postman, it's simpler — you choose the destination directly:

```bash
curl -o ~/Downloads/myfile.pdf http://localhost:8080/files/download/README.md
```
So to be clear: it's retrieved from the S3/LocalStack bucket, and saved to wherever you tell Postman or curl to put it — your app doesn't dictate a fixed download location.
This lists every file you've uploaded through /files/upload. If you want to pull a copy out onto your machine to inspect it directly:

bash
docker exec -it awsdemo-localstack awslocal s3 cp s3://order-attachments/yourfile.pdf /tmp/yourfile.pdf
docker cp awsdemo-localstack:/tmp/yourfile.pdf ~/Downloads/yourfile.pdf

Downloaded file → wherever you told Postman to save it

Your app doesn't choose a location — Postman does, and only if you explicitly save it:

After sending the GET request, click Save Response (top right of the response panel) → Save to a file
Pick a folder (e.g. Downloads) in the dialog that pops up

If you never clicked "Save to a file," it wasn't saved anywhere — it was just displayed in Postman's response panel and is gone once you close/overwrite that tab.

Quick way to confirm the whole round trip in one command (skips Postman GUI):

```bash
curl -F "file=@/path/to/some/file.txt" http://localhost:8080/files/upload
curl -o ~/Downloads/file.txt http://localhost:8080/files/download/file.txt
ls ~/Downloads/file.txt
```