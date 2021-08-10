package pro.hirooka.izanami.api.v1;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.LIVE_PATH_NAME;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_NAME;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_ROOT_PATH_NAME;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.izanami.domain.config.Constants;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.StreamingType;
import pro.hirooka.izanami.domain.model.Hello;
import pro.hirooka.izanami.domain.model.api.HlsPlaylist;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.model.hls.PlaybackSettings;
import pro.hirooka.izanami.domain.operator.IHlsOperator;

@Slf4j
@AllArgsConstructor
@RequestMapping("api/v1/izanami")
@RestController
public class HlsPlayerRestController {

  private final ServletContext servletContext;
  private final HttpServletRequest httpServletRequest;
  private final IHlsOperator hlsOperator;

  private String buildM3u8Uri(final PlaybackModel playbackModel) {
    final boolean isHevc = playbackModel.getFfmpegVcodecType() == FfmpegVcodecType.HEVC_NVENC;
    final String hevc;
    if (isHevc) {
      hevc = "hevc";
    } else {
      hevc = "h264";
    }
    String m3u8Uri = "/";
    if (playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.WEBCAM)
        || playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.TUNER)) {
      m3u8Uri = "/"
          + STREAM_ROOT_PATH_NAME
          + FILE_SEPARATOR
          + playbackModel.getUuid().toString()
          + FILE_SEPARATOR
          + playbackModel.getAdaptiveBitrateStreaming()
          + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getTranscodingSettings().getName()
          + FILE_SEPARATOR
          + LIVE_PATH_NAME
          + FILE_SEPARATOR
          + M3U8_FILE_NAME + hevc + M3U8_FILE_EXTENSION;
    } else if (playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.FILE)
        || playbackModel.getPlaybackSettings().getStreamingType().equals(StreamingType.OKKAKE)) {
      m3u8Uri = "/"
          + STREAM_ROOT_PATH_NAME
          + FILE_SEPARATOR
          + playbackModel.getUuid().toString()
          + FILE_SEPARATOR
          + playbackModel.getAdaptiveBitrateStreaming()
          + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getTranscodingSettings().getName()
          + FILE_SEPARATOR
          + playbackModel.getPlaybackSettings().getFileName()
          + FILE_SEPARATOR
          + M3U8_FILE_NAME + hevc + M3U8_FILE_EXTENSION;
    }
    log.info(m3u8Uri);
    return m3u8Uri;
  }

  @PostMapping("start")
  HlsPlaylist play(
      @RequestBody @Validated final PlaybackSettings playbackSettings
  ) {
    log.info("[START] {}", playbackSettings.toString());

    final String userAgent = httpServletRequest.getHeader("user-agent");
    log.info("user-agent: {}", userAgent);

    //final String servletRealPath =
    //    httpServletRequest.getSession().getServletContext().getRealPath("");
    final String servletRealPath = servletContext.getRealPath("");
    log.info("servletRealPath = {}", servletRealPath);

    final PlaybackModel playbackModel =
        hlsOperator.startPlayback(playbackSettings, userAgent, servletRealPath);
    final String playlistUri = buildM3u8Uri(playbackModel);
    if (playlistUri.equals("/")) {
      //
    }

    final HlsPlaylist hlsPlaylist = new HlsPlaylist();
    hlsPlaylist.setUri(playlistUri);
    return hlsPlaylist;
  }

  @GetMapping("stop")
  Hello stop() {
    hlsOperator.stopPlayback();
    return remove();
  }

  @GetMapping("remove")
  Hello remove() {
    hlsOperator.removeStream();
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    final Instant instant = Instant.now();
    final Hello hello = new Hello();
    hello.setDate(instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter));
    hello.setEpoch(instant.toEpochMilli());
    return hello;
  }
}
