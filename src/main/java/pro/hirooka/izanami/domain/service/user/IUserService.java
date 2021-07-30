package pro.hirooka.izanami.domain.service.user;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;
import pro.hirooka.izanami.domain.model.user.User;

public interface IUserService extends UserDetailsService {
  void createInitialUser();

  List<User> readAllUserDetails();
}
