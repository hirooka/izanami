package pro.hirooka.izanami.domain.operator;

import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;

public interface IRecorderOperator {
  void onBoot();

  ReservedProgram create(final ReservedProgram reservedProgram);
}
