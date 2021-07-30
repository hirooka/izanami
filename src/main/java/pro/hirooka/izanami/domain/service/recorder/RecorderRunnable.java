package pro.hirooka.izanami.domain.service.recorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.model.tuner.Tuner;

@Slf4j
public class RecorderRunnable implements Runnable {

  private ReservedProgram reservedProgram;
  private Tuner tuner;
  private String tsServerUri;

  @Override
  public void run() {

    final int channelRecording = reservedProgram.getChannelRecording();
    final long startRecording = reservedProgram.getStartRecording();
    final long stopRecording = reservedProgram.getStopRecording();
    final long duration = reservedProgram.getRecordingDuration();
    final long thumbnailPoint = duration / 3;
    final String title = reservedProgram.getTitle();
    final String fileName = reservedProgram.getFileName();

    log.info("start recording... [{}] {}", channelRecording, title);

    final File file = new File(fileName);
    final RestTemplate restTemplate = new RestTemplate();
    final ResponseEntity<File> responseEntity =
        restTemplate.execute(
            tsServerUri,
            HttpMethod.GET,
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
    log.info("{}", responseEntity.getStatusCode());
  }

  public ReservedProgram getReservedProgram() {
    return reservedProgram;
  }

  public void setReservedProgram(ReservedProgram reservedProgram) {
    this.reservedProgram = reservedProgram;
  }

  public Tuner getTuner() {
    return tuner;
  }

  public void setTuner(Tuner tuner) {
    this.tuner = tuner;
  }

  public String getTsServerUri() {
    return tsServerUri;
  }

  public void setTsServerUri(final String tsServerUri) {
    this.tsServerUri = tsServerUri;
  }
}
