package pro.hirooka.izanami.app;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.izanami.domain.model.common.VideoFile;
import pro.hirooka.izanami.domain.service.filer.IVideoFileService;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("files")
public class VideoFileController {

  private final IVideoFileService videoFileService;

  @GetMapping()
  String getAll(final Model model) {
    final List<VideoFile> videoFileList = videoFileService.getAll();
    model.addAttribute("videoFileList", videoFileList);
    return "files/list";
  }

  @PostMapping("transcode")
  String create(@Validated final VideoFile videoFile, final Model model) {
    final int i = 0;
    return "files/list";
  }
}
