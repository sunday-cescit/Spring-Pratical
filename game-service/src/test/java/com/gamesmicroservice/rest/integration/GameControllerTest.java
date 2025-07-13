package com.gamesmicroservice.rest.integration;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class GameControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private GameRepository gameRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        Game game = new Game(null, "Test Game", "This is a valid game description with more than 20 characters.", 
                           "Category", 29.99, "https://example.com/game");
        gameRepository.save(game);

        // Generate JWT token for authentication
        jwtToken = generateTestJwt("testuser", List.of("ROLE_ADMIN"));
    }

    private String generateTestJwt(String username, List<String> roles) {
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
            .signWith(Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("YWthc2gtc2VjcmV0LWtleS1oZXJlLW1ha2UtaXQtbG9uZy1lbm91Z2gtZm9yLUhTMjU2")
            ))
            .compact();
    }

    @Test
    void whenGetAllGames_thenInitGameIsReturned() throws Exception {
        mvc.perform(get("/api/games")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
           .andExpect(jsonPath("$[0].name", is("Test Game")));
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
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newGameJson))
           .andExpect(status().isCreated())
           .andReturn()
           .getResponse()
           .getHeader("Location");

        // Extract ID from Location header
        String[] parts = location.split("/");
        String id = parts[parts.length - 1];

        // Fetch it back
        mvc.perform(get("/api/games/{id}", id)
                .header("Authorization", "Bearer " + jwtToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name", is("TC New Game")))
           .andExpect(jsonPath("$.price", is(25.5)));
    }

    @Test
    void whenGetNonExistentGame_thenNotFound() throws Exception {
        mvc.perform(get("/api/games/99999")
                .header("Authorization", "Bearer " + jwtToken))
           .andExpect(status().isNotFound());
    }
}