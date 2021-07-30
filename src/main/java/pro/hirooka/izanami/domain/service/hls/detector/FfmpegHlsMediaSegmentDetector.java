package pro.hirooka.izanami.domain.service.hls.detector;

import static java.util.Objects.requireNonNull;
import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;
import static pro.hirooka.izanami.domain.config.Constants.HLS_IV_FILE_EXTENSION;
import static pro.hirooka.izanami.domain.config.Constants.MPEG2_TS_PACKET_LENGTH;
import static pro.hirooka.izanami.domain.config.Constants.STREAM_FILE_NAME_PREFIX;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pro.hirooka.izanami.domain.config.Constants;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.common.type.PlaylistType;
import pro.hirooka.izanami.domain.model.hls.PlaybackModel;
import pro.hirooka.izanami.domain.service.hls.IPlaybackModelManagementComponent;
import pro.hirooka.izanami.domain.service.hls.detector.event.LastMediaSegmentSequenceEvent;
import pro.hirooka.izanami.domain.service.hls.playlist.IPlaylistCreator;

@Slf4j
@Component
public class FfmpegHlsMediaSegmentDetector implements Runnable {

  private String username;
  private final IPlaybackModelManagementComponent izanamiModelManagementComponent;
  private final IPlaylistCreator playlistCreator;
  private final ApplicationEventPublisher applicationEventPublisher;

  public FfmpegHlsMediaSegmentDetector(
      IPlaybackModelManagementComponent izanamiModelManagementComponent,
      IPlaylistCreator playlistCreator,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.izanamiModelManagementComponent = requireNonNull(izanamiModelManagementComponent);
    this.playlistCreator = requireNonNull(playlistCreator);
    this.applicationEventPublisher = requireNonNull(applicationEventPublisher);
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  private Key makeKey(final int keyBit) {
    try {
      final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      keyGenerator.init(keyBit, secureRandom);
      final Key generatedKey = keyGenerator.generateKey();
      return generatedKey;
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Failed to create key.", ex);
    }
  }

  @Override
  public void run() {

    final PlaybackModel playbackModel = izanamiModelManagementComponent.get(username);
    final String streamFileExtension = playbackModel.getStreamFileExtension();
    final int sequenceMediaSegment = playbackModel.getSequenceMediaSegment();
    final boolean canEncrypt = playbackModel.getPlaybackSettings().isCanEncrypt();
    final String mediaPath = playbackModel.getStreamPath();
    final String encryptedMediaTemporaryPath = playbackModel.getTempEncPath();
    log.info("sequenceMediaSegment = {}", sequenceMediaSegment);

    final String commonPath = FILE_SEPARATOR
        + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1) + streamFileExtension;
    final String mediaSegmentPath;
    if (canEncrypt) {
      mediaSegmentPath = encryptedMediaTemporaryPath + commonPath;
    } else {
      mediaSegmentPath = mediaPath + commonPath;
    }
    log.info("mediaSegmentPath = {}", mediaSegmentPath);

    final File mediaSegmentFile = new File(mediaSegmentPath);
    if (mediaSegmentFile.exists()) {
      log.info("file exists: {}", mediaSegmentFile.getAbsolutePath());
      final String nextCommonPath = FILE_SEPARATOR
          + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 2) + streamFileExtension;
      final String nextMediaSegmentPath;
      if (canEncrypt) {
        nextMediaSegmentPath = encryptedMediaTemporaryPath + nextCommonPath;
      } else {
        nextMediaSegmentPath = mediaPath + nextCommonPath;
      }
      final File nextMediaSegmentFile = new File(nextMediaSegmentPath);
      if (nextMediaSegmentFile.exists()) {
        log.info("file exists: {}", nextMediaSegmentFile.getAbsolutePath());
        if (canEncrypt) {
          try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            final int hlsKeyLength = Constants.HLS_KEY_LENGTH;
            final Key key = makeKey(hlsKeyLength);
            final Cipher cipher =
                Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            final RandomStringGenerator randomStringGenerator =
                new RandomStringGenerator.Builder().withinRange('a', 'z').build();
            final String keyPrefix = randomStringGenerator.generate(10);
            final String hlsKeyFileExtension = Constants.HLS_KEY_FILE_EXTENSION;
            final FileOutputStream keyFileOutputStream =
                new FileOutputStream(
                    mediaPath
                        + FILE_SEPARATOR + keyPrefix
                        + (sequenceMediaSegment + 1) + hlsKeyFileExtension
                );

            playbackModel.getKeyArrayList().add(keyPrefix);

            assert key != null;
            final byte[] keyByteArray = key.getEncoded();
            keyFileOutputStream.write(keyByteArray);
            keyFileOutputStream.close();

            final byte[] ivArray = cipher.getIV();

            String ivHex = "";
            for (byte iv : ivArray) {
              final String ivHexTmp = String.format("%02x", iv).toUpperCase();
              ivHex = ivHex + ivHexTmp;
            }

            final String ivPrefix = randomStringGenerator.generate(10);
            final FileWriter ivFileWriter =
                new FileWriter(
                    mediaPath
                        + FILE_SEPARATOR + ivPrefix + (sequenceMediaSegment + 1)
                        + HLS_IV_FILE_EXTENSION
                );
            ivFileWriter.write(ivHex);
            ivFileWriter.close();

            playbackModel.getIvArrayList().add(ivHex);

            if (playbackModel.getFfmpegVcodecType() == FfmpegVcodecType.HEVC_NVENC
                || playbackModel.getFfmpegVcodecType() == FfmpegVcodecType.HEVC_VIDEOTOOLBOX
            ) {
              final Path fmp4InitFileInputPath =
                  FileSystems.getDefault().getPath(encryptedMediaTemporaryPath + FILE_SEPARATOR
                      + STREAM_FILE_NAME_PREFIX + ".mp4");
              final Path fmp4InitFileOutputPath =
                  FileSystems.getDefault().getPath(mediaPath + FILE_SEPARATOR
                      + STREAM_FILE_NAME_PREFIX + ".mp4");
              Files.copy(
                  fmp4InitFileInputPath,
                  fmp4InitFileOutputPath,
                  StandardCopyOption.REPLACE_EXISTING
              );
            }

            final BufferedInputStream bufferedInputStream =
                new BufferedInputStream(
                    new FileInputStream(
                        encryptedMediaTemporaryPath
                            + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1)
                            + streamFileExtension
                    )
                );
            final FileOutputStream fileOutputStream =
                new FileOutputStream(
                    mediaPath
                        + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1)
                        + streamFileExtension
                );
            final CipherOutputStream cipherOutputStream =
                new CipherOutputStream(fileOutputStream, cipher);

            final byte[] buf = new byte[MPEG2_TS_PACKET_LENGTH];

            int ch;
            while ((ch = bufferedInputStream.read(buf)) != -1) {
              cipherOutputStream.write(buf, 0, ch);
            }
            cipherOutputStream.close();
            fileOutputStream.close();
            bufferedInputStream.close();

            // PlaylistType に関わらず，テンポラリディレクトリ内の過去の不要なファイルを削除する．
            final File temporaryTsFile =
                new File(encryptedMediaTemporaryPath + FILE_SEPARATOR
                    + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment) + streamFileExtension);
            if (temporaryTsFile.exists()) {
              temporaryTsFile.delete();
            }
            // LIVE プレイリストの場合は過去の不要なファイルを削除する．
            if (playbackModel.getPlaybackSettings().getPlaylistType() == PlaylistType.LIVE) {
              final int uriInPlaylist = playbackModel.getHlsConfiguration().getUriInPlaylist();
              for (int i = 0; i < sequenceMediaSegment - 3 * uriInPlaylist; i++) {
                final File oldMediaSegmentFile =
                    new File(
                        mediaPath
                            + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + streamFileExtension
                    );
                if (oldMediaSegmentFile.exists()) {
                  oldMediaSegmentFile.delete();
                }
                final File oldKeyFile = new File(
                    mediaPath + FILE_SEPARATOR + keyPrefix + i + hlsKeyFileExtension
                );
                if (oldKeyFile.exists()) {
                  oldKeyFile.delete();
                }
                final File oldIvFile = new File(
                    mediaPath + FILE_SEPARATOR + ivPrefix + i + HLS_IV_FILE_EXTENSION
                );
                if (oldIvFile.exists()) {
                  oldIvFile.delete();
                }
                final File oldEncryptedMediaSegmentTemporaryFile =
                    new File(
                        encryptedMediaTemporaryPath
                            + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + streamFileExtension
                    );
                if (oldEncryptedMediaSegmentTemporaryFile.exists()) {
                  oldEncryptedMediaSegmentTemporaryFile.delete();
                }
              }
            }

          } catch (NoSuchAlgorithmException
              | NoSuchProviderException
              | NoSuchPaddingException
              | InvalidKeyException
              | IOException ex
          ) {
            throw new IllegalStateException("Failed to encrypt stream.", ex);
          }
        } else {
          if (playbackModel.getPlaybackSettings().getPlaylistType() == PlaylistType.LIVE) {
            final int uriInPlaylist = playbackModel.getHlsConfiguration().getUriInPlaylist();
            for (int i = 0; i < sequenceMediaSegment - 3 * uriInPlaylist; i++) {
              final File oldMediaSegmentFile = new File(
                  mediaPath
                      + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + streamFileExtension
              );
              if (oldMediaSegmentFile.exists()) {
                oldMediaSegmentFile.delete();
              }
            }
          }
        }

        final int nextSequenceMediaSegment = sequenceMediaSegment + 1;
        playbackModel.setSequenceMediaSegment(nextSequenceMediaSegment);
        playbackModel.getExtinfList().add(
            (double) playbackModel.getHlsConfiguration().getDuration()
        );
        izanamiModelManagementComponent.update(username, playbackModel);

        //
        final int lastSequenceMediaSegment = playbackModel.getSequenceLastMediaSegment();
        log.info("ls = {}", lastSequenceMediaSegment);
        final PlaylistType playlistType = playbackModel.getPlaybackSettings().getPlaylistType();
        if (playlistType == PlaylistType.LIVE) {
          if (lastSequenceMediaSegment > -1) {
            if (sequenceMediaSegment
                >= lastSequenceMediaSegment
                - (playbackModel.getHlsConfiguration().getUriInPlaylist() - 1)
            ) {
              applicationEventPublisher.publishEvent(
                  new LastMediaSegmentSequenceEvent(this, username)
              );
            }
          }
        }
      } else {
        final int nextSequenceMediaSegment = sequenceMediaSegment + 1;
        playbackModel.setSequenceMediaSegment(nextSequenceMediaSegment);
        playbackModel.getExtinfList().add(
            (double) playbackModel.getHlsConfiguration().getDuration()
        );
        izanamiModelManagementComponent.update(username, playbackModel);
        final int lastSequenceMediaSegment = playbackModel.getSequenceLastMediaSegment();
        log.info("ls = {}", lastSequenceMediaSegment);
      }

    } else {
      final int lastSequenceMediaSegment = playbackModel.getSequenceLastMediaSegment();
      log.info("ls = {}", lastSequenceMediaSegment);
      if (lastSequenceMediaSegment > -1) {
        applicationEventPublisher.publishEvent(
            new LastMediaSegmentSequenceEvent(this, username)
        );
      }
    }
    playlistCreator.create(username);

  }

}
