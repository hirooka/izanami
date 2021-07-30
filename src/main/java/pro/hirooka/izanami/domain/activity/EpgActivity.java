package pro.hirooka.izanami.domain.activity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.epg.EpgConfiguration;
import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;
import pro.hirooka.izanami.domain.operator.IEpgOperator;

@Slf4j
@AllArgsConstructor
@Service
public class EpgActivity implements IEpgActivity {

  private final EpgConfiguration epgConfiguration;
  private final IEpgOperator epgOperator;

  @PostConstruct
  void init() {
    log.info("EpgActivity init...");
    // TODO: if isMongoDB
    LatestEpgAcquisition latestEpgAcquisition = epgOperator.readLatestEpgAcquisition();
    if (latestEpgAcquisition == null) {
      epgOperator.persist();
    } else {
      final long now = Instant.now().toEpochMilli();
      final long latest = latestEpgAcquisition.getDate();
      log.info("now: {}, latest: {}",
          Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
          Instant.ofEpochMilli(latest).atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
      );
      final long diff = now - latest;
      if (diff > epgConfiguration.getAcquisitionOnBootIgnoredInterval()) {
        epgOperator.persist();
      } else {
        log.info("epgOperator: diff < interval");
      }
    }
  }

  @Scheduled(cron = "${epg.acquisition-schedule-cron}")
  void cron() {
    log.info("cron -----> ");
    // TODO: if isMongoDB
    epgOperator.persist();
  }
}
