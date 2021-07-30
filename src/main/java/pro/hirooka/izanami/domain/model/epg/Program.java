package pro.hirooka.izanami.domain.model.epg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Program {
  private UUID uuid = UUID.randomUUID();
  @Id
  private String id;
  private String channel;
  private String title;
  private String detail;
  //    private List<Item> extdetail;
  private long start;
  private long end;
  private long duration;
  //    private List<Category> category;
  //    private List<?> attachinfo;
  //    private Video video;
  //    private List<Audio> audio;
  //    private boolean freeCA;
  //    private int eventID;

  private long begin;
  //private int physicalChannel;
  private String channelName;
  private String beginDate;
  private String endDate;
  //    private int physicalLogicalChannel;
  //    private int remoteControllerChannel;
  private int channelRecording;
  private int channelRemoteControl;

  private int height;
}

