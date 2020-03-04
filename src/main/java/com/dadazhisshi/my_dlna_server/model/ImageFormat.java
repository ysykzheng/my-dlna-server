package com.dadazhisshi.my_dlna_server.model;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;

public enum ImageFormat implements MediaFormat {
  JPG("jpg", "image/jpeg"),
  JPEG("jpeg", "image/jpeg"),
  PNG("png", "image/png");

  private final String ext;
  private final String mime;

  ImageFormat(final String ext, final String mime) {
    this.ext = ext;
    this.mime = mime;
  }

  public String getMime() {
    return this.mime;
  }

  public String getExt() {
    return ext;
  }

  public Item createItem(String id, String title, Res res) {
    return new ImageItem(id, "", title, "", res);
  }
}
