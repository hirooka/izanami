package pro.hirooka.izanami.domain.model.hls;

import lombok.Data;
import pro.hirooka.izanami.domain.config.common.type.PlaylistType;
import pro.hirooka.izanami.domain.config.common.type.StreamingType;
import pro.hirooka.izanami.domain.config.common.type.TranscodingSettings;
import pro.hirooka.izanami.domain.model.tuner.TunerType;

@Data
public class PlaybackSettings {
  private int adaptiveBitrateStreaming;
  private StreamingType streamingType;
  private PlaylistType playlistType;
  private TranscodingSettings transcodingSettings;
  private boolean canEncrypt;
  private int channelRemoteControl;
  private TunerType tunerType;
  private String fileName;

  private String videoResolution;
  private int videoBitrate;
  private int audioBitrate;

  private String username;
}
