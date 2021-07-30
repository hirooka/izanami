package pro.hirooka.izanami.domain.config.epg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "epg")
@Configuration
public class EpgConfiguration {
  private String tuner;
  private String channelConfiguration;
  String acquisitionScheduleCron;
  long acquisitionOnBootIgnoredInterval;
}
