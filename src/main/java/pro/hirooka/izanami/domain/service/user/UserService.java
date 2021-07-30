package pro.hirooka.izanami.domain.service.user;

import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_PASSWORD;
import static pro.hirooka.izanami.domain.config.Constants.DEFAULT_USERNAME;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.aaa.AaaConfiguration;
import pro.hirooka.izanami.domain.model.user.User;
import pro.hirooka.izanami.domain.model.user.UserRole;
import pro.hirooka.izanami.domain.repository.user.UserRepository;

@Slf4j
@AllArgsConstructor
@Service
public class UserService implements IUserService {

  private final AaaConfiguration aaaConfiguration;
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findOneByUsername(username);
  }

  @Override
  public void createInitialUser() {

    log.info("create initial user");

    final UserRole adminUserRole = new UserRole();
    adminUserRole.setName("ADMIN");
    adminUserRole.setAuthority("ROLE_ADMIN");

    final UserRole guestUserRole = new UserRole();
    guestUserRole.setName("GUEST");
    guestUserRole.setAuthority("ROLE_GUEST");

    final List<UserRole> userRoleList = new ArrayList<>();
    userRoleList.add(adminUserRole);
    userRoleList.add(guestUserRole);

    final String username;
    if (aaaConfiguration.getInitialUsername().equals("")) {
      username = DEFAULT_USERNAME;
    } else {
      username = aaaConfiguration.getInitialUsername();
    }
    final String password;
    if (aaaConfiguration.getInitialPassword().equals("")) {
      password = DEFAULT_PASSWORD;
    } else {
      password = aaaConfiguration.getInitialPassword();
    }
    final User user = new User();
    user.setUsername(username);
    final PasswordEncoder passwordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder();
    user.setPassword(passwordEncoder.encode(password));
    user.setUserRoleList(userRoleList);
    userRepository.save(user);
  }

  @Override
  public List<User> readAllUserDetails() {
    return userRepository.findAll();
  }
}
