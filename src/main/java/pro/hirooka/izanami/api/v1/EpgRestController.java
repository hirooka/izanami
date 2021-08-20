package pro.hirooka.izanami.api.v1;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.izanami.domain.model.Hello;
import pro.hirooka.izanami.domain.operator.IEpgOperator;

@Slf4j
@AllArgsConstructor
@RequestMapping("api/v1/epg")
@RestController
public class EpgRestController {

  private final IEpgOperator epgOperator;

  @GetMapping("")
  public Hello persist() {

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    final Instant instant = Instant.now();
    final Hello hello = new Hello();
    hello.setDate(instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter));
    hello.setEpoch(instant.toEpochMilli());
    epgOperator.persist();
    return hello;
  }

}
