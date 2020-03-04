package com.dadazhisshi.my_dlna_server.model;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.Item;

public enum AudioFormat implements MediaFormat {
  MP3("mp3", "audio/mpeg"),
  OGG("ogg", "audio/ogg"),
  FLAC("flac", "audio/flac");

  private final String ext;
  private final String mime;

  AudioFormat(final String ext, final String mime) {
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
    return new AudioItem(id, "", title, "", res);
  }
}
