package pro.hirooka.izanami.app;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.izanami.domain.service.user.IUserService;

@Slf4j
@AllArgsConstructor
@RequestMapping("login")
@Controller
public class LoginController {

  private final Environment environment;
  private final IUserService userService;

  @GetMapping("")
  public String login() {
    final List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
    final Predicate<String> predicate = profile ->
        (profile.contains("mongodb")
            || profile.contains("postgresql")
            || profile.contains("mysql")
            || profile.contains("hsqldb")
        );
    if (activeProfiles.stream().anyMatch(predicate)) {
      if (userService.readAllUserDetails().size() == 0) {
        userService.createInitialUser();
      }
    }
    return "login";
  }
}
