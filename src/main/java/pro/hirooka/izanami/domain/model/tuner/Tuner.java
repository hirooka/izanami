package pro.hirooka.izanami.domain.model.tuner;

import lombok.Data;

@Data
public class Tuner {
  private String displayName;
  private TunerType type;
  private String deviceName;
  private int index;
  private String command;

  private int channelRecording;
  private long pid;
  private TunerStatus status = TunerStatus.AVAILABLE;
  private Process process;
}

