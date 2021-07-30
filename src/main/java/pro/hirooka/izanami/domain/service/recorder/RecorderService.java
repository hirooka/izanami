package pro.hirooka.izanami.domain.service.recorder;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import pro.hirooka.izanami.domain.config.common.TsServerConfiguration;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.repository.recorder.IReservedProgramRepository;

@Slf4j
@Service
public class RecorderService implements IRecorderService {

  private final IReservedProgramRepository reservedProgramRepository;
  private final TsServerConfiguration tsServerConfiguration;
  private final MongoTemplate mongoTemplate;

  private Map<Integer, ScheduledFuture> scheduledFutureMap = new HashMap<>();

  public RecorderService(
      IReservedProgramRepository reservedProgramRepository,
      TsServerConfiguration tsServerConfiguration,
      MongoTemplate mongoTemplate
  ) {
    this.reservedProgramRepository = requireNonNull(reservedProgramRepository);
    this.tsServerConfiguration = requireNonNull(tsServerConfiguration);
    this.mongoTemplate = requireNonNull(mongoTemplate);
  }

  @Override
  public ReservedProgram create(ReservedProgram reservedProgram) {
    return reservedProgramRepository.save(reservedProgram);
  }

  @Override
  public List<ReservedProgram> read() {
    return reservedProgramRepository.findAll();
  }

  @Override
  public ReservedProgram read(int id) {
    final Query query =
        new Query(Criteria.where("id").is(id)).with(Sort.by(Sort.Direction.ASC, "id"));
    List<ReservedProgram> reservedProgramList = mongoTemplate.find(query, ReservedProgram.class);
    if (reservedProgramList.size() != 1) {
      log.error("e");
      return null;
    } else {
      return reservedProgramList.get(0);
    }
  }

  @Override
  public ReservedProgram update(ReservedProgram reservedProgram) {
    return reservedProgramRepository.save(reservedProgram);
  }

  @Override
  public void delete(int id) {
    reservedProgramRepository.deleteById(id);
  }

  @Override
  public void deleteAll() {
    reservedProgramRepository.deleteAll();
  }

  @Async
  @Override
  public void recordDirectly(ReservedProgram reservedProgram, String tsServerUri) {

    final int channelRecording = reservedProgram.getChannelRecording();
    final long startRecording = reservedProgram.getStartRecording();
    final long stopRecording = reservedProgram.getStopRecording();
    final long duration = reservedProgram.getRecordingDuration();
    final long thumbnailPoint = duration / 3;
    final String title = reservedProgram.getTitle();
    final String fileName = reservedProgram.getFileName();

    log.info("start recording... [{}] {} --> {}", channelRecording, title, tsServerUri);

    final File file = new File(fileName);
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors()
        .add(new BasicAuthorizationInterceptor(
            tsServerConfiguration.getUsername(), tsServerConfiguration.getPassword()
        ));
    ResponseEntity<File> responseEntity = restTemplate.execute(
        tsServerUri,
        HttpMethod.GET,
        null,
        new ResponseExtractor<ResponseEntity<File>>() {
          @Override
          public ResponseEntity<File> extractData(ClientHttpResponse response) throws IOException {
            FileCopyUtils.copy(response.getBody(), new FileOutputStream(file));
            return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders()).body(file);
          }
        });
    log.info("{}", responseEntity.getStatusCode());
  }

  @Override
  public void reserve(RecorderRunnable recorderRunnable) {
    final ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor();
    final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(scheduledExecutorService);
    final Date date = new Date(recorderRunnable.getReservedProgram().getStart());
    final ScheduledFuture scheduledFuture = taskScheduler.schedule(recorderRunnable, date);
    scheduledFutureMap.put(recorderRunnable.getReservedProgram().getId(), scheduledFuture);
    log.info("scheduler: {}", date.toString());
  }

  @Override
  public void cancel(int id) {
    if (scheduledFutureMap.containsKey(id)) {
      scheduledFutureMap.get(id).cancel(true);
    }
  }

  @Override
  public void cancelAll() {

  }

  @Override
  public Map<Integer, ScheduledFuture> getScheduledFutureMap() {
    return scheduledFutureMap;
  }

}

