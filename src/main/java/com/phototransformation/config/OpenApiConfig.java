package com.phototransformation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI photoTransformationOpenAPI() {
        var devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development server");

        var contact = new Contact();
        contact.setName("Photo Transformation API");
        contact.setEmail("support@phototransformation.com");

        var license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        var info = new Info()
                .title("Photo Transformation API")
                .version("1.0.0")
                .contact(contact)
                .description("REST API for photo upload, transformation, and secure storage with AES encryption.")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}