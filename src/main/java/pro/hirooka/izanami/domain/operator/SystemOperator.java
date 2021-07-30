package pro.hirooka.izanami.domain.operator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.service.common.ISystemService;

@Slf4j
@AllArgsConstructor
@Service
public class SystemOperator implements ISystemOperator {

  private final ISystemService systemService;

  @Override
  public boolean hasFfmpeg() {
    return systemService.isFFmpeg();
  }

  @Override
  public boolean hasMongoDb() {
    return systemService.isFFmpeg();
  }

  @Override
  public boolean hasWebcam() {
    return systemService.isWebCamera();
  }
}
