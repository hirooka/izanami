package pro.hirooka.izanami.api.v1;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.izanami.domain.model.Hello;

@RequestMapping("api/hello")
@RestController
public class HelloRestController {

  @GetMapping("")
  public Hello hello() {
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    final Instant instant = Instant.now();
    final Hello hello = new Hello();
    hello.setDate(instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter));
    hello.setEpoch(instant.toEpochMilli());
    return hello;
  }
}
