package pro.hirooka.izanami.domain.service.recorder;

import java.util.List;
import pro.hirooka.izanami.domain.model.recorder.RecordingProgramModel;

public interface IRecordingProgramManagementComponent {
  RecordingProgramModel create(int id, RecordingProgramModel recordingProgramModel);

  List<RecordingProgramModel> get();

  RecordingProgramModel get(int id);

  RecordingProgramModel update(int id, RecordingProgramModel recordingProgramModel);

  void delete(int id);

  void deleteAll();
}
