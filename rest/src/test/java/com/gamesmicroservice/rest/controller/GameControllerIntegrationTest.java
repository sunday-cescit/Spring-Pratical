package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    private Long existingGameId;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        Game game = new Game(null, "Test Game", "This is a valid game description with more than 20 characters.", "Category", 29.99, "https://example.com/game");
        existingGameId = gameRepository.save(game).getId();

        // Simulate login and extract the JWT token
        loginAndExtractJwtToken();
    }

private void loginAndExtractJwtToken() {
    String loginPayload = "{\"username\": \"akash\", \"password\": \"akash123\"}";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(loginPayload, headers);

    ResponseEntity<String> response = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            request,
            String.class
    );

    System.out.println("Response Body: " + response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        jwtToken = json.get("token").asText(); // adjust key name if it's "access_token" or similar
        assertNotNull(jwtToken, "JWT token should not be null");
    } catch (Exception e) {
        e.printStackTrace();
        fail("Failed to parse JWT token from response");
    }
}

    // Helper to create headers with the JWT token
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        return headers;
    }

    // Test for GET /api/games
    @Test
    void getAllGames_shouldReturnGames() {
        ResponseEntity<Game[]> response = restTemplate.exchange(
                "/api/games", 
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()), 
                Game[].class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    // Test for GET /api/games/{id}
    @Test
    void getGameById_shouldReturnGame() {
        ResponseEntity<Game> response = restTemplate.exchange(
                "/api/games/" + existingGameId, 
                HttpMethod.GET, 
                new HttpEntity<>(createHeaders()), 
                Game.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(existingGameId, response.getBody().getId());
    }

    // Test for POST /api/games (requires ADMIN role)
    @Test
    void createGame_shouldReturnCreated() {
        Game newGame = new Game(null, "New Game", "This description is long enough to be valid.", "Category", 29.99, "https://example.com/newgame");

        ResponseEntity<Game> response = restTemplate.exchange(
                "/api/games",
                HttpMethod.POST,
                new HttpEntity<>(newGame, createHeaders()),
                Game.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("New Game", response.getBody().getName());
    }

    // Test for GET /api/games with invalid id
    @Test
    void getGameById_notFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/games/99999", 
                HttpMethod.GET, 
                new HttpEntity<>(createHeaders()), 
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
