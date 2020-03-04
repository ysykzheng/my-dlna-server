package com.dadazhisshi.my_dlna_server;

import com.dadazhisshi.my_dlna_server.model.ContainerNode;
import java.io.File;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Config {

  private final static Config INSTANCE = new Config();
  public static String APP_NAME = "My DLNA Server";
  public static String METADATA_MANUFACTURER = "ysykzheng";
  public static String METADATA_MODEL_NAME = "My DLNA Server";
  public static String UDN_ID = "My DLNA Server";
  public static String METADATA_MODEL_DESCRIPTION = "Simple DLNA Server, developed by ysykzheng";
  public static String METADATA_MODEL_NUMBER = "v1";
  private Logger logger = LoggerFactory.getLogger(Config.class);
  /**
   * Port for http server
   */
  @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
  private int httpPort = 8192;
  /**
   * Re-indexing interval (in milliseconds)
   */
  @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
  private int refreshInterval = 10 * 60 * 1000;

  private ContainerNode content;

  private String ipAddress;

  private Config() {
  }

  public static Config get() {
    return INSTANCE;
  }

  public void load(String configFile) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.readerForUpdating(this)
          .readValue(new File(configFile));
    } catch (Exception ex) {
      logger.error("Error occurred while loading config file", ex);
    }
  }

  public int getHttpPort() {
    return httpPort;
  }

  public void setHttpPort(int httpPort) {
    this.httpPort = httpPort;
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public void setRefreshInterval(int refreshInterval) {
    this.refreshInterval = refreshInterval;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public ContainerNode getContent() {
    return content;
  }

  public void setContent(ContainerNode content) {
    this.content = content;
  }
}
