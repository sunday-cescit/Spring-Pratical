package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private GameRepository gameRepository;

    private Long existingGameId;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        Game game = new Game(null, "Test Game", "This is a valid game description with more than 20 characters.", "Category", 29.99, "https://example.com/game");
        existingGameId = gameRepository.save(game).getId();
    }


    @Test
    void getAllGames_shouldReturnGames() {
        ResponseEntity<Game[]> response = restTemplate.getForEntity("/api/games", Game[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void getGameById_shouldReturnGame() {
        ResponseEntity<Game> response = restTemplate.getForEntity("/api/games/" + existingGameId, Game.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(existingGameId, response.getBody().getId());
    }

    @Test
    void getGameById_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/games/99999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createGame_shouldReturnCreated() {
        Game newGame = new Game(null, "New Game", "This description is long enough to be valid.", "Category", 29.99, "https://example.com/newgame");
        ResponseEntity<Game> response = restTemplate.postForEntity("/api/games", newGame, Game.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("New Game", response.getBody().getName());
    }

}
