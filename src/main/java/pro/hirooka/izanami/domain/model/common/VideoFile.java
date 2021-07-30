package pro.hirooka.izanami.domain.model.common;

import java.util.List;
import lombok.Data;

@Data
public class VideoFile {
  private String name;
  private String extension;
  private boolean isM4v;
  private boolean isWatch;
  private boolean isHls;
  private List<Integer> hlsBitrateList;
}
