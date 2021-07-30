package pro.hirooka.izanami.domain.activity;

import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.operator.IRecorderOperator;

@Slf4j
@AllArgsConstructor
@Service
public class RecorderActivity implements IRecorderActivity {

  private final IRecorderOperator recorderOperator;

  @PostConstruct
  void init() {
    // TODO:
    //recorderOperator.onBoot();
  }

  @Override
  public ReservedProgram create(final ReservedProgram reservedProgram) {
    return recorderOperator.create(reservedProgram);
  }
}
