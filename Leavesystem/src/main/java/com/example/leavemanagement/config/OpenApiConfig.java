package com.example.leavemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI leaveManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leave Management API")
                        .version("1.0.0")
                        .description("Employee leave application and approval APIs."));
    }
}
