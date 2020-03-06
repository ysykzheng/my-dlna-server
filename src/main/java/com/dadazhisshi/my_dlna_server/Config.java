package com.dadazhisshi.my_dlna_server;

public class Config {

  private final static Config INSTANCE = new Config();
  public static String APP_NAME = "My DLNA Server";
  public static String METADATA_MANUFACTURER = "ysykzheng";
  public static String METADATA_MODEL_NAME = "My DLNA Server";
  public static String UDN_ID = "My DLNA Server";
  public static String METADATA_MODEL_DESCRIPTION = "Simple DLNA Server, developed by ysykzheng";
  public static String METADATA_MODEL_NUMBER = "v1";
  private int port = 8192;

  private String host;
  private String basePath;

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  private Config() {
  }

  public static Config get() {
    return INSTANCE;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

}
