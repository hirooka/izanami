package pro.hirooka.izanami.domain.model.recorder;

import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ReservedProgram {
  private UUID uuid = UUID.randomUUID();
  @Id
  private int id;
  private String channel;
  private String title;
  private String detail;
  private long start;
  private long end;
  private long duration;
  //    private boolean freeCA;
  //    private int eventID;

  private long begin;
  private String channelName;
  private String beginDate;
  private String endDate;
  //    private int physicalLogicalChannel;
  //    private int remoteControllerChannel;
  private int channelRecording;
  private int channelRemoteControl;

  private long startRecording;
  private long stopRecording;
  //private long durationRecording;
  private long recordingDuration;

  private String fileName;
  RecordingStatus recordingStatus;
  M4vTranscodingStatus m4vTranscodingStatus;
}
