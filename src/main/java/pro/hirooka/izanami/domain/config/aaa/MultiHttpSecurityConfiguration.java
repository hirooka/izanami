package pro.hirooka.izanami.domain.config.aaa;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pro.hirooka.izanami.domain.service.user.IUserService;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class MultiHttpSecurityConfiguration {

  @Configuration
  @Order(1)
  public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final IUserService userService;
    private final AaaConfiguration aaaConfiguration;

    public ApiWebSecurityConfigurationAdapter(
        Environment environment,
        IUserService userService,
        AaaConfiguration aaaConfiguration
    ) {
      this.environment = requireNonNull(environment);
      this.userService = requireNonNull(userService);
      this.aaaConfiguration = requireNonNull(aaaConfiguration);
    }

    @Override
    protected void configure(
        AuthenticationManagerBuilder authenticationManagerBuilder
    ) throws Exception {
      if (aaaConfiguration.isEnabled()) {
        final List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        final Predicate<String> predicate = profile ->
            (profile.contains("postgresql")
                || profile.contains("mysql")
                || profile.contains("hsqldb")
            );
        if (activeProfiles.stream().anyMatch(profile -> profile.contains("mongodb"))) {
          authenticationManagerBuilder.userDetailsService(this.userService);
        } else if (activeProfiles.stream().anyMatch(predicate)) {
          authenticationManagerBuilder.userDetailsService(this.userService);
          //.passwordEncoder(passwordEncoder()); // TODO:
        } else {
          final PasswordEncoder passwordEncoder =
              PasswordEncoderFactories.createDelegatingPasswordEncoder();
          authenticationManagerBuilder.inMemoryAuthentication().withUser("admin")
              .password(passwordEncoder.encode("admin")).roles("ADMIN");
          authenticationManagerBuilder.inMemoryAuthentication().withUser("user")
              .password(passwordEncoder.encode("user")).roles("USER");
          authenticationManagerBuilder.inMemoryAuthentication().withUser("guest")
              .password(passwordEncoder.encode("guest")).roles("GUEST");
        }
      }
    }

    protected void configure(HttpSecurity http) throws Exception {
      if (aaaConfiguration.isEnabled()) {
        http
            .antMatcher("/api/**")
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf()
            .disable();
      } else {
        http
            .antMatcher("/**")
            .authorizeRequests()
            .anyRequest()
            .permitAll()
            .and()
            .csrf().disable()
            .httpBasic().disable();
      }
    }

    @Override
    public void configure(WebSecurity web) {
      web.ignoring().antMatchers("/images/**", "/webjars/**");
    }
  }

  @Configuration
  public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final IUserService userService;
    private final AaaConfiguration aaaConfiguration;

    public FormLoginWebSecurityConfigurerAdapter(
        Environment environment,
        IUserService userService,
        AaaConfiguration aaaConfiguration
    ) {
      this.environment = requireNonNull(environment);
      this.userService = requireNonNull(userService);
      this.aaaConfiguration = requireNonNull(aaaConfiguration);
    }

    @Override
    protected void configure(
        AuthenticationManagerBuilder authenticationManagerBuilder
    ) throws Exception {
      if (aaaConfiguration.isEnabled()) {
        final List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        final Predicate<String> predicate = profile ->
            (profile.contains("postgresql")
                || profile.contains("mysql")
                || profile.contains("hsqldb")
            );
        if (activeProfiles.stream().anyMatch(profile -> profile.contains("mongodb"))) {
          authenticationManagerBuilder.userDetailsService(this.userService);
        } else if (activeProfiles.stream().anyMatch(predicate)) {
          authenticationManagerBuilder.userDetailsService(this.userService);
          //.passwordEncoder(passwordEncoder()); // TODO:
        } else {
          final PasswordEncoder passwordEncoder =
              PasswordEncoderFactories.createDelegatingPasswordEncoder();
          authenticationManagerBuilder.inMemoryAuthentication().withUser("admin")
              .password(passwordEncoder.encode("admin")).roles("ADMIN");
          authenticationManagerBuilder.inMemoryAuthentication().withUser("user")
              .password(passwordEncoder.encode("user")).roles("USER");
          authenticationManagerBuilder.inMemoryAuthentication().withUser("guest")
              .password(passwordEncoder.encode("guest")).roles("GUEST");
        }
      }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (aaaConfiguration.isEnabled()) {
        http
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/izanami")
            .permitAll()
            .and()
            .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .permitAll();
      } else {
        http
            .antMatcher("/**")
            .authorizeRequests()
            .anyRequest()
            .permitAll()
            .and()
            .csrf().disable()
            .httpBasic().disable();
      }
    }

    @Override
    public void configure(WebSecurity web) {
      web.ignoring().antMatchers("/images/**", "/webjars/**");
    }
  }
}
