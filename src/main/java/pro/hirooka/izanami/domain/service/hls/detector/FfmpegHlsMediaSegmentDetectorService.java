package pro.hirooka.izanami.domain.service.hls.detector;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.service.hls.detector.event.LastMediaSegmentSequenceEvent;

@Slf4j
@Service
public class FfmpegHlsMediaSegmentDetectorService implements IFfmpegHlsMediaSegmentDetectorService {

  private final FfmpegHlsMediaSegmentDetector ffmpegHlsMediaSegmentDetector;
  private List<ThreadPoolTaskScheduler> threadPoolTaskSchedulerList = new CopyOnWriteArrayList<>();

  public FfmpegHlsMediaSegmentDetectorService(
      FfmpegHlsMediaSegmentDetector ffmpegHlsMediaSegmentDetector
  ) {
    this.ffmpegHlsMediaSegmentDetector = requireNonNull(ffmpegHlsMediaSegmentDetector);
  }

  @Async
  @Override
  public void schedule(final String username, final Date startTime, final long period) {
    ffmpegHlsMediaSegmentDetector.setUsername(username);
    final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setThreadNamePrefix(username);
    //threadPoolTaskScheduler.setPoolSize(3);
    threadPoolTaskScheduler.initialize();
    threadPoolTaskScheduler.scheduleAtFixedRate(ffmpegHlsMediaSegmentDetector, startTime, period);
    threadPoolTaskSchedulerList.add(threadPoolTaskScheduler);
  }

  private void shutdown(final String username) {
    for (ThreadPoolTaskScheduler threadPoolTaskScheduler : threadPoolTaskSchedulerList) {
      if (threadPoolTaskScheduler.getThreadNamePrefix().equals(username)) {
        threadPoolTaskScheduler.shutdown();
        log.info("Shutdown threadPoolTaskScheduler - {}", username);
        threadPoolTaskSchedulerList.remove(threadPoolTaskScheduler);
      }
    }
  }

  @Override
  public void cancel(final String username) {
    shutdown(username);
  }

  @EventListener
  @Override
  public void handleLastMediaSegmentSequence(LastMediaSegmentSequenceEvent event) {
    log.info("handleLastMediaSegmentSequence: {}", event.toString());
    shutdown(event.getUsername());
  }
}