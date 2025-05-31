package com.gamesmicroservice.rest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Details about a game")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 6, max = 30, message = "Name must be between 6 and 30 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 200, message = "Description must be between 20 and 200 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "5.00", message = "Price must be at least 5.00")
    @Column(nullable = false)
    private Double price;

    @NotBlank(message = "URL is required")
    @Column(nullable = false)
    private String url;
}