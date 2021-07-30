package pro.hirooka.izanami.domain.model.common;

import lombok.Data;
import pro.hirooka.izanami.domain.model.common.type.M4vType;

@Data
public class M4vFile {
  private String name;
  private M4vType type;
}
