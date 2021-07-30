package pro.hirooka.izanami.app;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.izanami.domain.operator.IEpgOperator;

@Slf4j
@AllArgsConstructor
@RequestMapping("epg")
@Controller
public class EpgController {

  private final IEpgOperator epgOperator;

  @GetMapping("")
  public String index() {
    epgOperator.persist();
    return "index";
  }
}
