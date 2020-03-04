package com.dadazhisshi.my_dlna_server.model;

import com.dadazhisshi.my_dlna_server.Config;
import java.io.File;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.seamless.util.MimeType;

public class ItemNode extends ContentNode {

  final File file;
  final MediaFormat format;

  ItemNode(String id, File file, MediaFormat format) {
    super(id);

    this.file = file;
    this.format = format;
  }

  public Item getItem() {
    final String mime = format.getMime();
    final MimeType extMimeType = new MimeType(mime.substring(0, mime.indexOf('/')),
        mime.substring(mime.indexOf('/') + 1));
    String url = "http://" + Config.get().getIpAddress() + ":" + Config.get().getHttpPort();
    final Res res = new Res(extMimeType, file.length(), url + "/" + getId());
    res.setSize(file.length());
    Item item = format.createItem(getId(), file.getName(), res);
    item.setParentID(getParent().getId());
    return item;
  }

  public File getFile() {
    return file;
  }

  public MediaFormat getFormat() {
    return format;
  }

  public String getTitle() {
    return file == null ? null : file.getName();
  }
}