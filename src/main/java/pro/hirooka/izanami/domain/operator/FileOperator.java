package pro.hirooka.izanami.domain.operator;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.common.VideoFile;
import pro.hirooka.izanami.domain.service.filer.IVideoFileService;

@Slf4j
@AllArgsConstructor
@Service
public class FileOperator implements IFileOperator {

  private final IVideoFileService videoFileService;

  @Override
  public List<VideoFile> getVideoFileList() {
    return videoFileService.getVideoFileList();
  }
}
