package pro.hirooka.izanami.domain.config.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "common")
@Configuration
public class CommonConfiguration {
  private String[] videoFileExtension;
}
