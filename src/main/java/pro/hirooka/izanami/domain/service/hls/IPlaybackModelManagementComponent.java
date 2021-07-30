package pro.hirooka.izanami.domain.service.hls;

import java.util.List;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;

public interface IPlaybackModelManagementComponent {
  PlaybackModel create(final String username, final PlaybackModel playbackModel);

  List<PlaybackModel> get();

  PlaybackModel get(final String username);

  PlaybackModel update(final String username, final PlaybackModel playbackModel);

  void delete(final String username);

  void deleteAll();
}
