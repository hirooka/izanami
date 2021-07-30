package pro.hirooka.izanami.domain.operator;

import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_TS_SERVER_PASSWORD;
import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_TS_SERVER_USERNAME;
import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.config.common.TsServerConfiguration;
import pro.hirooka.izanami.domain.config.recorder.RecorderConfiguration;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.service.common.ISystemService;
import pro.hirooka.izanami.domain.service.epg.IEpgService;
import pro.hirooka.izanami.domain.service.recorder.IRecorderService;
import pro.hirooka.izanami.domain.service.recorder.RecorderRunnable;

@Slf4j
@AllArgsConstructor
@Service
public class RecorderOperator implements IRecorderOperator {

  private final RecorderConfiguration recorderConfiguration;
  private final TsServerConfiguration tsServerConfiguration;
  private final SystemConfiguration systemConfiguration;
  private final IEpgService epgService;
  private final IRecorderService recorderService;
  private final ISystemService systemService;

  @Async
  @Override
  public void onBoot() {
    if (systemService.isMongoDB()) {
      log.info("recorder: on boot...");
      final List<ReservedProgram> reservedProgramList = recorderService.read();
      for (ReservedProgram reservedProgram : reservedProgramList) {
        if (true) { // TODO:check
          final long startRecording = reservedProgram.getStartRecording();
          final long stopRecording = reservedProgram.getStopRecording();
          final long now = new Date().getTime();
          if (startRecording > now && stopRecording > now) {
            // reserve
            log.info("reservation: {}", reservedProgram.toString());
            final RecorderRunnable recorderRunnable = new RecorderRunnable();
            recorderRunnable.setReservedProgram(reservedProgram);
            final String path = "/streams/"
                + epgService.getTunerType(reservedProgram.getChannelRecording())
                + "/"
                + reservedProgram.getChannelRecording()
                + "/"
                + reservedProgram.getRecordingDuration();
            final String tsServerUri = getTsServerUri(path);
            recorderRunnable.setTsServerUri(tsServerUri);
            recorderService.reserve(recorderRunnable);
          } else if (now > startRecording && stopRecording > now) {
            // start recording immediately
            log.info("no reservation, direct recording");
            long recordingDuration = (stopRecording - now) / 1000;
            reservedProgram.setRecordingDuration(recordingDuration);
            final String path = "/streams/"
                + epgService.getTunerType(reservedProgram.getChannelRecording())
                + "/"
                + reservedProgram.getChannelRecording()
                + "/"
                + reservedProgram.getRecordingDuration();
            final String tsServerUri = getTsServerUri(path);
            recorderService.recordDirectly(reservedProgram, tsServerUri);

          } else if (now > startRecording && now > stopRecording) {
            //  nothing to do... (as error)
            log.error("no reservation, no recording");
          } else {
            log.error("");
          }
        } else {
          log.info("skip (in recording...) {}", reservedProgram.toString());
        }
      }
    }
  }

  @Override
  public ReservedProgram create(final ReservedProgram reservedProgram) {

    final List<ReservedProgram> reservedProgramList = recorderService.read();
    if (reservedProgramList.size() > 0) {
      int n = Collections.max(reservedProgramList.stream()
          .map(ReservedProgram::getId).collect(Collectors.toList()));
      n++;
      reservedProgram.setId(n);
    } else {
      reservedProgram.setId(0);
    }

    final long startRecording =
        reservedProgram.getBegin() - recorderConfiguration.getStartMargin() * 1000;
    final long stopRecording =
        reservedProgram.getEnd() + recorderConfiguration.getStopMargin() * 1000;
    long recordingDuration = (stopRecording - startRecording) / 1000;
    reservedProgram.setStartRecording(startRecording);
    reservedProgram.setStopRecording(stopRecording);
    reservedProgram.setRecordingDuration(recordingDuration);

    final String fileName = systemConfiguration.getFilePath() + FILE_SEPARATOR
        + reservedProgram.getChannelRemoteControl() + "_" + reservedProgram.getBegin()
        + "_" + reservedProgram.getTitle() + ".ts";
    reservedProgram.setFileName(fileName);

    final long now = Instant.now().toEpochMilli();

    if (startRecording > now && stopRecording > now) {
      // reserve
      log.info("reservation: {}", reservedProgram.toString());
      final RecorderRunnable recorderRunnable = new RecorderRunnable();
      recorderRunnable.setReservedProgram(reservedProgram);
      final String path = "/streams"
          + "/"
          + reservedProgram.getChannelRemoteControl()
          + "/"
          + reservedProgram.getRecordingDuration();
      final String tsServerUri = getTsServerUri(path);
      recorderRunnable.setTsServerUri(tsServerUri);
      recorderService.reserve(recorderRunnable);
    } else if (now > startRecording && stopRecording > now) {
      // start recording immediately
      log.info("no reservation, direct recording");
      recordingDuration = (stopRecording - now) / 1000;
      reservedProgram.setRecordingDuration(recordingDuration);
      final String path = "/streams"
          + "/"
          + reservedProgram.getChannelRemoteControl()
          + "/"
          + reservedProgram.getRecordingDuration();
      final String tsServerUri = getTsServerUri(path);
      recorderService.recordDirectly(reservedProgram, tsServerUri);

    } else if (now > startRecording && now > stopRecording) {
      //  nothing to do... (as error)
      log.info("no reservation, no recording");
    } else {
      //
    }

    return null;
  }

  private String getTsServerUri(String path) {
    final String username = tsServerConfiguration.getUsername();
    final String password = tsServerConfiguration.getPassword();
    final String scheme = tsServerConfiguration.getScheme().name().toLowerCase();
    final String host = tsServerConfiguration.getHost();
    final int port = tsServerConfiguration.getPort();
    final String uri = scheme.toLowerCase() + "://"
        + username + ":" + password + "@"
        + host + ":" + port
        + "/api/v1" + path;
    return uri;
  }

}
