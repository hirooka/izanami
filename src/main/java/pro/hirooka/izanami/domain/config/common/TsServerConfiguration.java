package pro.hirooka.izanami.domain.config.common;

import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_TS_SERVER_PASSWORD;
import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_TS_SERVER_USERNAME;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import pro.hirooka.izanami.domain.config.common.type.SchemeType;

@Data
@ConfigurationProperties(prefix = "ts-server")
@Configuration
public class TsServerConfiguration {
  private String username;
  private String password;
  private SchemeType scheme;
  private String host;
  private int port;
  private boolean enabled;
  private boolean unixDomainSocketEnabled;

  public String getUsername() {
    if (ObjectUtils.isEmpty(username)) {
      return DEFAULT_TS_SERVER_USERNAME;
    }
    return username;
  }

  public String getPassword() {
    if (ObjectUtils.isEmpty(password)) {
      return DEFAULT_TS_SERVER_PASSWORD;
    }
    return password;
  }
}
