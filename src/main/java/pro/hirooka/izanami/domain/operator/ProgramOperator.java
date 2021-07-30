package pro.hirooka.izanami.domain.operator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.epg.ChannelConfiguration;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.service.common.ISystemService;
import pro.hirooka.izanami.domain.service.epg.IEpgService;
import pro.hirooka.izanami.domain.service.epg.IProgramService;

@Slf4j
@AllArgsConstructor
@Service
public class ProgramOperator implements IProgramOperator {

  private final IProgramService programService;
  private final IEpgService epgService;
  private final ISystemService systemService;

  @Async
  @Override
  public void deleteOldProgramList() {

    if (systemService.isMongoDB()) {
      final Date date = new Date();
      final Instant instant = Instant.ofEpochMilli(date.getTime());
      final ZonedDateTime zonedDateTime =
          ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).minusDays(1);
      final int year = zonedDateTime.getYear();
      final int month = zonedDateTime.getMonthValue();
      final int day = zonedDateTime.getDayOfMonth();
      final ZonedDateTime thresholdZonedDateTime =
          ZonedDateTime.of(
              year, month, day, 0, 0, 0, 0, ZoneId.systemDefault()
          );

      final DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ISO_ZONED_DATE_TIME;
      final String thresholdZonedDateTimeString =
          thresholdZonedDateTime.format(dateTimeFormatter);
      log.info("thresholdZonedDateTime = {}, {}",
          thresholdZonedDateTimeString, thresholdZonedDateTime.toEpochSecond()
      );

      final List<Program> toBeDeletedProgramList =
          programService.deleteByEnd(thresholdZonedDateTime.toEpochSecond() * 1000);
      log.info("toBeDeletedProgramList.size() = {}", toBeDeletedProgramList.size());
      toBeDeletedProgramList.forEach(program -> programService.delete(program.getId()));
    }
  }

  @Override
  public List<Program> getProgramListNow() {

    List<Program> programList = new ArrayList<>();
    boolean hasEpg = false;
    final List<ChannelConfiguration> channelConfigurationList =
        epgService.getChannelConfigurationList();
    if (systemService.isMongoDB()) {
      programList = programService.readByNow(new Date().getTime()).stream()
          .sorted(Comparator.comparing(Program::getChannelRemoteControl))
          .collect(Collectors.toList());
      if (programList != null
          && programList.size() > 0
          && epgService.readLatestEpgAcquisition() != null) {
        hasEpg = true;
      }
    }
    if (!hasEpg) {
      for (ChannelConfiguration channelConfiguration : channelConfigurationList) {
        try {
          Program program = new Program();
          program.setChannelRecording(channelConfiguration.getChannelRecording());
          program.setChannelRemoteControl(channelConfiguration.getChannelRemoteControl());
          assert programList != null;
          programList.add(program);
        } catch (NumberFormatException e) {
          log.error("invalid value : {}", e.getCause().toString());
        }
      }
    }
    assert programList != null;
    programList.sort(Comparator.comparingInt(Program::getChannelRemoteControl));

    return programList;
  }

  @Override
  public List<List<Program>> getOneDayFromNow() {
    final List<List<Program>> listOfProgramList = new ArrayList<>(new ArrayList<>());
    final List<ChannelConfiguration> channelConfigurationList =
        epgService.getChannelConfigurationList();
    for (ChannelConfiguration channelConfiguration : channelConfigurationList) {
      final List<Program> programList =
          programService.getOneDayFromNowByChannelRemoteControl(
              channelConfiguration.getChannelRemoteControl()
          );
      if (programList.size() > 0) {
        listOfProgramList.add(programList);
      }
    }
    return listOfProgramList;
  }
}
