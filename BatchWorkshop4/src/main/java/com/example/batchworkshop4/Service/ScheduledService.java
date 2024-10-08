package com.example.batchworkshop4.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ScheduledService {

    private final WebClient webClient;

    public ScheduledService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @Scheduled(fixedRate = 120000)
    public void callOrchestrator() {

        Mono<String> response = webClient.get()
                .uri("/invoke")
                .retrieve()
                .bodyToMono(String.class);

        response.subscribe(
                result -> System.out.println("Orquestador respondió: " + result),
                error -> System.err.println("Error al invocar orquestador: " + error.getMessage())
        );
    }
}