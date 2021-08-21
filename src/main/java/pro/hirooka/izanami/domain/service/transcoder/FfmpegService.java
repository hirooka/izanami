package pro.hirooka.izanami.domain.service.transcoder;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.FMP4_INIT_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.FMP4_INIT_FILE_NAME;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_NAME;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_FILE_NAME_PREFIX;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import pro.hirooka.izanami.domain.config.Constants;
import pro.hirooka.izanami.domain.config.common.TsServerConfiguration;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.StreamingType;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.service.hls.IPlaybackModelManagementComponent;

@Slf4j
@AllArgsConstructor
@Service
public class FfmpegService implements IFfmpegService {

  private final TsServerConfiguration tsServerConfigurationn;
  private final IPlaybackModelManagementComponent izanamiModelManagementComponent;

  @Async
  @Override
  public Future<Integer> submit(final String username) {

    // TODO: final
    PlaybackModel playbackModel = izanamiModelManagementComponent.get(username);
    final String streamFileExtension = playbackModel.getStreamFileExtension();
    log.debug("StreamPath: {}", playbackModel.getStreamPath());

    final FfmpegVcodecType ffmpegVcodecType = playbackModel.getFfmpegVcodecType();

    final boolean canEncrypt = playbackModel.getPlaybackSettings().isCanEncrypt();
    final String ffmpegOutputPath;
    final String fmp4InitFileOutputPath;
    if (canEncrypt) {
      ffmpegOutputPath = playbackModel.getTempEncPath() + FILE_SEPARATOR
          + STREAM_FILE_NAME_PREFIX + "%d" + streamFileExtension;
      fmp4InitFileOutputPath = playbackModel.getTempEncPath() + FILE_SEPARATOR
          + STREAM_FILE_NAME_PREFIX + ".mp4";
    } else {
      ffmpegOutputPath = playbackModel.getStreamPath() + FILE_SEPARATOR
          + STREAM_FILE_NAME_PREFIX + "%d" + streamFileExtension;
      fmp4InitFileOutputPath = playbackModel.getStreamPath() + FILE_SEPARATOR
          + STREAM_FILE_NAME_PREFIX + ".mp4";
    }
    final String ffmpegM3U8OutputPath;
    if (canEncrypt) {
      ffmpegM3U8OutputPath = playbackModel.getTempEncPath() + FILE_SEPARATOR
          + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
    } else {
      ffmpegM3U8OutputPath = playbackModel.getStreamPath() + FILE_SEPARATOR
          + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
    }

    final String user = tsServerConfigurationn.getUsername();
    final String password = tsServerConfigurationn.getPassword();
    final String scheme = tsServerConfigurationn.getScheme().name();
    final String host = tsServerConfigurationn.getHost();
    final int port = tsServerConfigurationn.getPort();

    final String uri;
    if (tsServerConfigurationn.isEnabled() && tsServerConfigurationn.isUnixDomainSocketEnabled()) {
      uri = "unix:" + playbackModel.getUnixDomainSocketPath();
    } else {
      uri = scheme.toLowerCase() + "://"
          + user + ":" + password + "@" + host + ":" + port
          + "/api/v1" + "/streams"
          + "/" + playbackModel.getPlaybackSettings().getChannelRemoteControl();
      final URI streamUri = UriComponentsBuilder.newInstance()
          .scheme(scheme)
          .host(host)
          .port(port)
          .path("/api/v1/streams/" + playbackModel.getPlaybackSettings().getChannelRemoteControl())
          .build()
          .toUri();
    }

    String[] commandArray = new String[]{};

    final StreamingType streamingType = playbackModel.getPlaybackSettings().getStreamingType();
    if (streamingType.equals(StreamingType.TUNER)) {
      if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", uri,
            "-sn",
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "48000",
            "-ac", "2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_nvenc",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.HEVC_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", uri,
            "-sn",
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "48000",
            "-ac", "2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "hevc_nvenc",
            "-tag:v", "hvc1",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_segment_type", "fmp4",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_fmp4_init_filename", FMP4_INIT_FILE_NAME + FMP4_INIT_FILE_EXTENSION,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_VIDEOTOOLBOX)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", uri,
            "-sn",
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "48000",
            "-ac", "2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_videotoolbox",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.HEVC_VIDEOTOOLBOX)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", uri,
            "-sn",
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "48000",
            "-ac", "2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "hevc_videotoolbox",
            "-tag:v", "hvc1",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_segment_type", "fmp4",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_fmp4_init_filename", FMP4_INIT_FILE_NAME + FMP4_INIT_FILE_EXTENSION,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_V4L2M2M)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", uri,
            "-sn",
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "48000",
            "-ac", "2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_v4l2m2m",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else {
        //
      }
    } else if (streamingType.equals(StreamingType.WEBCAM)) {
      if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-f", "video4linux2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            //"-r", "30",
            "-i", playbackModel.getSystemConfiguration().getWebcamDeviceName(),
            "-f", "alsa",
            "-ac", Integer.toString(playbackModel.getSystemConfiguration().getWebcamAudioChannel()),
            //                        "-i", "hw:0,0",
            "-i", playbackModel.getSystemConfiguration().getWebcamAudioName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "44100",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_nvenc",
            //"-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-pix_fmt", "yuv420p",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.HEVC_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-f", "video4linux2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            //"-r", "30",
            "-i", playbackModel.getSystemConfiguration().getWebcamDeviceName(),
            "-f", "alsa",
            "-ac", Integer.toString(playbackModel.getSystemConfiguration().getWebcamAudioChannel()),
            //                        "-i", "hw:0,0",
            "-i", playbackModel.getSystemConfiguration().getWebcamAudioName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "44100",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "hevc_nvenc",
            "-tag:v", "hvc1",
            //"-vf", "yadif", // fffmpeg 4.4
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-pix_fmt", "yuv420p",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_segment_type", "fmp4",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_fmp4_init_filename", fmp4InitFileOutputPath,
            //"-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_V4L2M2M)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-f", "video4linux2",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            //"-r", "30",
            "-i", playbackModel.getSystemConfiguration().getWebcamDeviceName(),
            "-f", "alsa",
            "-ac", Integer.toString(playbackModel.getSystemConfiguration().getWebcamAudioChannel()),
            //                        "-i", "hw:0,0",
            "-i", playbackModel.getSystemConfiguration().getWebcamAudioName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ar", "44100",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_v4l2m2m",
            //"-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-pix_fmt", "yuv420p",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else {
        //
      }
    } else if (streamingType.equals(StreamingType.FILE)) {
      if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", playbackModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR
            + playbackModel.getPlaybackSettings().getFileName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ac", "2",
            "-ar", "44100",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_nvenc",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.HEVC_NVENC)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", playbackModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR
            + playbackModel.getPlaybackSettings().getFileName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ac", "2",
            "-ar", "48000",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "hevc_nvenc",
            "-tag:v", "hvc1",
            "-vf", "yadif",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_segment_type", "fmp4",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_fmp4_init_filename", fmp4InitFileOutputPath,
            //"-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else if (ffmpegVcodecType.equals(FfmpegVcodecType.H264_V4L2M2M)) {
        commandArray = new String[]{
            playbackModel.getSystemConfiguration().getFfmpegPath(),
            "-i", playbackModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR
            + playbackModel.getPlaybackSettings().getFileName(),
            "-acodec", "aac",
            "-ab", playbackModel.getPlaybackSettings().getAudioBitrate() + "k",
            "-ac", "2",
            "-ar", "44100",
            "-s", playbackModel.getPlaybackSettings().getVideoResolution(),
            "-vcodec", "h264_v4l2m2m",
            "-g", "10",
            "-b:v", playbackModel.getPlaybackSettings().getVideoBitrate() + "k",
            "-threads", Integer.toString(playbackModel.getSystemConfiguration().getFfmpegThreads()),
            "-f", "hls",
            "-hls_time", Integer.toString(playbackModel.getHlsConfiguration().getDuration()),
            "-hls_segment_filename", ffmpegOutputPath,
            ffmpegM3U8OutputPath
        };
      } else {
        //
      }
    } else if (streamingType.equals(StreamingType.OKKAKE)) {
      // not implemented
    } else {
      // not implemented
    }

    String command = "";
    for (int i = 0; i < commandArray.length; i++) {
      command += commandArray[i] + " ";
    }
    log.info("FFmpeg command ================");
    log.info("{}", command);

    final ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
    final Process process;
    try {
      process = processBuilder.start();
      final long pid = process.pid();
      final BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String str = "";
      boolean isTranscoding = false;
      while ((str = bufferedReader.readLine()) != null) {
        log.debug("{}", str);
        if (str.startsWith("frame=")) {
          if (!isTranscoding) {
            isTranscoding = true;
            playbackModel.setTrascoding(isTranscoding);
            playbackModel.setFfmpegPid(pid);
            playbackModel.setFfmpegProcess(process);
            playbackModel =
                izanamiModelManagementComponent.update(username, playbackModel);
          }
        }
      }
      isTranscoding = false;
      playbackModel.setTrascoding(isTranscoding);
      playbackModel.setFfmpegPid(-1);
      playbackModel.setFfmpegProcess(null);
      izanamiModelManagementComponent.update(username, playbackModel);
      process.getInputStream().close();
      process.getErrorStream().close();
      process.getOutputStream().close();
      bufferedReader.close();
      process.destroy();

      if (playbackModel.getPlaybackSettings().getStreamingType() == StreamingType.FILE) {
        int sequenceLastMediaSegment = -1;
        playbackModel = izanamiModelManagementComponent.get(username);
        if (playbackModel.getPlaybackSettings().isCanEncrypt()) {
          String encryptedStreamTemporaryPath = playbackModel.getTempEncPath();
          File[] temporaryfiles = new File(encryptedStreamTemporaryPath).listFiles();
          assert temporaryfiles != null;
          for (File file : temporaryfiles) {
            log.info(file.getName());
            String fileName = file.getName();
            if (fileName.startsWith(Constants.STREAM_FILE_NAME_PREFIX)
                && fileName.endsWith(streamFileExtension)) {
              log.info("... {}", fileName.split(M3U8_FILE_NAME)[1]);
              String sequenceString =
                  fileName.split(M3U8_FILE_NAME)[1].split(streamFileExtension)[0];
              log.info(sequenceString);
              int sequence = Integer.parseInt(sequenceString);
              if (sequence > sequenceLastMediaSegment) {
                sequenceLastMediaSegment = sequence;
              }
            }
          }
        } else {
          String streamPath = playbackModel.getStreamPath();
          File[] files = new File(streamPath).listFiles();
          assert files != null;
          for (File file : files) {
            log.info(file.getName());
            String fileName = file.getName();
            if (fileName.startsWith(Constants.STREAM_FILE_NAME_PREFIX)
                && fileName.endsWith(streamFileExtension)) {
              log.info("... {}", fileName.split(M3U8_FILE_NAME)[1]);
              String sequenceString =
                  fileName.split(M3U8_FILE_NAME)[1].split(streamFileExtension)[0];
              log.info(sequenceString);
              int sequence = Integer.parseInt(sequenceString);
              if (sequence > sequenceLastMediaSegment) {
                sequenceLastMediaSegment = sequence;
              }
            }
          }
        }
        log.info("sequenceLastMediaSegment = {}.", sequenceLastMediaSegment);
        playbackModel.setSeqTsLast(sequenceLastMediaSegment);
        playbackModel.setSequenceLastMediaSegment(sequenceLastMediaSegment);
        izanamiModelManagementComponent.update(username, playbackModel);
        return new AsyncResult<>(sequenceLastMediaSegment);
      }

    } catch (IOException ex) {
      log.warn("{}", ex.getMessage());
    } finally {
      log.info("stream is closed.");
    }
    return new AsyncResult<>(0);
  }

  @Override
  public void execute(final String username) {

  }

  @Override
  public void cancel(final String username) {
    PlaybackModel playbackModel = izanamiModelManagementComponent.get(username);
    playbackModel.setTunerDeviceName("");
    playbackModel.setFfmpegPid(-1);
    if (playbackModel.getFfmpegProcess() != null) {
      playbackModel.getFfmpegProcess().destroy();
      playbackModel.setFfmpegProcess(null);
    }
    izanamiModelManagementComponent.update(username, playbackModel);
  }

}
