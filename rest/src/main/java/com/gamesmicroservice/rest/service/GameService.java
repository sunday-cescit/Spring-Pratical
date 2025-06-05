package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private static final String RECORD_CACHE = "recordCache";
    private static final String CATEGORY_CACHE = "categoryCache";
    
    private final GameRepository gameRepository;
    private final RedisCacheService redisCacheService;

    public GameService(GameRepository gameRepository, RedisCacheService redisCacheService) {
        this.gameRepository = gameRepository;
        this.redisCacheService = redisCacheService;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(Long id) {
        Optional<Game> cachedGame = redisCacheService.get(RECORD_CACHE, id.toString(), Game.class);
        if (cachedGame.isPresent()) {
            return cachedGame;
        }
        
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(g -> redisCacheService.put(RECORD_CACHE, id.toString(), g));
        return game;
    }

    public List<Game> getGamesByCategory(String category) {
       /*  Optional<List<Game>> cachedGames = redisCacheService.get(CATEGORY_CACHE, category, List.class);
        if (cachedGames.isPresent()) {
            return cachedGames.get();
        } */
        
        List<Game> games = gameRepository.findByCategoryIgnoreCase(category);
        if (!games.isEmpty()) {
            redisCacheService.put(CATEGORY_CACHE, category, games);
        }
        return games;
    }

    public Game createGame(Game game) {
        Game createdGame = gameRepository.save(game);
        // Evict the category cache for this game's category
        redisCacheService.evict(CATEGORY_CACHE, game.getCategory());
        return createdGame;
    }

    public Game updateGame(Long id, Game gameDetails) {
        return gameRepository.findById(id)
                .map(existingGame -> {
                    // Get old category before update
                    String oldCategory = existingGame.getCategory();
                    
                    existingGame.setName(gameDetails.getName());
                    existingGame.setDescription(gameDetails.getDescription());
                    existingGame.setCategory(gameDetails.getCategory());
                    existingGame.setPrice(gameDetails.getPrice());
                    existingGame.setUrl(gameDetails.getUrl());
                    
                    Game updatedGame = gameRepository.save(existingGame);
                    
                    // Evict caches
                    redisCacheService.evict(RECORD_CACHE, id.toString());
                    
                    // If category changed, evict both old and new category caches
                    if (!oldCategory.equalsIgnoreCase(updatedGame.getCategory())) {
                        redisCacheService.evict(CATEGORY_CACHE, oldCategory);
                    }
                    redisCacheService.evict(CATEGORY_CACHE, updatedGame.getCategory());
                    
                    return updatedGame;
                })
                .orElse(null);
    }

    public boolean deleteGame(Long id) {
        return gameRepository.findById(id)
                .map(game -> {
                    // Get category before deletion
                    String category = game.getCategory();
                    
                    gameRepository.deleteById(id);
                    
                    // Evict caches
                    redisCacheService.evict(RECORD_CACHE, id.toString());
                    redisCacheService.evict(CATEGORY_CACHE, category);
                    
                    return true;
                })
                .orElse(false);
    }
}