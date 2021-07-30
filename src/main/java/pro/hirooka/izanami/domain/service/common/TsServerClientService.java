package pro.hirooka.izanami.domain.service.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import pro.hirooka.izanami.domain.config.common.TsServerConfiguration;
import pro.hirooka.izanami.domain.model.epg.Program;

@Slf4j
@AllArgsConstructor
@Service
public class TsServerClientService implements ITsServerClientService {

  private final TsServerConfiguration tsServerConfiguration;

  @Override
  public List<Program> getProgramListNow() {
    final String uri = getTsServerUri("/programs/now");
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<Program[]> responseEntity =
        restTemplate.getForEntity(uri, Program[].class);
    return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
  }

  @Override
  public List<Program> getProgramListByChannelRecording(int channelRecording) {
    final String uri = getTsServerUri("/programs/" + channelRecording);
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<Program[]> responseEntity =
        restTemplate.getForEntity(uri, Program[].class);
    return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
  }

  @Override
  public List<Program> getProgramListByChannelRemoteControl(int channelRemoteControl) {
    final String uri = getTsServerUri("/programs/" + channelRemoteControl);
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<Program[]> responseEntity =
        restTemplate.getForEntity(uri, Program[].class);
    return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
  }

  @Override
  public Program getProgramByChannelRecordingNow(int channelRecording) {
    final String uri = getTsServerUri("/programs/" + channelRecording + "/now");
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<Program> responseEntity =
        restTemplate.getForEntity(uri, Program.class);
    return responseEntity.getBody();
  }

  @Override
  public Program getProgramByChannelRemoteControlNow(int channelRemoteControl) {
    final String uri = getTsServerUri("/programs/" + channelRemoteControl + "/now");
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<Program> responseEntity =
        restTemplate.getForEntity(uri, Program.class);
    return responseEntity.getBody();
  }

  @Async
  @Override
  public Future<ResponseEntity<File>> getStream(
      int channelRemoteControl, long duration, File file
  ) {
    final String uri = getTsServerUri("/streams" + "/" + channelRemoteControl);
    final RestTemplate restTemplate = new RestTemplate();
    //restTemplate.getInterceptors()
    // .add(new BasicAuthenticationInterceptor(
    // tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword()
    // ));
    ResponseEntity<File> responseEntity =
        restTemplate.execute(
            uri, HttpMethod.GET,
            null,
            new ResponseExtractor<ResponseEntity<File>>() {
              @Override
              public ResponseEntity<File> extractData(
                  ClientHttpResponse response
              ) throws IOException {
                FileCopyUtils.copy(response.getBody(), new FileOutputStream(file));
                return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders()).body(file);
              }
            });
    return new AsyncResult<>(responseEntity);
  }

  @Override
  public String getUnixDomainSocketPath(int channelRemoteControl) {
    final String uri = getTsServerUri("/streams" + "/" + channelRemoteControl);
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthenticationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword())
        );
    final ResponseEntity<String> responseEntity =
        restTemplate.getForEntity(uri, String.class);
    return responseEntity.getBody().toString();
  }

  private String getTsServerUri(String path) {
    final String username = tsServerConfiguration.getUsername();
    final String password = tsServerConfiguration.getPassword();
    final String scheme = tsServerConfiguration.getScheme().name().toLowerCase();
    final String host = tsServerConfiguration.getHost();
    final int port = tsServerConfiguration.getPort();
    final String uri = scheme.toLowerCase() + "://"
        + username + ":" + password + "@"
        + host + ":" + port
        + "/api/v1" + path;
    return uri;
  }

}
