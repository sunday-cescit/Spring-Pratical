package com.gamesmicroservice.rest.repository;

import com.gamesmicroservice.rest.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    // Custom queries can be added here
    // Example: List<Game> findByCategory(String category);
}