package pro.hirooka.izanami.domain.service.common;

import static java.util.Objects.requireNonNull;
import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_ROOT_PATH_NAME;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.common.MongoDbConfiguration;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.model.common.EmbeddedPlayerType;

@Slf4j
@Service
public class SystemService implements ISystemService {

  @Value("${system.ffmpeg-vcodec}")
  private String ffmpegVcodec;

  // https://developer.mozilla.org/ja/docs/Web/HTTP/Browser_detection_using_the_user_agent
  private static final String SAFARI = "Safari/";
  private static final String CHROME = "Chrome/";
  private static final String CHROMIUM = "Chromium/";
  private static final String EDGE = "Edge/";

  private final SystemConfiguration systemConfiguration;
  private final MongoDbConfiguration mongoDbConfiguration;

  public SystemService(
      SystemConfiguration systemConfiguration,
      MongoDbConfiguration mongoDbConfiguration
  ) {
    this.systemConfiguration = requireNonNull(systemConfiguration);
    this.mongoDbConfiguration = requireNonNull(mongoDbConfiguration);
  }

  @Override
  public boolean isFFmpeg() {
    return new File(systemConfiguration.getFfmpegPath()).exists();
  }

  @Override
  public boolean isWebCamera() {
    final String webCameraDeviceName = systemConfiguration.getWebcamDeviceName();
    return new File(webCameraDeviceName).exists();
  }

  @Override
  public String getWebCameraDeviceName() {
    return systemConfiguration.getWebcamDeviceName();
  }

  @Override
  public boolean isMongoDB() {
    if (mongoDbConfiguration.getHost() == null) {
      return false;
    }
    if (mongoDbConfiguration.getHost().equals("izanami-mongo")) {
      return true;
    }
    final MongoClient mongoClient = MongoClients.create();
    try {
      mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
      mongoClient.close();
      log.info("MongoDB is running.");
      return true;
    } catch (Exception ex) {
      log.info("MongoDB is down or not installed.");
      mongoClient.close();
      return false;
    }
  }

  @Override
  public boolean canWebCameraStreaming() {
    return isFFmpeg() && isWebCamera();
  }

  @Override
  public boolean canFileStreaming() {
    return isFFmpeg();
  }

  @Override
  public boolean canPTxStreaming() {
    return isFFmpeg();
  }

  @Override
  public boolean canRecording() {
    return isFFmpeg() && isMongoDB();
  }

  private boolean isSafari(final String userAgent) {
    return userAgent.contains(SAFARI)
        && !userAgent.contains(CHROME)
        && !userAgent.contains(CHROMIUM);
  }

  private boolean isChrome(final String userAgent) {
    return userAgent.contains(CHROME) && !userAgent.contains(CHROMIUM);
  }

  private boolean isEdge(final String userAgent) {
    return userAgent.contains(EDGE);
  }

  @Override
  public FfmpegVcodecType getFfmpegVcodecType(final String userAgent) {
    if (isChrome(userAgent) || isEdge(userAgent)) {
      return FfmpegVcodecType.H264_NVENC;
    } else if (isSafari(userAgent)) {
      return FfmpegVcodecType.valueOf(ffmpegVcodec.toUpperCase());
    }
    throw new IllegalArgumentException("Unsupported");
  }

  @Override
  public EmbeddedPlayerType getEmbeddedPlayerType(final String userAgent) {
    if (isSafari(userAgent) || isEdge(userAgent)) {
      return EmbeddedPlayerType.NATIVE;
    } else if (isChrome(userAgent)) {
      return EmbeddedPlayerType.HLSJS;
    }
    throw new IllegalArgumentException("Unsupported");
  }

  @Override
  public String getStreamRootPath(final String servletRealPath) {
    if (servletRealPath.substring(servletRealPath.length() - 1).equals(FILE_SEPARATOR)) {
      return servletRealPath + STREAM_ROOT_PATH_NAME; // e.g. Tomcat
    } else {
      return servletRealPath + FILE_SEPARATOR + STREAM_ROOT_PATH_NAME; // e.g. Jetty
    }
  }
}

