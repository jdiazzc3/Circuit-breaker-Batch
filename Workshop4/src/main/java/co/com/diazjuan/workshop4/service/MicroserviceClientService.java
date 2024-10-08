package co.com.diazjuan.workshop4.service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class MicroserviceClientService {

    private final WebClient webClient1;
    private final WebClient webClient2;
    private final WebClient webClient3;

    private final WebClient webhookClient;
    private final ObjectMapper objectMapper;

    public MicroserviceClientService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient1 = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.webClient2 = webClientBuilder.baseUrl("http://localhost:8082").build();
        this.webClient3 = webClientBuilder.baseUrl("http://localhost:8083").build();
        this.webhookClient = webClientBuilder.baseUrl("http://localhost:8090").build();
        this.objectMapper = objectMapper;
    }

    public Mono<String> invokeMicroservices() {
        String requestBody = "{\n" +
                "    \"data\":[\n" +
                "        {\n" +
                "            \"header\":{\n" +
                "                \"id\":\"12345\",\n" +
                "                \"type\":\"TestGiraffeRefrigerator\"\n" +
                "            },\n" +
                "            \"enigma\":\"How to put a giraffe into a refrigerator?\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Mono<String> response1 = callMicroservice1(requestBody);
        Mono<String> response2 = callMicroservice2(requestBody);
        Mono<String> response3 = callMicroservice3(requestBody);

        return Mono.zip(response1, response2, response3)
                .map(tuple -> {
                    String body1 = extractAnswerFromResponse(tuple.getT1());
                    String body2 = extractAnswerFromResponse(tuple.getT2());
                    String body3 = extractAnswerFromResponse(tuple.getT3());
                    return "1: " + body1 + "\n2: " + body2 + "\n3: " + body3;
                }).doAfterTerminate(() -> notifyWebhook("orchestrator working").subscribe());
    }

    @CircuitBreaker(name = "microservice1", fallbackMethod = "fallback")
    @Retry(name = "microserviceClientRetry")
    private Mono<String> callMicroservice1(String requestBody) {
        System.out.println("Intentando llamar al microservicio 1...");
        return webClient1.post()
                .uri("/getStep")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (response.statusCode().value() == 501) {
                                        // Si el error es 501, retornar el cuerpo directamente
                                        return Mono.just(body);
                                    } else {
                                        // Otros errores del servidor
                                        return Mono.error(new WebClientResponseException("Server error", 500, "Server Error", null, null, null));
                                    }
                                });
                    } else {
                        return response.bodyToMono(String.class);
                    }
                });
    }


    @CircuitBreaker(name = "microservice2", fallbackMethod = "fallback")
    @Retry(name = "microserviceClientRetry")
    private Mono<String> callMicroservice2(String requestBody) {
        System.out.println("Intentando llamar al microservicio 2...");
        return webClient2.post()
                .uri("/getStep")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (response.statusCode().value() == 501) {
                                        return Mono.just(body);
                                    } else {
                                        return Mono.error(new WebClientResponseException("Server error", 500, "Server Error", null, null, null));
                                    }
                                });
                    } else {
                        return response.bodyToMono(String.class);
                    }
                });
    }


    @CircuitBreaker(name = "microservice3", fallbackMethod = "fallback")
    @Retry(name = "microserviceClientRetry")
    private Mono<String> callMicroservice3(String requestBody) {
        System.out.println("Intentando llamar al microservicio 3...");
        return webClient3.post()
                .uri("/getStep")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (response.statusCode().value() == 501) {
                                        return Mono.just(body);
                                    } else {
                                        return Mono.error(new WebClientResponseException("Server error", 500, "Server Error", null, null, null));
                                    }
                                });
                    } else {
                        return response.bodyToMono(String.class);
                    }
                });
    }


    private String extractAnswerFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get(0).get("data").get(0).get("answer").asText();
        } catch (Exception e) {
            return "Error parsing response";
        }
    }

    private Mono<String> fallback(String requestBody, Throwable t) {
        if (t instanceof WebClientResponseException) {
            WebClientResponseException exception = (WebClientResponseException) t;
            if (exception.getRawStatusCode() == 501) {
                return Mono.just("501 Error - Fallback triggered: " + exception.getResponseBodyAsString());
            }
        }
        return Mono.just("Fallback response: " + t.getMessage());
    }

    private Mono<String> notifyWebhook(String message) {
        return webhookClient.post()
                .uri("/webhook") // URL del webhook
                .bodyValue(message)
                .retrieve()
                .bodyToMono(String.class);
    }

}

