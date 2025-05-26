package com.gamesmicroservice.rest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(columnDefinition = "number")
    private String age;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private String url;
}
