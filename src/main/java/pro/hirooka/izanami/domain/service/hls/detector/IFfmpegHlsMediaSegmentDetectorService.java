package pro.hirooka.izanami.domain.service.hls.detector;

import java.util.Date;
import pro.hirooka.izanami.domain.service.hls.detector.event.LastMediaSegmentSequenceEvent;

public interface IFfmpegHlsMediaSegmentDetectorService {
  void schedule(final String username, final Date startTime, final long period);

  void cancel(final String username);

  void handleLastMediaSegmentSequence(LastMediaSegmentSequenceEvent event);
}
