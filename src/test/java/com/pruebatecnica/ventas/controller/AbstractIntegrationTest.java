package com.pruebatecnica.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebatecnica.ventas.dto.auth.LoginRequestDTO;
import com.pruebatecnica.ventas.dto.auth.LoginResponseDTO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base común para las pruebas de integración de los controllers.
 *
 * Se usa un contenedor MySQL real (Testcontainers) en lugar de H2, tanto
 * para respetar el requerimiento del enunciado ("no se acepta otro motor
 * de base de datos") como para probar contra el mismo motor que se usará
 * en producción. El contenedor se declara estático y se comparte entre
 * todas las subclases dentro de la misma JVM (patrón "singleton container"
 * recomendado por Testcontainers), evitando levantar un MySQL por clase.
 *
 * Requiere Docker disponible en la máquina donde se ejecutan los tests.
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("ventas_test_db")
                    .withUsername("test_user")
                    .withPassword("test_pass");

    @DynamicPropertySource
    static void overrideDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Inicia sesión con el usuario administrador sembrado por DataSeeder y
     * devuelve el token JWT, listo para usarse en el header Authorization.
     */
    protected String obtenerTokenAdmin() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("admin");
        login.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponseDTO.class);
        return response.getToken();
    }
}
