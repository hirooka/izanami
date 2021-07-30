package pro.hirooka.izanami.api.v1;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.model.common.M4vFile;
import pro.hirooka.izanami.domain.model.common.type.M4vType;
import pro.hirooka.izanami.domain.service.filer.IVideoFileService;

@Slf4j
@AllArgsConstructor
@RequestMapping("api/v1/m4v")
@RestController
public class M4vRestController {

  private final SystemConfiguration systemConfiguration;
  private final IVideoFileService videoFileService;

  @PostMapping(produces = "video/mp4")
  Resource downloadFile(@RequestBody @Validated final M4vFile m4vFile) {

    String filePath = "";

    switch (m4vFile.getType()) {
      case PHONE:
      case PAD:
        filePath = systemConfiguration.getFilePath()
            + FILE_SEPARATOR + m4vFile.getName() + ".m4v";
        break;
      case WATCH:
        filePath = systemConfiguration.getFilePath()
            + FILE_SEPARATOR + m4vFile.getName() + ".watch.m4v";
        break;
      default:
        break;
    }

    return new FileSystemResource(filePath);
  }

  @GetMapping("{name}")
  Resource downloadFile(@PathVariable final String name) {
    final String filePath =
        systemConfiguration.getFilePath() + FILE_SEPARATOR + name + ".watch.m4v";
    return new FileSystemResource(filePath);
  }

  @GetMapping("watch")
  List<M4vFile> readAllWatchM4v() {
    return videoFileService.getAllM4v(M4vType.WATCH);
  }

  @GetMapping("phone")
  List<M4vFile> readAllPhoneM4v() {
    return videoFileService.getAllM4v(M4vType.PHONE);
  }

  @GetMapping("watch/{name}")
  Resource downloadWatchM4v(@PathVariable final String name) {
    final String filePath =
        systemConfiguration.getFilePath() + FILE_SEPARATOR + name + ".watch.m4v";
    return new FileSystemResource(filePath);
  }
}
