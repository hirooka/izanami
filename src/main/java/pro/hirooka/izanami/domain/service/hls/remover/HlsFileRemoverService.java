package pro.hirooka.izanami.domain.service.hls.remover;

import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;

@Slf4j
@AllArgsConstructor
@Service
public class HlsFileRemoverService implements IHlsFileRemoverService {

  private final SystemConfiguration systemConfiguration;

  @Async
  @Override
  public void remove(String streamRootPath) {

    String tempPath = systemConfiguration.getTemporaryPath();

    log.info("remove command is called.");
    log.info("streamRootPath: {} and tempPath: {} are to be removed.", streamRootPath, tempPath);

    try {
      FileUtils.cleanDirectory(new File(streamRootPath));
      FileUtils.cleanDirectory(new File(tempPath));
      if ((new File(streamRootPath)).delete() && (new File(tempPath)).delete()) {
        log.info("all Izanami HLS files have been removed completely.");
      } else {
        log.warn("all Izanami HLS files have not been removed completely.");
      }
    } catch (IOException e) {
      log.error("{} {}", e.getMessage(), e);
    }
  }
}
