package pro.hirooka.izanami.domain.service.filer;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.common.CommonConfiguration;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.model.common.M4vFile;
import pro.hirooka.izanami.domain.model.common.VideoFile;
import pro.hirooka.izanami.domain.model.common.type.M4vType;

@Slf4j
@AllArgsConstructor
@Service
public class VideoFileService implements IVideoFileService {

  private final CommonConfiguration commonConfiguration;
  private final SystemConfiguration systemConfiguration;

  @Override
  public List<VideoFile> getAll() {
    final List<VideoFile> videoFileList = new ArrayList<>();
    final List<String> extensionList = Arrays.asList(commonConfiguration.getVideoFileExtension());
    final File videoFileDirectory = new File(systemConfiguration.getFilePath());
    final File[] fileArray = videoFileDirectory.listFiles();
    if (fileArray != null) {
      for (final File file : fileArray) {
        for (final String extension : extensionList) {
          if (file.getName().endsWith("." + extension)) {
            final VideoFile videoFile = new VideoFile();
            videoFile.setName(file.getName().split("." + extension)[0]);
            videoFile.setExtension(extension);
            videoFile.setHls(false);
            videoFile.setHlsBitrateList(new ArrayList<>());
            videoFile.setM4v(false);
            videoFile.setWatch(false);
            videoFileList.add(videoFile);
          }
        }
      }
    } else {
      log.warn("'{}' does not exist.", videoFileDirectory);
    }
    for (VideoFile videoFile : videoFileList) {
      final String name = videoFile.getName();
      final File directory =
          new File(systemConfiguration.getFilePath() + FILE_SEPARATOR + name);
      if (directory.exists()) {
        File[] files = directory.listFiles();

      }
    }
    return videoFileList;
  }

  @Override
  public List<M4vFile> getAllM4v(M4vType m4vType) {
    final List<M4vFile> m4vFileList = new ArrayList<>();
    final File videoFileDirectory = new File(systemConfiguration.getFilePath());
    final File[] fileArray = videoFileDirectory.listFiles();
    if (fileArray != null) {
      for (final File file : fileArray) {
        if (m4vType == M4vType.WATCH) {
          if (file.getName().endsWith(".watch.m4v")) {
            final M4vFile m4vFile = new M4vFile();
            m4vFile.setName(file.getName().split(".watch.m4v")[0]);
            m4vFile.setType(m4vType);
            m4vFileList.add(m4vFile);
          }
        } else if (m4vType == M4vType.PHONE) {
          if (file.getName().endsWith(".m4v") && !file.getName().endsWith(".watch.m4v")) {
            final M4vFile m4vFile = new M4vFile();
            m4vFile.setName(file.getName().split(".m4v")[0]);
            m4vFile.setType(m4vType);
            m4vFileList.add(m4vFile);
          }
        } else if (m4vType == M4vType.PAD) {
          if (file.getName().endsWith(".m4v") && !file.getName().endsWith(".watch.m4v")) {
            final M4vFile m4vFile = new M4vFile();
            m4vFile.setName(file.getName().split(".m4v")[0]);
            m4vFile.setType(m4vType);
            m4vFileList.add(m4vFile);
          }
        }
      }
    } else {
      log.warn("'{}' does not exist.", videoFileDirectory);
    }
    return m4vFileList;
  }

  @Override
  public List<VideoFile> getVideoFileList() {
    final List<VideoFile> videoFileList = new ArrayList<>();
    Path path = Paths.get(systemConfiguration.getFilePath());
    if (Files.isSymbolicLink(path)) {
      try {
        path = Files.readSymbolicLink(path);
      } catch (IOException e) {
        log.error(e.getMessage());
      }
    }
    final File fileDirectory = path.toFile();
    final File[] fileArray = fileDirectory.listFiles();
    if (fileArray != null) {
      final String[] videoFileExtensionArray = commonConfiguration.getVideoFileExtension();
      final List<String> videoFileExtensionList = Arrays.asList(videoFileExtensionArray);
      for (File file : fileArray) {
        for (final String videoFileExtension : videoFileExtensionList) {
          if (file.getName().endsWith("." + videoFileExtension)) {
            final VideoFile videoFileModel = new VideoFile();
            videoFileModel.setName(file.getName());
            videoFileList.add(videoFileModel);
          }
        }
      }
    } else {
      log.warn("'{}' does not exist.", fileDirectory);
    }
    videoFileList.sort(Comparator.comparing(VideoFile::getName));

    return videoFileList;
  }
}
