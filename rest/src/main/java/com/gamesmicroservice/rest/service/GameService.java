package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private static final String RECORD_CACHE = "recordCache";
    private static final String CATEGORY_CACHE = "categoryCache";
    private static final String ALL_GAMES_CACHE = "allGames";
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final RedisCacheService redisCacheService;

    public GameService(GameRepository gameRepository, RedisCacheService redisCacheService) {
        this.gameRepository = gameRepository;
        this.redisCacheService = redisCacheService;
    }

    public List<Game> getAllGames() {
        logger.info("Fetching all games from cache...");
        Optional<List> cachedGames = redisCacheService.get(ALL_GAMES_CACHE, "ALL", List.class);
        
        if (cachedGames.isPresent()) {
            logger.info("Cache hit for all games");
            return (List<Game>) cachedGames.get();
        }
    
        logger.info("Cache miss for all games. Fetching from DB...");
        List<Game> games = gameRepository.findAll();
        redisCacheService.put(ALL_GAMES_CACHE, "ALL", games);
        return games;
    }

    public Optional<Game> getGameById(Long id) {
        logger.info("Fetching game with ID {} from cache...", id);
        Optional<Game> cachedGame = redisCacheService.get(RECORD_CACHE, id.toString(), Game.class);
        if (cachedGame.isPresent()) {
            logger.info("Cache hit for game ID {}", id);
            return cachedGame;
        }
    
        logger.info("Cache miss for game ID {}. Fetching from DB...", id);
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(g -> redisCacheService.put(RECORD_CACHE, id.toString(), g));
        return game;
    }

    public List<Game> getGamesByCategory(String category) {
        logger.info("Checking cache for category: {}", category);
        Optional<List> cachedGames = redisCacheService.get(CATEGORY_CACHE, category, List.class);
        
        if (cachedGames.isPresent()) {
            logger.info("Cache hit for category: {}", category);
            return (List<Game>) cachedGames.get(); 
        }
    
        logger.info("Cache miss for category: {}. Querying DB...", category);
        List<Game> games = gameRepository.findByCategoryIgnoreCase(category);
        if (!games.isEmpty()) {
            redisCacheService.put(CATEGORY_CACHE, category, games);
        }
        return games;
    }

    public Game createGame(Game game) {
        Game createdGame = gameRepository.save(game);
        logger.info("Game created. Evicting category cache for '{}'", game.getCategory());
    
        redisCacheService.evict(CATEGORY_CACHE, game.getCategory());
        redisCacheService.evict(ALL_GAMES_CACHE, "ALL");
        return createdGame;
    }

    public Game updateGame(Long id, Game gameDetails) {
        return gameRepository.findById(id)
                .map(existingGame -> {
                    String oldCategory = existingGame.getCategory();
    
                    existingGame.setName(gameDetails.getName());
                    existingGame.setDescription(gameDetails.getDescription());
                    existingGame.setCategory(gameDetails.getCategory());
                    existingGame.setPrice(gameDetails.getPrice());
                    existingGame.setUrl(gameDetails.getUrl());
    
                    Game updatedGame = gameRepository.save(existingGame);
    
                    logger.info("Game updated. Evicting caches for ID: {}, old category: {}, new category: {}",
                                id, oldCategory, updatedGame.getCategory());
    
                    redisCacheService.evict(RECORD_CACHE, id.toString());
                    redisCacheService.evict(CATEGORY_CACHE, oldCategory);
                    redisCacheService.evict(CATEGORY_CACHE, updatedGame.getCategory());
                    redisCacheService.evict(ALL_GAMES_CACHE, "ALL");
    
                    return updatedGame;
                })
                .orElse(null);
    }
    
    public boolean deleteGame(Long id) {
        return gameRepository.findById(id)
                .map(game -> {
                    String category = game.getCategory();
                    gameRepository.deleteById(id);
    
                    logger.info("Game deleted. Evicting caches for ID: {} and category: {}", id, category);
                    redisCacheService.evict(RECORD_CACHE, id.toString());
                    redisCacheService.evict(CATEGORY_CACHE, category);
                    redisCacheService.evict(ALL_GAMES_CACHE, "ALL");
    
                    return true;
                })
                .orElse(false);
    }    
}