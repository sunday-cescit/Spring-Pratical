package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.Game;
import com.gamesmicroservice.rest.repository.GameRepository;
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
        return gameRepository.findAll();// to find all games
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id); //to find game by id
    }

    public Game createGame(Game game) {
        return gameRepository.save(game); //jpa provide save method
    }

    public Optional<Game> updateGame(Long id, Game gameDetails) {
        return gameRepository.findById(id)
                .map(existingGame -> {
                    existingGame.setName(gameDetails.getName());
                    existingGame.setDescription(gameDetails.getDescription());
                    existingGame.setCategory(gameDetails.getCategory());
                    existingGame.setPrice(gameDetails.getPrice());
                    existingGame.setUrl(gameDetails.getUrl());
                    return gameRepository.save(existingGame);
                });
    }

    public boolean deleteGame(Long id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }
}