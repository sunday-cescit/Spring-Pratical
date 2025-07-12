/* package com.gamesmicroservice.rest.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GameControllerTest {

    // 1) Define the container, point to our init script
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql"); // runs src/test/resources/schema.sql

    // 2) Override Spring properties so our app uses the container
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        // ensure Hibernate doesn’t try to re-run init scripts
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        // 👇 Reduce the connection pool size to avoid overload
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");

    }

    @Autowired
    MockMvc mvc;

    @Test
    void whenGetAllGames_thenInitGameIsReturned() throws Exception {
        mvc.perform(get("/api/games")
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
           .andExpect(jsonPath("$[0].name", is("Integration Test Game")));
    }

    @Test
    void whenCreateAndGetById_thenWorks() throws Exception {
        String newGameJson = """
            {
              "name":"TC New Game",
              "description":"A game created in TestContainer",
              "category":"TC",
              "price":25.5,
              "url":"http://example.com/tcnew"
            }
        """;

        // Create
        String location = mvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newGameJson))
           .andExpect(status().isCreated())
           .andReturn()
           .getResponse()
           .getHeader("Location");

        // Extract ID from Location header (/api/games/{id})
        String[] parts = location.split("/");
        String id = parts[parts.length - 1];

        // Fetch it back
        mvc.perform(get("/api/games/{id}", id))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name", is("TC New Game")))
           .andExpect(jsonPath("$.price", is(25.5)));
    }
}  */