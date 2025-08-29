package com.example.bfh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class FlowService {

    private static final Logger log = LoggerFactory.getLogger(FlowService.class);

    private final WebClient webClient;

    public FlowService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void execute(String name, String regNo, String email, String finalQuery) {
        log.info("Starting flow for {} ({})", name, regNo);

        // 1) Generate webhook + token
        GenerateWebhookRequest req = new GenerateWebhookRequest(name, regNo, email);
        GenerateWebhookResponse resp = webClient.post()
                .uri("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .doOnNext(r -> log.info("Received webhook: {} | accessToken: {}", r.webhook(), mask(r.accessToken())))
                .block();

        if (resp == null) {
            log.error("No response received from generateWebhook endpoint.");
            return;
        }

        String webhookUrl = (resp.webhook() != null && !resp.webhook().isBlank())
                ? resp.webhook()
                : "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        // 2) Submit final SQL query using JWT token in Authorization header
        SubmissionRequest submission = new SubmissionRequest(finalQuery);

        try {
            String result = webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, resp.accessToken()) // Spec says raw token; if 401, try Bearer
                    .bodyValue(submission)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Submission response: {}", result);
            log.info("Flow COMPLETED.");
        } catch (WebClientResponseException.Unauthorized e) {
            log.warn("401 Unauthorized with raw token, retrying with 'Bearer ' prefix...");
            String result = webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resp.accessToken())
                    .bodyValue(submission)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Submission response (after Bearer retry): {}", result);
            log.info("Flow COMPLETED.");
        } catch (WebClientResponseException e) {
            log.error("Submission failed: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Submission failed with exception", e);
        }
    }

    private static String mask(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
