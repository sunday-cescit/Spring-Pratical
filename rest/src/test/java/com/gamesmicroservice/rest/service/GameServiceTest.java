package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    private Game testGame;

    @BeforeEach
    void setUp() {
        testGame = new Game(1L, "Test Game", "Some Description...", "Action", 29.99, "https://example.com/game");
    }

    @Test
    void getAllGames_shouldReturnAllGames() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        List<Game> games = gameService.getAllGames();

        assertFalse(games.isEmpty());
        assertEquals(1, games.size());
        assertEquals("Test Game", games.get(0).getName());
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void getGameById_shouldReturnGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        Optional<Game> game = gameService.getGameById(1L);

        assertTrue(game.isPresent());
        assertEquals("Test Game", game.get().getName());
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void createGame_shouldSaveGame() {
        Game inputGame = new Game(null, "Test Game", "Some Description...", "Action", 29.99,
                "https://example.com/game");
        Game savedGame = new Game(1L, "Test Game", "Some Description...", "Action", 29.99, "https://example.com/game");

        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        Game created = gameService.createGame(inputGame);

        assertNotNull(created.getId());
        assertEquals("Test Game", created.getName());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void updateGame_shouldUpdateExistingGame() {
        Game updatedDetails = new Game(null, "Updated Name", "Updated Description", "Adventure", 39.99,
                "https://example.com/updated");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Game> updated = gameService.updateGame(1L, updatedDetails);

        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("Updated Description", updated.get().getDescription());
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }
}
