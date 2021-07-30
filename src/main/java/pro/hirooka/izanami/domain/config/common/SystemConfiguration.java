package pro.hirooka.izanami.domain.config.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "system")
@Configuration
public class SystemConfiguration {
  String ffmpegPath;
  String webcamDeviceName;
  String webcamAudioName;
  int webcamAudioChannel;
  String temporaryPath;
  String filePath;
  int ffmpegThreads;
}
