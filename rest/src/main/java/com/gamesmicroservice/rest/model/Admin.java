package com.gamesmicroservice.rest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Admin extends User {
    //Use the variables that are in user class.

    // 6+2=8 Variables total 6  from  user class  and 2 in admin class
    @Column(nullable = false)
    private String adminRole;

    @Column(columnDefinition = "number")
    private String adminSpecification;

}
