package pro.hirooka.izanami.domain.service.hls.detector.event;

import org.springframework.context.ApplicationEvent;

public class LastMediaSegmentSequenceEvent extends ApplicationEvent {

  private String username;

  public LastMediaSegmentSequenceEvent(final Object source, final String username) {
    super(source);
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
