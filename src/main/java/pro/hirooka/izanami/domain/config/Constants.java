package pro.hirooka.izanami.domain.config;

import java.io.File;

public class Constants {
  public static final String FILE_SEPARATOR = File.separator;
  public static final String INITIAL_STREAM_PATH = "/istream";
  public static final String STREAM_ROOT_PATH_NAME = "stream";
  public static final String LIVE_PATH_NAME = "live";
  public static final String STREAM_FILE_NAME_PREFIX = "izanami";
  public static final String M3U8_FILE_NAME = "izanami";
  public static final String FMP4_INIT_FILE_NAME = "izanami";
  public static final String FMP4_INIT_FILE_EXTENSION = ".mp4";
  public static final String M3U8_FILE_EXTENSION = ".m3u8";
  public static final String HLS_KEY_FILE_EXTENSION = ".key";
  public static final String HLS_IV_FILE_EXTENSION = ".iv";
  public static final int HLS_KEY_LENGTH = 128;
  public static final int MPEG2_TS_PACKET_LENGTH = 188;
  public static final String USER_AGENT = "izanami-ios";
  public static final String DEFAULT_USERNAME = "izanami";
  public static final String DEFAULT_PASSWORD = DEFAULT_USERNAME;
  public static final String DEFAULT_TS_SERVER_USERNAME = "izanagi";
  public static final String DEFAULT_TS_SERVER_PASSWORD = DEFAULT_TS_SERVER_USERNAME;
}
