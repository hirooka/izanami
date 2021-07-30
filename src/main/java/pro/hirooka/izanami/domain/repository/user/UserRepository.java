package pro.hirooka.izanami.domain.repository.user;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import pro.hirooka.izanami.domain.model.user.User;

public interface UserRepository extends MongoRepository<User, UUID> {
  User findOneByUsername(String username);
}
