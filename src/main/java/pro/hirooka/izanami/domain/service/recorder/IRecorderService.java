package pro.hirooka.izanami.domain.service.recorder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;

public interface IRecorderService {
  ReservedProgram create(ReservedProgram reservedProgram);

  List<ReservedProgram> read();

  ReservedProgram read(int id);

  ReservedProgram update(ReservedProgram reservedProgram);

  void delete(int id);

  void deleteAll();

  void recordDirectly(ReservedProgram reservedProgram, String tsServerUri);

  void reserve(RecorderRunnable recorderRunnable);

  void cancel(int id);

  void cancelAll();

  Map<Integer, ScheduledFuture> getScheduledFutureMap();
}
