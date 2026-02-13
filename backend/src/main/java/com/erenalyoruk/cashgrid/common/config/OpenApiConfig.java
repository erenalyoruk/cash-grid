package com.erenalyoruk.cashgrid.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cashGridOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("CashGrid API")
                                .description(
                                        "Commercial Cash Payment Platform with Maker-Checker"
                                                + " Approval Workflow")
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Eren Alyörük")
                                                .email("eren@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "Bearer Authentication",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Enter JWT token")));
    }
}
