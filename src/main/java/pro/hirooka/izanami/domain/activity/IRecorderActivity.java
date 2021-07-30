package pro.hirooka.izanami.domain.activity;

import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;

public interface IRecorderActivity {
  ReservedProgram create(final ReservedProgram reservedProgram);
}
