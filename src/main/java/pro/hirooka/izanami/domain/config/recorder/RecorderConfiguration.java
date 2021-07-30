package pro.hirooka.izanami.domain.config.recorder;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "recorder")
@Configuration
public class RecorderConfiguration {
  long startMargin;
  long stopMargin;
}
