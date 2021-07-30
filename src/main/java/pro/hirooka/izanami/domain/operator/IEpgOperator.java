package pro.hirooka.izanami.domain.operator;

import pro.hirooka.izanami.domain.model.epg.LatestEpgAcquisition;

public interface IEpgOperator {
  void persist();

  LatestEpgAcquisition readLatestEpgAcquisition();
}
