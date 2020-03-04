package com.dadazhisshi.my_dlna_server.model;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

public enum VideoFormat implements MediaFormat {

  AVI("avi", "video/avi"),
  MP4("mp4", "video/mp4"),
  M4V("m4v", "video/mp4"),
  MKV("mkv", "video/x-matroska"),
  FLV("flv", "video/x-flv"),
  WMV("wmv", "video/x-ms-wmv"),
  MPG("mpg", "video/mpeg"),
  MPEG("mpeg", "video/mpeg");

  private final String ext;
  private final String mime;

  VideoFormat(final String ext, final String mime) {
    this.ext = ext;
    this.mime = mime;
  }

  @Override
  public String getMime() {
    return this.mime;
  }

  @Override
  public String getExt() {
    return ext;
  }

  @Override
  public Item createItem(String id, String title, Res res) {
    return new VideoItem(id, "", title, "", res);
  }
}