package pro.hirooka.izanami.domain.service.hls.playlist;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.INITIAL_STREAM_PATH;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.M3U8_FILE_NAME;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_FILE_NAME_PREFIX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.PlaylistType;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.service.hls.IPlaybackModelManagementComponent;

@Slf4j
@AllArgsConstructor
@Component
public class PlaylistCreator implements IPlaylistCreator {

  private final IPlaybackModelManagementComponent izanamiModelManagementComponent;

  public void create(final String username) {

    try {

      final PlaybackModel playbackModel = izanamiModelManagementComponent.get(username);
      final String streamFileExtension = playbackModel.getStreamFileExtension();

      final int uriInPlaylist = playbackModel.getHlsConfiguration().getUriInPlaylist();
      final int targetDuration = playbackModel.getHlsConfiguration().getDuration() + 1;
      final double duration = playbackModel.getHlsConfiguration().getDuration();
      final PlaylistType playlistType = playbackModel.getPlaybackSettings().getPlaylistType();
      final boolean isHevc = playbackModel.getFfmpegVcodecType() == FfmpegVcodecType.HEVC_NVENC;
      final String hevc;
      if (isHevc) {
        hevc = "hevc";
      } else {
        hevc = "h264";
      }
      final String playlistPath =
          playbackModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME
              + hevc + M3U8_FILE_EXTENSION;
      final boolean canEncrypt = playbackModel.getPlaybackSettings().isCanEncrypt();

      final FfmpegVcodecType ffmpegVcodecType = playbackModel.getFfmpegVcodecType();

      final int sequenceMediaSegment = playbackModel.getSequenceMediaSegment();
      final int sequencePlaylist = playbackModel.getSequencePlaylist();
      log.info(
          "sequenceMediaSegment = {}, sequencePlaylist = {}",
          sequenceMediaSegment, sequencePlaylist
      );

      // イニシャルストリームのみか否か。
      // sequenceMediaSegment が 0 以上にならない限りイニシャルストリームを流し続ける。
      if (sequenceMediaSegment >= 0) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(playlistPath))) {

          bufferedWriter.write("#EXTM3U");
          bufferedWriter.newLine();
          bufferedWriter.write("#EXT-X-VERSION:7");
          bufferedWriter.newLine();
          bufferedWriter.write("#EXT-X-TARGETDURATION:" + targetDuration);
          bufferedWriter.newLine();

          // MIX STREAM or ONLY LIVE STREAM
          if (uriInPlaylist - 1 > sequenceMediaSegment) {
            // MIX STREAM
            log.info("MIX STREAM");

            final int initialSequenceInPlaylist = playbackModel.getSequenceInitialPlaylist() + 1;
            playbackModel.setSequenceInitialPlaylist(initialSequenceInPlaylist);

            if (playlistType == PlaylistType.LIVE) {
              bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + initialSequenceInPlaylist);
              bufferedWriter.newLine();
              bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
            } else if (playlistType == PlaylistType.EVENT) {
              bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
            }
            bufferedWriter.newLine();

            // TODO: HardwareAccelerationType -> hls_segment_type
            // /usr/local/bin/ffmpeg -i now_transcoding.ts -acodec aac -ab 160k -ar 48000 -ac 2 -s
            // 1280x720 -vcodec hevc_nvenc -tag:v hvc1 -g 60 -b:v 2560k -threads 1 -f
            // hls -hls_segment_type fmp4 -segment_time 2 i.m3u8
            if (ffmpegVcodecType == FfmpegVcodecType.HEVC_NVENC
                || ffmpegVcodecType == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
            ) {
              bufferedWriter.write("#EXT-X-MAP:URI=\"" + INITIAL_STREAM_PATH + "/init.mp4\"");
              bufferedWriter.newLine();
            }

            if (playlistType == PlaylistType.LIVE) {
              for (int i = initialSequenceInPlaylist;
                   i < initialSequenceInPlaylist + uriInPlaylist - (sequenceMediaSegment + 1);
                   i++
              ) {
                bufferedWriter.write("#EXTINF:" + duration + ",");
                bufferedWriter.newLine();
                bufferedWriter.write(
                    INITIAL_STREAM_PATH + "/" + "i" + i + streamFileExtension
                );
                bufferedWriter.newLine();
              }
            } else if (playlistType == PlaylistType.EVENT) {
              for (int i = 0;
                   i < initialSequenceInPlaylist + uriInPlaylist - (sequenceMediaSegment + 1);
                   i++
              ) {
                bufferedWriter.write("#EXTINF:" + duration + ",");
                bufferedWriter.newLine();
                bufferedWriter.write(
                    INITIAL_STREAM_PATH + "/" + "i" + i + streamFileExtension
                );
                bufferedWriter.newLine();
              }
            }

            bufferedWriter.write("#EXT-X-DISCONTINUITY");
            bufferedWriter.newLine();

            if (ffmpegVcodecType == FfmpegVcodecType.HEVC_NVENC
                || ffmpegVcodecType == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
            ) { //TODO: service
              bufferedWriter.write("#EXT-X-MAP:URI=\"" + STREAM_FILE_NAME_PREFIX + ".mp4\"");
              bufferedWriter.newLine();
            }

            for (int i = 0; i < sequenceMediaSegment + 1; i++) {
              if (canEncrypt) {
                bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                bufferedWriter.write("\"" + "" + playbackModel.getKeyArrayList().get(i) + i
                    + ".key\"" + ",IV=0x");
                bufferedWriter.write(playbackModel.getIvArrayList().get(i));
                bufferedWriter.newLine();
              }
              bufferedWriter.write("#EXTINF:" + playbackModel.getExtinfList().get(i) + ",");
              bufferedWriter.newLine();
              bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + streamFileExtension);
              bufferedWriter.newLine();
            }

          } else {
            // ONLY LIVE STREAM
            log.info("ONLY LIVE STREAM");

            if (playlistType == PlaylistType.LIVE) {
              final int extXMmediaSequence = sequenceMediaSegment - (uriInPlaylist - 1)
                  + playbackModel.getSequenceInitialPlaylist() + 1;
              bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + extXMmediaSequence);
              bufferedWriter.newLine();
              bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
            } else if (playlistType == PlaylistType.EVENT) {
              bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
            }
            bufferedWriter.newLine();

            if (playlistType == PlaylistType.EVENT) {
              final int sequenceInitialPlaylist = playbackModel.getSequenceInitialPlaylist();
              for (int i = 0; i < sequenceInitialPlaylist + 1; i++) {
                bufferedWriter.write("#EXTINF:" + duration + ",");
                bufferedWriter.newLine();
                bufferedWriter.write(
                    INITIAL_STREAM_PATH + "/" + "i" + i + streamFileExtension
                );
                bufferedWriter.newLine();
              }
              bufferedWriter.write("#EXT-X-DISCONTINUITY");
              bufferedWriter.newLine();
            }

            if (ffmpegVcodecType == FfmpegVcodecType.HEVC_NVENC
                || ffmpegVcodecType == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
            ) {
              bufferedWriter.write("#EXT-X-MAP:URI=\"" + STREAM_FILE_NAME_PREFIX + ".mp4\"");
              bufferedWriter.newLine();
            }

            if (playlistType == PlaylistType.LIVE) {
              for (int i = sequenceMediaSegment - (uriInPlaylist - 1);
                   i < sequenceMediaSegment + uriInPlaylist - (uriInPlaylist - 1);
                   i++
              ) {
                if (canEncrypt) {
                  bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                  bufferedWriter.write(
                      "\"" + "" + playbackModel.getKeyArrayList().get(i) + i + ".key\""
                          + ",IV=0x"
                  );
                  bufferedWriter.write(playbackModel.getIvArrayList().get(i));
                  bufferedWriter.newLine();
                }
                bufferedWriter.write("#EXTINF:" + playbackModel.getExtinfList().get(i) + ",");
                bufferedWriter.newLine();
                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + streamFileExtension);
                bufferedWriter.newLine();
              }
            } else if (playlistType == PlaylistType.EVENT) {
              for (int i = 0; i < sequenceMediaSegment + 1; i++) {
                if (canEncrypt) {
                  bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                  bufferedWriter.write(
                      "\"" + "" + playbackModel.getKeyArrayList().get(i) + i + ".key\""
                          + ",IV=0x"
                  );
                  bufferedWriter.write(playbackModel.getIvArrayList().get(i));
                  bufferedWriter.newLine();
                }
                bufferedWriter.write("#EXTINF:" + playbackModel.getExtinfList().get(i) + ",");
                bufferedWriter.newLine();
                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + streamFileExtension);
                bufferedWriter.newLine();
              }
            }
          }
          final int lastSequenceMediaSegment = playbackModel.getSequenceLastMediaSegment();
          if (playlistType == PlaylistType.LIVE) {
            if (lastSequenceMediaSegment > -1) {
              if (sequenceMediaSegment >= lastSequenceMediaSegment - (uriInPlaylist - 1)) {
                bufferedWriter.write("#EXT-X-ENDLIST");
                log.info("end of playlist: {}", lastSequenceMediaSegment);
              }
            }
          } else {
            if (lastSequenceMediaSegment > -1) {
              if (sequenceMediaSegment == lastSequenceMediaSegment) {
                bufferedWriter.write("#EXT-X-ENDLIST");
                log.info("end of playlist: {}", lastSequenceMediaSegment);
              }
            }
          }
        }
        izanamiModelManagementComponent.update(username, playbackModel);

      } else {

        // FFmpeg で生成されるストリームを検出できないため、イニシャルストリームのみを流すプレイリスト

        // ONLY INITIAL STREAM //
        log.info("ONLY INITIAL STREAM");

        final int sequenceInitialPlaylist;
        if (playlistType == PlaylistType.LIVE) {
          sequenceInitialPlaylist = playbackModel.getSequenceInitialPlaylist() + 1;
        } else if (playlistType == PlaylistType.EVENT) {
          sequenceInitialPlaylist = 0;
        } else {
          sequenceInitialPlaylist = 0;
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(playlistPath))) {

          bufferedWriter.write("#EXTM3U");
          bufferedWriter.newLine();
          bufferedWriter.write("#EXT-X-VERSION:7");
          bufferedWriter.newLine();
          bufferedWriter.write("#EXT-X-TARGETDURATION:" + targetDuration);
          bufferedWriter.newLine();
          bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + sequenceInitialPlaylist);
          bufferedWriter.newLine();

          if (ffmpegVcodecType == FfmpegVcodecType.HEVC_NVENC
              || ffmpegVcodecType == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
          ) {
            bufferedWriter.write("#EXT-X-MAP:URI=\"" + INITIAL_STREAM_PATH + "/init.mp4\"");
            bufferedWriter.newLine();
          }

          if (playlistType == PlaylistType.LIVE) {
            for (int i = sequenceInitialPlaylist;
                 i < sequenceInitialPlaylist + uriInPlaylist;
                 i++
            ) {
              bufferedWriter.write("#EXTINF:" + Double.toString(duration) + ",");
              bufferedWriter.newLine();
              bufferedWriter.write(INITIAL_STREAM_PATH + "/" + "i" + i + streamFileExtension);
              bufferedWriter.newLine();
            }
          } else if (playlistType == PlaylistType.EVENT) {
            for (int i = 0; i < sequenceInitialPlaylist + uriInPlaylist; i++) {
              bufferedWriter.write("#EXTINF:" + Double.toString(duration) + ",");
              bufferedWriter.newLine();
              bufferedWriter.write(INITIAL_STREAM_PATH + "/" + "i" + i + streamFileExtension);
              bufferedWriter.newLine();
            }
          } else {
            //
          }
        }

        playbackModel.setSequenceInitialPlaylist(sequenceInitialPlaylist);
        izanamiModelManagementComponent.update(username, playbackModel);
      }

      try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(playlistPath))) {
        String string;
        while ((string = bufferedReader.readLine()) != null) {
          System.out.println(string);
        }
      }

    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read playlist file.", ex);
    }
  }

}

