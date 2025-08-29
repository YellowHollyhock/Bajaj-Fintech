# BFH JAVA Qualifier – Auto Flow (Spring Boot)

This project automates the qualifier flow:

1. On startup, it **POSTs** to generate a webhook + token.
2. You solve your assigned SQL question and put the **final SQL** in `application.yml`.
3. It **submits** the final SQL query to the webhook using the JWT token.

> No REST endpoints to click—everything runs automatically on app startup.

---

## Prerequisites

- **Java 17** installed (`java -version` should show 17+)
- **Maven** installed (`mvn -v`)

## How to run

1. Open `src/main/resources/application.yml` and fill:
   ```yaml
   bfh:
     name: "Your Name"
     regNo: "REG12347"
     email: "your_email@example.com"
     finalQuery: "YOUR_SQL_QUERY_HERE"
   ```

2. Solve your SQL based on **last two digits** of regNo:
   - **Odd → Question 1** (Google Drive link in the PDF)
   - **Even → Question 2** (Google Drive link in the PDF)

3. Run the app:
   ```bash
   mvn spring-boot:run
   ```

4. Watch logs. It will:
   - Call `.../hiring/generateWebhook/JAVA` to get `webhook` and `accessToken`.
   - Submit your `{ "finalQuery": "..." }` to the webhook with `Authorization: <accessToken>`.

> If you get 401 Unauthorized, the code automatically retries with `Authorization: Bearer <accessToken>`.

## Build a JAR (for submission)

```bash
mvn clean package
```
The jar will be in `target/bfh-java-app-0.0.1-SNAPSHOT.jar`.

You can run it with:
```bash
java -jar target/bfh-java-app-0.0.1-SNAPSHOT.jar
```

## Where to edit

- **Your details + SQL** → `src/main/resources/application.yml`
- Code entry point → `src/main/java/com/example/bfh/BfhJavaApp.java`
- Flow logic → `src/main/java/com/example/bfh/FlowService.java`

## Notes

- The PDF specifies that you must use **WebClient or RestTemplate**, no controllers, and put the **JWT in Authorization header** for the second API. This project uses **WebClient** and runs the flow on startup.
- If your response JSON from `generateWebhook` doesn't include a `webhook`, the code falls back to the fixed submit URL in the PDF.
