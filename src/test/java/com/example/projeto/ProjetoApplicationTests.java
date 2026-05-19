package com.example.projeto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de integração que verifica que o contexto completo do Spring Boot
 * carrega corretamente com um MongoDB real (via Testcontainers).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
class ProjetoApplicationTests {

    // @ServiceConnection configura automaticamente o spring.data.mongodb.uri
    // apontando para o container MongoDB que sobe antes dos testes
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring Boot carrega corretamente
    }
}
