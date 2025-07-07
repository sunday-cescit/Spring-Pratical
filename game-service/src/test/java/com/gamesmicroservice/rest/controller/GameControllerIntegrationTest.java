package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

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

    // Generate JWT manually (simulate authenticated user with roles)
    jwtToken = generateTestJwt("akash1", List.of("ROLE_ADMIN"));
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
                Game[].class);
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
                Game.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(existingGameId, response.getBody().getId());
    }

    // Test for POST /api/games (requires ADMIN role)
    @Test
    void createGame_shouldReturnCreated() {
        Game newGame = new Game(null, "New Game", "This description is long enough to be valid.", "Category", 29.99,
                "https://example.com/newgame");

        ResponseEntity<Game> response = restTemplate.exchange(
                "/api/games",
                HttpMethod.POST,
                new HttpEntity<>(newGame, createHeaders()),
                Game.class);

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
                String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
