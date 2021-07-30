package pro.hirooka.izanami.domain.service.epg;

import java.util.List;
import pro.hirooka.izanami.domain.model.epg.Program;

public interface IProgramService {
  Program create(Program program);

  List<Program> read();

  List<Program> read(int channelRemoteControl);

  List<Program> read(int ch, String beginDate);

  Program read(String id);

  List<Program> readByBeginDate(String beginDate);

  List<Program> readByNow(long now);

  List<Program> readOneDayByNow(long now);

  Program readNow(int ch, long now);

  Program update(Program program);

  void delete(String id);

  List<Program> deleteByEnd(long threshold);

  void deleteAll();

  int getNumberOfPhysicalLogicalChannels();

  List<Program> getOneDayFromNowByChannelRemoteControl(int channelRemoteControl);

  List<List<Program>> getOneDayFromNow();
}
