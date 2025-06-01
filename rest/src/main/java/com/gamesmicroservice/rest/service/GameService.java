package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll(); // get all games
    }

    @Cacheable(value = "recordCache", key = "#id")
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id); // get game by ID with caching
    }

    @Cacheable(value = "categoryCache", key = "#category")
    public List<Game> getGamesByCategory(String category) {
        return gameRepository.findByCategoryIgnoreCase(category); // get games by category with caching
    }

    @CacheEvict(value = "categoryCache", key = "#game.category")
    public Game createGame(Game game) {
        return gameRepository.save(game); // save new game and evict related category cache
    }
    
    @Caching(evict = {
        @CacheEvict(value = "recordCache", key = "#id"),
        @CacheEvict(value = "categoryCache", key = "#result.category", condition = "#result != null")
    })
    public Game updateGame(Long id, Game gameDetails) {
        return gameRepository.findById(id)
                .map(existingGame -> {
                    existingGame.setName(gameDetails.getName());
                    existingGame.setDescription(gameDetails.getDescription());
                    existingGame.setCategory(gameDetails.getCategory());
                    existingGame.setPrice(gameDetails.getPrice());
                    existingGame.setUrl(gameDetails.getUrl());
                    return gameRepository.save(existingGame);
                })
                .orElse(null);
    }
    

    @Caching(evict = {
        @CacheEvict(value = "recordCache", key = "#id"),
        @CacheEvict(value = "categoryCache", allEntries = true) // fallback if category is unknown
    })
    public boolean deleteGame(Long id) {
        return gameRepository.findById(id).map(game -> {
            gameRepository.deleteById(id);
            return true;
        }).orElse(false);
    }
}
