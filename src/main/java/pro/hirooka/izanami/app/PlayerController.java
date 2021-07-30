package pro.hirooka.izanami.app;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.LIVE_PATH_NAME;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_NAME;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_ROOT_PATH_NAME;

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.izanami.domain.activity.IProgramActivity;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.StreamingType;
import pro.hirooka.izanami.domain.model.app.Html5Player;
import pro.hirooka.izanami.domain.model.common.VideoFile;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.model.hls.PlaybackSettings;
import pro.hirooka.izanami.domain.operator.IFileOperator;
import pro.hirooka.izanami.domain.operator.IHlsOperator;
import pro.hirooka.izanami.domain.service.common.ISystemService;

@Slf4j
@AllArgsConstructor
@RequestMapping("izanami")
@Controller
public class PlayerController {

  private final ServletContext servletContext;
  private final HttpServletRequest httpServletRequest;
  private final IHlsOperator hlsOperator;
  private final IFileOperator fileOperator;
  private final ISystemService systemService;
  private final IProgramActivity programActivity;

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

  @GetMapping("")
  public String index(final Model model) {

    final boolean hasWebcam = systemService.isWebCamera();
    model.addAttribute("isWebCamera", hasWebcam);

    final Html5Player html5Player = new Html5Player();
    html5Player.setPlaylistUri("");
    model.addAttribute("html5Player", html5Player);

    // TODO: improve
    final List<Program> programList = programActivity.getProgramListNow();
    final boolean isPTxByChannel = ObjectUtils.isEmpty(programList.get(0).getTitle());
    final boolean isPTxByProgram = !isPTxByChannel;
    model.addAttribute("programList", programList);
    model.addAttribute("isPTxByChannel", isPTxByChannel);
    model.addAttribute("isPTxByProgram", isPTxByProgram);

    final List<VideoFile> videoFileList = fileOperator.getVideoFileList();
    model.addAttribute("videoFileModelList", videoFileList);

    return "izanami";
  }

  @PostMapping("")
  String play(
      final Model model,
      @RequestBody @Validated final PlaybackSettings playbackSettings,
      final BindingResult bindingResult
  ) {
    log.info("[START] {}", playbackSettings.toString());

    if (bindingResult.hasErrors()) {
      return "index";
    }

    final String userAgent = httpServletRequest.getHeader("user-agent");
    log.info("userAgent: {}", userAgent);

    //final String servletRealPath =
    //    httpServletRequest.getSession().getServletContext().getRealPath("");
    final String servletRealPath = servletContext.getRealPath("");
    log.info("servletRealPath = {}", servletRealPath);

    final PlaybackModel playbackModel =
        hlsOperator.startPlayback(playbackSettings, userAgent, servletRealPath);
    final String playlistUri = buildM3u8Uri(playbackModel);
    if (playlistUri.equals("/")) {
      return "index";
    }

    final Html5Player html5PlayerModel = new Html5Player();
    html5PlayerModel.setPlaylistUri(playlistUri);
    model.addAttribute("html5Player", html5PlayerModel);

    return "embedded-"
        + systemService.getEmbeddedPlayerType(userAgent).name().toLowerCase()
        + "-player";
  }

  @GetMapping("stop")
  String stop() {
    log.info("[STOP]");
    hlsOperator.stopPlayback();
    return "redirect:/izanami/remove";
  }

  @GetMapping("remove")
  String remove(final Model model) {
    log.info("[REMOVE]");
    // TODO: ファイル削除前にモデル削除されている
    hlsOperator.removeStream();

    final Html5Player html5PlayerModel = new Html5Player();
    html5PlayerModel.setPlaylistUri("");
    model.addAttribute("html5Player", html5PlayerModel);

    final String userAgent = httpServletRequest.getHeader("user-agent");
    return "embedded-"
        + systemService.getEmbeddedPlayerType(userAgent).name().toLowerCase()
        + "-player";
  }
}
