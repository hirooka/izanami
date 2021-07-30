package pro.hirooka.izanami.domain.activity;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.operator.IProgramOperator;

@Slf4j
@AllArgsConstructor
@Service
public class ProgramActivity implements IProgramActivity {

  private final IProgramOperator programOperator;

  @PostConstruct
  void init() {
    programOperator.deleteOldProgramList();
  }

  @Scheduled(cron = "${epg.old-program-deletion-schedule-cron}")
  void cron() {
    log.info("cron -----> ");
    programOperator.deleteOldProgramList();
  }

  @Override
  public List<Program> getProgramListNow() {
    return programOperator.getProgramListNow();
  }
}
