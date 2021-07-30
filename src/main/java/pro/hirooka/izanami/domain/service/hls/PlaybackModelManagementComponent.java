package pro.hirooka.izanami.domain.service.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;

@Slf4j
@Component
public class PlaybackModelManagementComponent implements IPlaybackModelManagementComponent {

  private Map<String, PlaybackModel> izanamiModelLMap = new ConcurrentHashMap<>();

  @Override
  public PlaybackModel create(final String username, final PlaybackModel playbackModel) {
    if (!izanamiModelLMap.containsKey(username)) {
      izanamiModelLMap.put(username, playbackModel);
      return izanamiModelLMap.get(username);
    }
    return null;
  }

  @Override
  public List<PlaybackModel> get() {
    return new ArrayList<>(izanamiModelLMap.values());
  }

  @Override
  public PlaybackModel get(final String username) {
    if (izanamiModelLMap.containsKey(username)) {
      return izanamiModelLMap.get(username);
    }
    return null;
  }

  @Override
  public PlaybackModel update(final String username, final PlaybackModel playbackModel) {
    if (izanamiModelLMap.containsKey(username)) {
      izanamiModelLMap.put(username, playbackModel);
      return izanamiModelLMap.get(username);
    }
    return null;
  }

  @Override
  public void delete(final String username) {
    if (izanamiModelLMap.containsKey(username)) {
      izanamiModelLMap.remove(username);
    }
  }

  @Override
  public void deleteAll() {
    izanamiModelLMap.clear();
  }
}
