package pro.hirooka.izanami.domain.model.recorder;

import lombok.Data;

@Data
public class RecordingProgramModel {
  private String fileName;
  private long startRecording;
  private long stopRecording;
}
