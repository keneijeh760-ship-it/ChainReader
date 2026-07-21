package com.vectoros.fleet.repository;

import com.vectoros.fleet.entity.Robot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RobotRepository extends JpaRepository<Robot, Long> {

    boolean existsByName(String name);

    Optional<Robot> findByName(String name);
}

