package pro.hirooka.izanami.domain.operator;

import static java.util.Objects.requireNonNull;
import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.LIVE_PATH_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.config.common.TsServerConfiguration;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.StreamingType;
import pro.hirooka.izanami.domain.config.hls.HlsConfiguration;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.model.hls.PlaybackSettings;
import pro.hirooka.izanami.domain.service.common.ISystemService;
import pro.hirooka.izanami.domain.service.common.ITsServerClientService;
import pro.hirooka.izanami.domain.service.epg.IEpgService;
import pro.hirooka.izanami.domain.service.hls.IPlaybackModelManagementComponent;
import pro.hirooka.izanami.domain.service.hls.detector.IFfmpegHlsMediaSegmentDetectorService;
import pro.hirooka.izanami.domain.service.hls.remover.IHlsFileRemoverService;
import pro.hirooka.izanami.domain.service.transcoder.IFfmpegService;

@Slf4j
@Service
public class HlsOperator implements IHlsOperator {

  private Future<Integer> future;

  private final IPlaybackModelManagementComponent izanamiModelManagementComponent;
  private final TsServerConfiguration tsServerConfiguration;
  private final IFfmpegService ffmpegService;
  private final IFfmpegHlsMediaSegmentDetectorService ffmpegHlsMediaSegmentDetectorService;
  private final IHlsFileRemoverService izanamiHlsFileRemoverService;
  private final ISystemService systemService;
  private final ITsServerClientService tsServerClientService;
  private final IEpgService epgService;
  private final SystemConfiguration systemConfiguration;
  private final HlsConfiguration hlsConfiguration;

  public HlsOperator(
      IPlaybackModelManagementComponent izanamiModelManagementComponent,
      TsServerConfiguration tsServerConfiguration,
      IFfmpegService ffmpegService,
      IFfmpegHlsMediaSegmentDetectorService ffmpegHlsMediaSegmentDetectorService,
      IHlsFileRemoverService izanamiHlsFileRemoverService,
      ISystemService systemService,
      ITsServerClientService tsServerClientService,
      IEpgService epgService,
      SystemConfiguration systemConfiguration,
      HlsConfiguration hlsConfiguration
  ) {
    this.izanamiModelManagementComponent = requireNonNull(izanamiModelManagementComponent);
    this.tsServerConfiguration = requireNonNull(tsServerConfiguration);
    this.ffmpegService = requireNonNull(ffmpegService);
    this.ffmpegHlsMediaSegmentDetectorService =
        requireNonNull(ffmpegHlsMediaSegmentDetectorService);
    this.izanamiHlsFileRemoverService = requireNonNull(izanamiHlsFileRemoverService);
    this.systemService = requireNonNull(systemService);
    this.tsServerClientService = requireNonNull(tsServerClientService);
    this.epgService = requireNonNull(epgService);
    this.systemConfiguration = requireNonNull(systemConfiguration);
    this.hlsConfiguration = requireNonNull(hlsConfiguration);
  }

  private void cancel() {
    izanamiModelManagementComponent.get().forEach(izanamiModel -> {
      final StreamingType streamingType = izanamiModel.getPlaybackSettings().getStreamingType();
      final String username = izanamiModel.getPlaybackSettings().getUsername();
      if (streamingType == StreamingType.WEBCAM
          || streamingType == StreamingType.FILE
          || streamingType == StreamingType.TUNER
      ) {
        ffmpegHlsMediaSegmentDetectorService.cancel(username);
        ffmpegService.cancel(username);
        if (future != null) {
          future.cancel(true);
        }
      } else if (streamingType == StreamingType.OKKAKE) {
        //
      }
    });
  }

  private void stop() {
    izanamiModelManagementComponent.deleteAll();
  }

  private void remove() {
    izanamiModelManagementComponent.get().forEach(izanamiModel -> {
      final String streamPath = izanamiModel.getStreamPath();
      izanamiHlsFileRemoverService.remove(streamPath);
    });
  }

  private void execute() {
    izanamiModelManagementComponent.get().forEach(izanamiModel -> {
      final StreamingType streamingType = izanamiModel.getPlaybackSettings().getStreamingType();
      final String username = izanamiModel.getPlaybackSettings().getUsername();
      log.info("TEST username = {}", username);
      if (streamingType == StreamingType.WEBCAM
          || streamingType == StreamingType.FILE
          || streamingType == StreamingType.TUNER
      ) {
        ffmpegHlsMediaSegmentDetectorService
            .schedule(username, new Date(), 2000);
        if (future != null) {
          future.cancel(true);
        }
        future = ffmpegService.submit(username);
      } else if (streamingType == StreamingType.OKKAKE) {
        //
      }
    });
  }

  private PlaybackModel operateEncodingSettings(final PlaybackModel playbackModel) {
    final String encodingSettings =
        playbackModel.getPlaybackSettings().getTranscodingSettings().getName();
    try {
      final String videoResolution = encodingSettings.split("-")[0];
      final int videoBitrate = Integer.parseInt(encodingSettings.split("-")[1]);
      final int audioBitrate = Integer.parseInt(encodingSettings.split("-")[2]);
      playbackModel.getPlaybackSettings().setVideoResolution(videoResolution);
      playbackModel.getPlaybackSettings().setVideoBitrate(videoBitrate);
      playbackModel.getPlaybackSettings().setAudioBitrate(audioBitrate);
      return playbackModel;
    } catch (NumberFormatException ex) {
      return null;
    } catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }

  private PlaybackModel createIzanamiDirectory(final PlaybackModel playbackModel) {

    String streamRootPath = playbackModel.getStreamRootPath();
    String temporaryPath = playbackModel.getSystemConfiguration().getTemporaryPath();

    String basementStreamPath = "";
    String streamPath = "";
    String temporaryEncryptedStreamPath = "";
    if (playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.TUNER)
        || playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.WEBCAM)
    ) {
      basementStreamPath = playbackModel.getUuid().toString() + FILE_SEPARATOR
          + playbackModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getTranscodingSettings().getName() + FILE_SEPARATOR
          + LIVE_PATH_NAME;
      streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
      playbackModel.setStreamPath(streamPath);
      temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
      playbackModel.setTempEncPath(temporaryEncryptedStreamPath);
    } else if (
        playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.WEBCAM)
    ) {
      streamPath = streamRootPath + FILE_SEPARATOR + LIVE_PATH_NAME;
      playbackModel.setStreamPath(streamPath);
      temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + LIVE_PATH_NAME;
      playbackModel.setTempEncPath(temporaryEncryptedStreamPath);
    } else {
      basementStreamPath = playbackModel.getUuid().toString() + FILE_SEPARATOR
          + playbackModel.getAdaptiveBitrateStreaming() + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getTranscodingSettings().getName() + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getFileName();
      streamPath = streamRootPath + FILE_SEPARATOR + basementStreamPath;
      playbackModel.setStreamPath(streamPath);
      temporaryEncryptedStreamPath = temporaryPath + FILE_SEPARATOR + basementStreamPath;
      playbackModel.setTempEncPath(temporaryEncryptedStreamPath);
    }


    // create directory to deploy segmented MPEG2-TS files (per Video bitrate...)
    if (Files.exists(new File(streamPath).toPath())) {
      try {
        FileUtils.cleanDirectory(new File(streamPath));
        log.info("clean {} as streamPath", streamPath);
      } catch (IOException e) {
        log.error("cannot clean {} as streamPath", streamPath);
        return null;
      }
    } else {
      if (new File(streamPath).mkdirs()) {
        log.info("create {} as streamPath", streamPath);
      } else {
        log.error("cannot create {} as streamPath", streamPath);
      }
    }

    if (Files.exists(new File(temporaryEncryptedStreamPath).toPath())) {
      try {
        FileUtils.cleanDirectory(new File(temporaryEncryptedStreamPath));
        log.info("clean {} as tempEncPath", temporaryEncryptedStreamPath);
      } catch (IOException e) {
        log.error("cannot clean {} as tempEncPath", temporaryEncryptedStreamPath);
      }
    } else {
      if (new File(temporaryEncryptedStreamPath).mkdirs()) {
        log.info("create {} as tempEncPath", temporaryEncryptedStreamPath);
      } else {
        log.error("cannot create {} as tempEncPath", temporaryEncryptedStreamPath);
      }
    }

    return playbackModel;
  }

  private PlaybackModel calculateTimerTaskParameter(PlaybackModel playbackModel) {

    // segmenter timer parameters [ms]
    int duration = playbackModel.getHlsConfiguration().getDuration();
    int uriInPlaylist = playbackModel.getHlsConfiguration().getUriInPlaylist();

    long timerSegmenterDelay = (long) (duration * 1000 * (uriInPlaylist - 1));
    StreamingType streamingType = playbackModel.getPlaybackSettings().getStreamingType();
    if (streamingType.equals(StreamingType.FILE)
        || streamingType.equals(StreamingType.TUNER)
        || streamingType.equals(StreamingType.WEBCAM)) {
      timerSegmenterDelay = (long) (duration * 1000);
    }
    long timerSegmenterPeriod = (long) (duration * 1000);

    // playlister timer parameters [ms]
    long timerPlaylisterDelay = 0;
    long timerPlaylisterPeriod = (long) (duration * 1000);

    log.info(
        "timerSegmenterDelay = {}, "
            + "timerSegmenterPeriod = {}, "
            + "timerPlaylisterDelay = {}, "
            + "timerPlaylisterPeriod = {}",
        timerSegmenterDelay,
        timerSegmenterPeriod,
        timerPlaylisterDelay,
        timerPlaylisterPeriod
    );

    playbackModel.setTimerSegmenterDelay(timerSegmenterDelay);
    playbackModel.setTimerSegmenterPeriod(timerSegmenterPeriod);
    playbackModel.setTimerPlaylisterDelay(timerPlaylisterDelay);
    playbackModel.setTimerPlaylisterPeriod(timerPlaylisterPeriod);
    return playbackModel;
  }

  private PlaybackModel setup(
      final PlaybackSettings playbackSettings,
      final String userAgent,
      final String servletRealPath
  ) {
    final FfmpegVcodecType ffmpegVcodecType = systemService.getFfmpegVcodecType(userAgent);
    if (ffmpegVcodecType == FfmpegVcodecType.UNKNOWN) {
      //
    }
    izanamiModelManagementComponent.deleteAll();

    String unixDomainSocketPath = "";
    if (tsServerConfiguration.isEnabled() && tsServerConfiguration.isUnixDomainSocketEnabled()) {
      unixDomainSocketPath =
          tsServerClientService.getUnixDomainSocketPath(playbackSettings.getChannelRemoteControl());
    }

    PlaybackModel playbackModel = new PlaybackModel();
    playbackModel.setSystemConfiguration(systemConfiguration);
    playbackModel.setHlsConfiguration(hlsConfiguration);
    playbackModel.setUnixDomainSocketPath(unixDomainSocketPath);
    playbackSettings.setTunerType(
        epgService.getTunerType(playbackSettings.getChannelRemoteControl())
    );
    log.info("IzanamiSettings -> {}", playbackSettings);
    playbackModel.setPlaybackSettings(playbackSettings);
    playbackModel.setUuid(UUID.randomUUID());
    playbackModel.setAdaptiveBitrateStreaming(0);
    playbackModel.setFfmpegVcodecType(ffmpegVcodecType);
    // TODO: -> system service
    if (ffmpegVcodecType == FfmpegVcodecType.HEVC_NVENC
        || ffmpegVcodecType == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
    ) {
      playbackModel.setStreamFileExtension(".m4s");
    }

    playbackModel = operateEncodingSettings(playbackModel);
    if (playbackModel == null) {
      //
    }

    final String streamRootPath = systemService.getStreamRootPath(servletRealPath);
    playbackModel.setStreamRootPath(streamRootPath);
    playbackModel = createIzanamiDirectory(playbackModel);
    playbackModel = calculateTimerTaskParameter(playbackModel);

    return izanamiModelManagementComponent.create(playbackSettings.getUsername(), playbackModel);
  }

  @Override
  public PlaybackModel startPlayback(
      final PlaybackSettings playbackSettings,
      final String userAgent,
      final String servletRealPath
  ) {
    cancel();
    stop();
    remove();
    final PlaybackModel playbackModel = setup(playbackSettings, userAgent, servletRealPath);
    execute();
    return playbackModel;
  }

  @Override
  public void stopPlayback() {
    izanamiModelManagementComponent.get().forEach(izanamiModel -> {
      if (tsServerConfiguration.isEnabled()
          && izanamiModel.getPlaybackSettings().getStreamingType() == StreamingType.TUNER
      ) {
        final String username = tsServerConfiguration.getUsername();
        final String password = tsServerConfiguration.getPassword();
        final String scheme = tsServerConfiguration.getScheme().name();
        final String host = tsServerConfiguration.getHost();
        final int port = tsServerConfiguration.getPort();
        final String uri = scheme.toLowerCase() + "://"
            + username + ":" + password + "@"
            + host + ":" + port
            + "/api/v1" + "/streams"
            + "/" + izanamiModel.getPlaybackSettings().getChannelRemoteControl();
        log.info("{}", uri);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(
            new BasicAuthenticationInterceptor(username, password));
        restTemplate.exchange(uri, HttpMethod.DELETE, null, String.class);
      }
    });
    cancel();
    stop();
  }

  @Override
  public void removeStream() {
    remove();
  }
}
