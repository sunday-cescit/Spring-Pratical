package com.gamesmicroservice.rest.repository;

import com.gamesmicroservice.rest.model.ERole;
import com.gamesmicroservice.rest.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}