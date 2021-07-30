package pro.hirooka.izanami.domain.config.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${info.app.version}")
  String version;

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .components(
            new Components()
                .addSecuritySchemes(
                    "izanami",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("izanami")
                )
        ).info(
            new Info()
                .title("API")
                .version(version)
                .description("API")
                .termsOfService("")
                .license(
                    new License()
                        .name("")
                        .url("")
                )
        );
  }
}
