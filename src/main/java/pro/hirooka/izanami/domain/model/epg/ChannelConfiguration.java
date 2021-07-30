package pro.hirooka.izanami.domain.model.epg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import pro.hirooka.izanami.domain.model.tuner.TunerType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ChannelConfiguration {
  private TunerType type;
  private int channelRemoteControl;
  private int channelRecording;
  private int serviceId;
}
