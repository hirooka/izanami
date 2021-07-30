package pro.hirooka.izanami.domain.model.epg;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ChannelConfigurationWrapper {
  @JsonProperty("channelConfiguration")
  private List<ChannelConfiguration> channelConfigurationList;
}
