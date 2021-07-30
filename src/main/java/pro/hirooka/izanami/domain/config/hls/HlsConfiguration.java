package pro.hirooka.izanami.domain.config.hls;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "hls")
@Configuration
public class HlsConfiguration {
  int duration;
  int uriInPlaylist;
}
