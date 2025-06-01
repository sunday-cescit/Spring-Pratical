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
        // Given
        Game existingGame = new Game(1L, "Original Name", "Original Description", "Action", 
                                29.99, "https://example.com/original");
        Game updatedDetails = new Game(null, "Updated Name", "Updated Description", "Adventure", 
                                    39.99, "https://example.com/updated");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(existingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Game result = gameService.updateGame(1L, updatedDetails);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId()); // Verify ID remains the same
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Adventure", result.getCategory());
        assertEquals(39.99, result.getPrice(), 0.001);
        assertEquals("https://example.com/updated", result.getUrl());
        
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void updateGame_shouldReturnNullWhenGameNotFound() {
        // Given
        Game updatedDetails = new Game(null, "Updated Name", "Updated Description", "Adventure", 
                                    39.99, "https://example.com/updated");
        
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Game result = gameService.updateGame(1L, updatedDetails);

        // Then
        assertNull(result);
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, never()).save(any(Game.class));
    }
}
