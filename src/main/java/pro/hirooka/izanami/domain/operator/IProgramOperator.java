package pro.hirooka.izanami.domain.operator;

import java.util.List;
import pro.hirooka.izanami.domain.model.epg.Program;

public interface IProgramOperator {
  void deleteOldProgramList();

  List<Program> getProgramListNow();

  List<List<Program>> getOneDayFromNow();
}
