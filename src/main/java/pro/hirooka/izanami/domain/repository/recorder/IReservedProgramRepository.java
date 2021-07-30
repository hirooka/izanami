package pro.hirooka.izanami.domain.repository.recorder;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;

@Repository
public interface IReservedProgramRepository extends MongoRepository<ReservedProgram, Integer> {
}
