package pro.hirooka.izanami.domain.service.common;

import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.model.common.EmbeddedPlayerType;

public interface ISystemService {
  boolean isFFmpeg();

  boolean isWebCamera();

  String getWebCameraDeviceName();

  boolean isMongoDB();

  boolean canWebCameraStreaming();

  boolean canFileStreaming();

  boolean canPTxStreaming();

  boolean canRecording();

  FfmpegVcodecType getFfmpegVcodecType(final String userAgent);

  String getStreamRootPath(final String servletRealPath);

  EmbeddedPlayerType getEmbeddedPlayerType(final String userAgent);
}
