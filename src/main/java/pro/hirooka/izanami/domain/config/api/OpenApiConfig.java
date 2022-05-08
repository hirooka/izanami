package pro.hirooka.izanami.domain.config.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    public OpenApiConfig(BuildProperties buildProperties, GitProperties gitProperties) {
      this.buildProperties = buildProperties;
      this.gitProperties = gitProperties;
    }

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
                .version(buildProperties.getVersion() + "." + gitProperties.getShortCommitId())
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
