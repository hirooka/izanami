package pro.hirooka.izanami.domain.model.user;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class UserRole implements Serializable {
  private UUID uuid = UUID.randomUUID();
  private String name;
  private String authority;
}
