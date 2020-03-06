package com.dadazhisshi.my_dlna_server.model;

import com.dadazhisshi.my_dlna_server.Config;
import java.io.File;
import java.io.IOException;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemNode extends ContentNode {

  private static final Logger LOG = LoggerFactory.getLogger(ItemNode.class);

  final File file;

  public MediaFormat getFormat() {
    return format;
  }

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
    String url = "http://" + Config.get().getHost() + ":" + Config.get().getPort();
    try {
      LOG.info("file={}", file.getCanonicalPath());
      String path = file.getCanonicalPath().substring(Config.get().getBasePath().length());
      String value = url + "/" + path;
      LOG.info("item mime={} url={}", mime, value);
      final Res res = new Res(extMimeType, file.length(), value);
      Item item = format.createItem(getId(), file.getName(), res);
      item.setParentID(getParent().getId());
      return item;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public File getFile() {
    return file;
  }

  public String getTitle() {
    return file == null ? null : file.getName();
  }
}