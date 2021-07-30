package pro.hirooka.izanami.domain.service.filer;

import java.util.List;
import pro.hirooka.izanami.domain.model.common.M4vFile;
import pro.hirooka.izanami.domain.model.common.VideoFile;
import pro.hirooka.izanami.domain.model.common.type.M4vType;

public interface IVideoFileService {
  List<VideoFile> getAll();

  List<M4vFile> getAllM4v(M4vType m4vType);

  List<VideoFile> getVideoFileList();
}
