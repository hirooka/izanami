package pro.hirooka.izanami.domain.service.epg;

import static pro.hirooka.izanami.domain.config.Constants.FILE_SEPARATOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import pro.hirooka.izanami.domain.config.epg.EpgConfiguration;
import pro.hirooka.izanami.domain.model.epg.ChannelConfiguration;
import pro.hirooka.izanami.domain.model.epg.ChannelConfigurationWrapper;
import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.model.tuner.Tuner;
import pro.hirooka.izanami.domain.model.tuner.TunerType;
import pro.hirooka.izanami.domain.model.tuner.TunerWrapper;
import pro.hirooka.izanami.domain.repository.epg.ILatestEpgAcquisitionRepository;
import pro.hirooka.izanami.domain.repository.epg.IProgramRepository;

@Slf4j
@AllArgsConstructor
@Service
public class EpgService implements IEpgService {

  private final EpgConfiguration epgConfiguration;
  private final IProgramRepository programRepository;
  private final ILatestEpgAcquisitionRepository latestEpgAcquisitionRepository;
  private final MongoTemplate mongoTemplate;

  @Override
  public List<ChannelConfiguration> getChannelConfigurationList() {
    List<ChannelConfiguration> channelConfigurationList = new ArrayList<>();
    try {
      Resource resource = new ClassPathResource(epgConfiguration.getChannelConfiguration());
      ObjectMapper objectMapper = new ObjectMapper();
      channelConfigurationList =
          objectMapper.readValue(resource.getInputStream(), ChannelConfigurationWrapper.class)
              .getChannelConfigurationList();
      log.info("channelConfigurationList = {}", channelConfigurationList.toString());
    } catch (IOException e) {
      log.error("invalid channel_settings.json: {} {}", e.getMessage(), e);
    }
    return channelConfigurationList;
  }

  @Override
  public List<Tuner> getTunerList() {
    final String path =
        IEpgService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    final String[] pathArray = path.split(FILE_SEPARATOR);
    String currentPath = "";
    for (int i = 1; i < pathArray.length - 3; i++) {
      currentPath = currentPath + FILE_SEPARATOR + pathArray[i];
    }
    log.info("currentPath = {}", currentPath);

    List<Tuner> tunerList = new ArrayList<>();
    try {
      Resource resource = new FileSystemResource(
          currentPath + FILE_SEPARATOR + epgConfiguration.getTuner()
      );
      if (!resource.exists()) {
        resource = new ClassPathResource(epgConfiguration.getTuner());
      }
      final ObjectMapper objectMapper = new ObjectMapper();
      tunerList = objectMapper.readValue(resource.getInputStream(), TunerWrapper.class)
          .getTunerList();
      log.info("tunerList = {}", tunerList.toString());
    } catch (IOException e) {
      log.error("invalid tuner.json: {} {}", e.getMessage(), e);
    }
    return tunerList;
  }

  @Override
  public TunerType getTunerType(final int channelRemoteControl) {
    for (ChannelConfiguration channelConfiguration : getChannelConfigurationList()) {
      if (channelConfiguration.getChannelRemoteControl() == channelRemoteControl) {
        if (channelConfiguration.getType() == TunerType.GR) {
          return TunerType.GR;
        } else if (channelConfiguration.getType() == TunerType.BS) {
          return TunerType.BS;
        }
      }
    }
    return null;
  }

  @Override
  public Program create(final Program program) {
    return programRepository.save(program);
  }

  @Override
  public LatestEpgAcquisition createLatestEpgAcquisition(
      final LatestEpgAcquisition latestEpgAcquisition
  ) {
    return latestEpgAcquisitionRepository.save(latestEpgAcquisition);
  }

  @Override
  public LatestEpgAcquisition readLatestEpgAcquisition() {
    final List<LatestEpgAcquisition> latestEpgAcquisitionList =
        mongoTemplate.findAll(LatestEpgAcquisition.class);
    if (latestEpgAcquisitionList.size() != 1) {
      log.error("LatestEpgAcquisition is duplicated or none.");
      return null;
    } else {
      return latestEpgAcquisitionList.get(0);
    }
  }

  @Override
  public LatestEpgAcquisition updateLatestEpgAcquisition(
      final LatestEpgAcquisition latestEpgAcquisition
  ) {
    return latestEpgAcquisitionRepository.save(latestEpgAcquisition);
  }

  @Override
  public void deleteLatestEpgAcquisition(final int unique) {
    latestEpgAcquisitionRepository.deleteById(unique);
  }
}
