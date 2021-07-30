package pro.hirooka.izanami.domain.repository.epg;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;

@Repository
public interface ILatestEpgAcquisitionRepository
    extends MongoRepository<LatestEpgAcquisition, Integer> {
}
