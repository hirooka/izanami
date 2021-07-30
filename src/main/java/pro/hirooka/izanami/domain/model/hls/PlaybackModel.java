package pro.hirooka.izanami.domain.model.hls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import pro.hirooka.izanami.domain.config.common.SystemConfiguration;
import pro.hirooka.izanami.domain.config.common.type.FfmpegVcodecType;
import pro.hirooka.izanami.domain.config.hls.HlsConfiguration;

@Data
public class PlaybackModel {

  // TODO: せいり

  private int adaptiveBitrateStreaming = 0;

  private SystemConfiguration systemConfiguration = null;
  private HlsConfiguration hlsConfiguration = null;

  // Configuration
  private String streamRootPath = "";
  private String streamPath = "";
  private String tempEncPath = "";
  private int videoBitrate = 0;

  private long timerSegmenterDelay = 0;
  private long timerSegmenterPeriod = 0;
  private long timerPlaylisterDelay = 0;
  private long timerPlaylisterPeriod = 0;

  private PlaybackSettings playbackSettings = null;

  private String streamFileExtension = ".ts";

  // Segmenter
  private long readBytes = 0;
  private int seqTs = -1;
  private int seqTsEnc = 0;
  private int seqTsOkkake = 0;
  private int seqTsLast = 0;
  private boolean flagSegFullDuration = false;
  private boolean flagLastTs = false;
  private BigDecimal initPcrSecond = new BigDecimal("0.0");
  private BigDecimal lastPcrSecond = new BigDecimal("0.0");
  private BigDecimal diffPcrSecond = new BigDecimal("0.0");
  private BigDecimal lastPcrSec = new BigDecimal("0.0");
  private double duration = 0;
  private List<Double> extinfList = new ArrayList<>();
  private BigDecimal nextInit = new BigDecimal("0.0");
  private int segmentedSequenceByFFmpeg = -1;

  // Encrypter
  private ArrayList<String> keyArrayList = new ArrayList<>();
  private ArrayList<String> ivArrayList = new ArrayList<>();

  // Playlister
  private int seqPl = -1;
  private String namePl = "playlist.m3u8";
  private boolean flagLastPl = false;
  private int sequenceInitialPlaylist = -1;

  // Flag for Timer
  private boolean flagTimerSegmenter = false;
  private boolean flagTimerFfmpegHlsSegmenter = false;
  private boolean flagTimerPlaylister = false;

  // Remover
  private boolean flagRemoveFile = false;

  private boolean isTrascoding = false;
  private long ffmpegPid = -1;
  private Process ffmpegProcess = null;

  private UUID uuid = null;

  private FfmpegVcodecType ffmpegVcodecType = FfmpegVcodecType.UNKNOWN;

  private int sequenceMediaSegment = -1;
  private int sequenceLastMediaSegment = -1;
  private int sequencePlaylist = -1;

  private String tunerDeviceName = "";

  private String unixDomainSocketPath = "";
}

