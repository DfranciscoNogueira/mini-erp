package br.com.mini.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniErpOpenAPI() {
        return new OpenAPI().info(new Info().title("Mini-ERP").version("v1"));
    }

    // http://localhost:8080/swagger-ui/index.html
}


