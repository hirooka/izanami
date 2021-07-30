package pro.hirooka.izanami.domain.service.common;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;
import org.springframework.http.ResponseEntity;
import pro.hirooka.izanami.domain.model.epg.Program;

public interface ITsServerClientService {
  List<Program> getProgramListNow();

  List<Program> getProgramListByChannelRecording(int channelRecording);

  List<Program> getProgramListByChannelRemoteControl(int channelRemoteControl);

  Program getProgramByChannelRecordingNow(int channelRecording);

  Program getProgramByChannelRemoteControlNow(int channelRemoteControl);

  Future<ResponseEntity<File>> getStream(int channelRemoteControl, long duration, File file);

  String getUnixDomainSocketPath(int channelRemoteControl);
}
