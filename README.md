# Documentación del Proyecto

## Introducción
Este proyecto implementa un sistema distribuido compuesto por tres partes principales:
1. **Batch Service**: Un servicio que se ejecuta periódicamente para verificar el estado del sistema.
2. **Webhook Service**: Un servicio que recibe notificaciones cuando el orquestador completa su proceso.
3. **Orchestrator Service**: El componente central que orquesta la llamada a varios microservicios con manejo de errores usando **Resilience4j** (circuit breakers, retires).

## Contenido del Proyecto

- **Batch Service**: Llama al orquestador cada 2 minutos de manera automática.
- **Webhook Service**: Recibe una notificación de éxito del orquestador y responde con un mensaje de confirmación en la consola.
- **Orchestrator Service**: Orquesta varios microservicios y aplica políticas de manejo de errores usando circuit breakers. Si los microservicios fallan, se activa el manejo de errores correspondiente.

## Estructura del Proyecto

```
.
├── batch/
│   ├── BatchService.java
│   └── BatchConfiguration.java
├── webhook/
│   └── WebhookController.java
└── orchestrator/
    ├── MicroserviceClientService.java
    └── CircuitBreakerConfig.java
```

## Configuración de Circuit Breaker (Resilience4j)

El **orquestador** está configurado para manejar errores de microservicios mediante circuit breakers usando **Resilience4j**. Algunas configuraciones importantes incluyen:

- `failure-rate-threshold`: Define el umbral de error para abrir el circuito.
- `wait-duration-in-open-state`: Tiempo que el circuito permanece en estado abierto antes de intentar llamadas de nuevo.
- `sliding-window-size`: Tamaño de la ventana deslizante para contar fallas.
  
Configura estas propiedades en `application.properties` para ajustar el comportamiento.

## Requerimientos del Sistema

- **Java 17**
- **Spring Boot 3.x**
- **Resilience4j 1.x**
- **Maven 3.x**

## Instalación y Ejecución

### 1. Clonar el repositorio:
```bash
git clone https://github.com/tu-repositorio.git
```

### 2. Compilar y empaquetar el proyecto:
```bash
mvn clean install
```

### 3. Ejecutar el Orquestador:
```bash
java -jar orchestrator/target/orchestrator.jar
```

### 4. Ejecutar el Webhook:
```bash
java -jar webhook/target/webhook.jar
```

### 5. Ejecutar el Batch:
```bash
java -jar batch/target/batch.jar
```

## Funcionamiento

1. **Orchestrator** llama a tres microservicios (`http://localhost:8081`, `http://localhost:8082`, `http://localhost:8083`) y maneja las respuestas con **circuit breakers**.
2. Al finalizar la orquestación, el orquestador notifica al **Webhook Service**, el cual imprime en la consola: `Recibido el mensaje del orquestador`.
3. El **Batch Service** corre en intervalos regulares (cada 2 minutos) y verifica el estado de los microservicios a través del orquestador.

## Dependencias
- **Spring Boot WebFlux**: Para comunicación reactiva entre servicios.
- **Resilience4j**: Para manejo de circuit breakers.
- **Spring Scheduler**: Para la ejecución del batch.

## Ejemplos de Configuración

### `application.properties` para el Circuit Breaker:
```properties
resilience4j.circuitbreaker.instances.microservice1.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.microservice1.wait-duration-in-open-state=5s
resilience4j.circuitbreaker.instances.microservice1.sliding-window-size=5
```

## Contribuir
Para contribuir a este proyecto, envía un *pull request* o abre un *issue* describiendo el problema o la mejora que propones.

## Licencia
Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
