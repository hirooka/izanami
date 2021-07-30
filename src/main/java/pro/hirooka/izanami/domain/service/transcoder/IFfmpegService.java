package pro.hirooka.izanami.domain.service.transcoder;

import java.util.concurrent.Future;

public interface IFfmpegService {
  Future<Integer> submit(final String username);

  void execute(final String username);

  void cancel(final String username);
}
