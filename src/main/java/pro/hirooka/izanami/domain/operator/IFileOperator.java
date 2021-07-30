package pro.hirooka.izanami.domain.operator;

import java.util.List;
import pro.hirooka.izanami.domain.model.common.VideoFile;

public interface IFileOperator {
  List<VideoFile> getVideoFileList();
}
