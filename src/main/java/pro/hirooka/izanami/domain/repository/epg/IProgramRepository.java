package pro.hirooka.izanami.domain.repository.epg;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pro.hirooka.izanami.domain.model.epg.Program;

@Repository
public interface IProgramRepository extends MongoRepository<Program, String> {

  // ex. {$and:[{'piyo':{$eq:123}},{'channel':{$regex:/^GR_/}}]}
  //@Query("{$and:[{'piyo':{$eq:?0}},{'channel':{$regex:?1}}]}")
  List<Program> findAllByChannel(String channel);

  List<Program> findAllByChannelRemoteControl(int channelRemoteControl);

  @Query("{$and:[{'channelRemoteControl':{$eq:?0}},{'start':{$lte:?1}},{'end':{$gte:?1}}]}")
  Program findOneByChannelRemoteControlAndNowLike(String channelRemoteControl, long now);

  // spring-data-mongodb:1.9.x.RELEASE から spring-data-mongodb:1.10.0.RELEASE にすると機能せず
  @Query("{$and:[{'start':{$lte:?0}},{'end':{$gte:?0}},{'channelRemoteControl':{$ne:0}}]}")
  List<Program> findAllByNowLike(long now);

  @Query("{$and:[{'start':{$lte:?1}},{'end':{$gte:?1}},{'channelRemoteControl':{$ne:?0}}]}")
  List<Program> findOneByChannelRecordingAndNowLike(int channelRecording, long now);

  @Query("{$and:[{'channelRemoteControl':{$eq:?0}},{'start':{$lte:?2}},{'start':{$gte:?1}}]}")
  Program findOneByChannelRemoteControlAndFromAndToLike(
      int channelRemoteControl, long from, long to
  );

  @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
  List<Program> deleteByDate(long date);

  @Query("{'end':{$lte:?0}}")
  List<Program> deleteByEnd(long end);

  @Query("{$and:[{'begin':{$gte:?0}},{'end':{$lte:?1}},{'channelRemoteControl':{$ne:0}}]}")
  List<Program> findAllByBeginAndEndLike(long begin, long end);

  //    @Query("{$and:[{'begin':{$lte:?0}},{'end':{$lte:?0}}]}")
  //    Long deleteProgramByDate(long date); // 機能しない...
}
