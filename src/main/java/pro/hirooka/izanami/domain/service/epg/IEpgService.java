package pro.hirooka.izanami.domain.service.epg;

import java.util.List;
import pro.hirooka.izanami.domain.model.epg.ChannelConfiguration;
import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.model.tuner.Tuner;
import pro.hirooka.izanami.domain.model.tuner.TunerType;

public interface IEpgService {
  List<ChannelConfiguration> getChannelConfigurationList();

  List<Tuner> getTunerList();

  TunerType getTunerType(int channelRemoteControl);

  Program create(Program program);

  LatestEpgAcquisition createLatestEpgAcquisition(LatestEpgAcquisition latestEpgAcquisition);

  LatestEpgAcquisition readLatestEpgAcquisition();

  LatestEpgAcquisition updateLatestEpgAcquisition(LatestEpgAcquisition latestEpgAcquisition);

  void deleteLatestEpgAcquisition(int unique);
}
