package pro.hirooka.izanami.domain.activity;

import java.util.List;
import pro.hirooka.izanami.domain.model.epg.Program;

public interface IProgramActivity {
  List<Program> getProgramListNow();
}
