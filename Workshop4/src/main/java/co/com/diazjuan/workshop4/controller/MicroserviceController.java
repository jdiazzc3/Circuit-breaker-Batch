package co.com.diazjuan.workshop4.controller;
import co.com.diazjuan.workshop4.service.MicroserviceClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class MicroserviceController {

    @Autowired
    private MicroserviceClientService microserviceClientService;

    @GetMapping("/invoke")
    public Mono<String> invokeMicroservices() {
        return microserviceClientService.invokeMicroservices()
                .onErrorResume(e -> {
                    return Mono.just("Error en el orquestador: " + e.getMessage());
                });
    }

}

