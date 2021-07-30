package pro.hirooka.izanami.domain.model.epg;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class LatestEpgAcquisition {
  @Id
  private int unique;
  private long date;
}
