package pro.hirooka.izanami.domain.operator;

import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.model.hls.PlaybackSettings;

public interface IHlsOperator {
  PlaybackModel startPlayback(
      final PlaybackSettings playbackSettings,
      final String userAgent,
      final String servletRealPath
  );

  void stopPlayback();

  void removeStream();
}
