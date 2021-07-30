package pro.hirooka.izanami.domain.model.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails {

  private UUID uuid = UUID.randomUUID();

  private String username;
  private String password;
  private List<UserRole> userRoleList;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    final List<String> authorityList =
        userRoleList.stream().map(UserRole::getAuthority).collect(Collectors.toList());
    return AuthorityUtils.createAuthorityList(authorityList.toArray(new String[0]));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<UserRole> getUserRoleList() {
    return userRoleList;
  }

  public void setUserRoleList(List<UserRole> userRoleList) {
    this.userRoleList = userRoleList;
  }
}
