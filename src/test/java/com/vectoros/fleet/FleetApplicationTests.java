package com.vectoros.fleet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FleetApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationCreatesRobotsAndTasksTables() {
        Integer robotsTableExists = jdbcTemplate.queryForObject(
                "SELECT CASE WHEN to_regclass('public.robots') IS NOT NULL THEN 1 ELSE 0 END",
                Integer.class
        );
        Integer tasksTableExists = jdbcTemplate.queryForObject(
                "SELECT CASE WHEN to_regclass('public.tasks') IS NOT NULL THEN 1 ELSE 0 END",
                Integer.class
        );

        Assertions.assertEquals(1, robotsTableExists);
        Assertions.assertEquals(1, tasksTableExists);
    }
}
