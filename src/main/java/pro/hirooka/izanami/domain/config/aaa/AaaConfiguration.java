package pro.hirooka.izanami.domain.config.aaa;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "aaa")
@Configuration
public class AaaConfiguration {
  private boolean enabled;
  private String initialUsername;
  private String initialPassword;
}
