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
        testGame = new Game(1L, "Test Game", "Test Description", "Test Category", "19.99", "http://test.com");
    }

    @Test
    void getAllGames_shouldReturnAllGames() {
        // Arrange
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        // Act
        List<Game> games = gameService.getAllGames();

        // Assert
        assertFalse(games.isEmpty());
        assertEquals(1, games.size());
        assertEquals("Test Game", games.get(0).getName());
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void getGameById_shouldReturnGame() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        // Act
        Optional<Game> game = gameService.getGameById(1L);

        // Assert
        assertTrue(game.isPresent());
        assertEquals("Test Game", game.get().getName());
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void createGame_shouldSaveGame() {
        // Arrange
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        // Act
        Game created = gameService.createGame(testGame);

        // Assert
        assertNotNull(created.getId());
        assertEquals("Test Game", created.getName());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void updateGame_shouldUpdateExistingGame() {
        // Arrange
        Game updatedDetails = new Game(null, "Updated Name", "Updated Desc", "Updated Cat", "29.99", "updated.url");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        // Act
        Optional<Game> updated = gameService.updateGame(1L, updatedDetails);

        // Assert
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(testGame);
    }

    @Test
    void deleteGame_shouldReturnTrueWhenExists() {
        // Arrange
        when(gameRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gameRepository).deleteById(1L);

        // Act
        boolean result = gameService.deleteGame(1L);

        // Assert
        assertTrue(result);
        verify(gameRepository, times(1)).existsById(1L);
        verify(gameRepository, times(1)).deleteById(1L);
    }
}