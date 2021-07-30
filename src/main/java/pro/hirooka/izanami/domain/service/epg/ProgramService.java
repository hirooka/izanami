package pro.hirooka.izanami.domain.service.epg;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.repository.epg.IProgramRepository;

@Slf4j
@AllArgsConstructor
@Service
public class ProgramService implements IProgramService {

  private final IProgramRepository programRepository;
  private final MongoTemplate mongoTemplate;

  @Override
  public Program create(Program program) {
    return programRepository.save(program);
  }

  @Override
  public List<Program> read() {
    return programRepository.findAll();
  }

  @Override
  public List<Program> read(int channelRemoteControl) {
    final Query query = new Query(Criteria.where("channelRemoteControl").is(channelRemoteControl))
        .with(Sort.by(Sort.Direction.ASC, "begin"));
    return mongoTemplate.find(query, Program.class);
  }

  @Override
  public List<Program> read(int ch, String beginDate) {

    final Instant instant = Instant.ofEpochMilli(new Date().getTime());
    final ZonedDateTime beginZonedDateTime =
        ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    final ZonedDateTime endZonedDateTime =
        ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).plusDays(1);
    final long begin = beginZonedDateTime.toEpochSecond() * 1000;
    final long end = endZonedDateTime.toEpochSecond() * 1000;
    final Query query = new Query(
        Criteria.where("channelRemoteControl")
            .is(ch).and("end").gte(begin).and("begin").lte(end)
    ).with(Sort.by(Sort.Direction.ASC, "begin"));

    return mongoTemplate.find(query, Program.class);
  }

  @Override
  public Program read(String id) {
    final Query query =
        new Query(Criteria.where("id").is(id)).with(Sort.by(Sort.Direction.ASC, "id"));
    List<Program> programList = mongoTemplate.find(query, Program.class);
    if (programList.size() != 1) {
      log.error("e");
      return null;
    } else {
      return programList.get(0);
    }
  }

  @Override
  public List<Program> readByBeginDate(String beginDate) {
    return null;
  }

  @Override
  public List<Program> readByNow(long now) {
    final Query query = new Query(
        Criteria.where("begin").lte(now).and("end").gte(now)
    ).with(Sort.by(Sort.Direction.ASC, "channelRemoteControl"));
    return mongoTemplate.find(query, Program.class);
  }

  @Override
  public List<Program> readOneDayByNow(long now) {
    final Instant instant = Instant.ofEpochMilli(now);
    final ZonedDateTime nowZonedDateTime =
        ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    final ZonedDateTime tomorrowZonedDateTime =
        ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).plusDays(1);

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    final String nowZonedDateTimeString = nowZonedDateTime.format(dateTimeFormatter);
    final String tomorrowZonedDateTimeString = tomorrowZonedDateTime.format(dateTimeFormatter);
    log.info("nowZonedDateTime = {}, {}", nowZonedDateTimeString, nowZonedDateTime.toEpochSecond());
    log.info(
        "tomorrowZonedDateTime = {}, {}",
        tomorrowZonedDateTimeString, tomorrowZonedDateTime.toEpochSecond()
    );

    final Query query = new Query(
        Criteria.where("begin")
            .lte(now)
            .and("end")
            .lte(tomorrowZonedDateTime.toEpochSecond() * 1000)
    ).with(Sort.by(Sort.Direction.ASC, "channelRemoteControl"));
    return mongoTemplate.find(query, Program.class);
  }

  @Override
  public Program readNow(int ch, long now) {
    return null;
  }

  @Override
  public Program update(Program program) {
    return programRepository.save(program);
  }

  @Override
  public void delete(String id) {
    programRepository.deleteById(id);
  }

  @Override
  public List<Program> deleteByEnd(long threshold) {
    return programRepository.deleteByEnd(threshold);
  }

  @Override
  public void deleteAll() {
    programRepository.deleteAll();
  }

  @Override
  public int getNumberOfPhysicalLogicalChannels() {
    return programRepository.findAll().stream()
        .map(Program::getChannel).collect(Collectors.toSet()).size();
  }

  @Override
  public List<Program> getOneDayFromNowByChannelRemoteControl(int channelRemoteControl) {
    final Instant instant = Instant.ofEpochMilli(new Date().getTime());
    final ZonedDateTime beginZonedDateTime =
        ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    final ZonedDateTime endZonedDateTime =
        ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).plusDays(1);
    final long begin = beginZonedDateTime.toEpochSecond() * 1000;
    final long end = endZonedDateTime.toEpochSecond() * 1000;
    final Query query = new Query(
        Criteria.where("channelRemoteControl").is(channelRemoteControl)
            .and("end").gte(begin)
            .and("begin").lte(end))
        .with(Sort.by(Sort.Direction.ASC, "begin"));
    return mongoTemplate.find(query, Program.class);
  }

  @Override
  public List<List<Program>> getOneDayFromNow() {
    return null;
  }
}
