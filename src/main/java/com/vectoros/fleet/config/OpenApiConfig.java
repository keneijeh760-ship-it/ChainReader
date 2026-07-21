package com.vectoros.fleet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VectorOS Fleet")
                        .version("v1.0.0")
                        .description("Fleet Management Service — central coordinator of the VectorOS platform."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")));
    }
}
