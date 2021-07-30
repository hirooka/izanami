package pro.hirooka.izanami.domain.config.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Configuration
public class MongoDbConfiguration {
  String host;
  int port;
  String database;
}
