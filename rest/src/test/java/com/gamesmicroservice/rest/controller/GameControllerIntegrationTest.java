package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.model.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllGames_shouldReturnGames() {
        ResponseEntity<Game[]> response = restTemplate.getForEntity("/api/games", Game[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void getGameById_shouldReturnGame() {
        ResponseEntity<Game> response = restTemplate.getForEntity("/api/games/1", Game.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getGameById_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/games/999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createGame_shouldReturnCreated() {
        Game newGame = new Game(null, "New Game", "Description", "Category", "29.99", "https://example.com/newgame");
        ResponseEntity<Game> response = restTemplate.postForEntity("/api/games", newGame, Game.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("New Game", response.getBody().getName());
    }
}