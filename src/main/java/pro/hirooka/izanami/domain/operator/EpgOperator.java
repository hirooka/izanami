package pro.hirooka.izanami.domain.operator;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.service.common.ITsServerClientService;
import pro.hirooka.izanami.domain.service.epg.IEpgService;

@Slf4j
@AllArgsConstructor
@Service
public class EpgOperator implements IEpgOperator {

  private final IEpgService epgService;
  private final ITsServerClientService tsServerClientService;

  @Async
  @Override
  public void persist() {
    log.info("save programs to database...");
    epgService
        .getChannelConfigurationList()
        .forEach(channelConfiguration -> {
          List<Program> programList =
              tsServerClientService
                  .getProgramListByChannelRemoteControl(
                      channelConfiguration.getChannelRemoteControl()
                  );
          programList.forEach(epgService::create);
        });

    // TODO: simple
    final LatestEpgAcquisition previousLatestEpgAcquisition = epgService.readLatestEpgAcquisition();
    final LatestEpgAcquisition newLatestEpgAcquisition;
    final Date date = new Date();
    if (previousLatestEpgAcquisition == null) {
      newLatestEpgAcquisition = new LatestEpgAcquisition();
      newLatestEpgAcquisition.setDate(date.getTime());
      newLatestEpgAcquisition.setUnique(1);
    } else {
      previousLatestEpgAcquisition.setDate(date.getTime());
      newLatestEpgAcquisition = previousLatestEpgAcquisition;
    }
    final LatestEpgAcquisition updatedLatestEpgAcquisition =
        epgService.updateLatestEpgAcquisition(newLatestEpgAcquisition);
    log.info("updatedLatestEpgAcquisition = {}", updatedLatestEpgAcquisition.getDate());
  }

  @Override
  public LatestEpgAcquisition readLatestEpgAcquisition() {
    return epgService.readLatestEpgAcquisition();
  }
}
