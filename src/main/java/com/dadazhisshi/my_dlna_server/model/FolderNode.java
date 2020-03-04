package com.dadazhisshi.my_dlna_server.model;

import static com.dadazhisshi.my_dlna_server.http.HashHelper.sha1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FolderNode<T extends Enum<T> & MediaFormat> extends VirtualFolderNode {

  static final File[] EMPTY_FILE_ARR = new File[0];
  private static final Logger LOG = LoggerFactory.getLogger(FolderNode.class);
  final File folder;
  final Class<T> formatClass;

  @JsonCreator
  public FolderNode(@JsonProperty("title")
      String title,
      @JsonProperty("path")
          File folder,
      @JsonProperty("format")
          String format) {
    super();
    setId(contentId(format, folder));
    setTitle(title == null || title.trim().isEmpty() ? folder.getName() : title);
    this.folder = folder;
    this.formatClass = Formats.fromString(format);
  }

  public FolderNode(File folder, Class<T> formatClass) {
    super();
    setId(contentId(Formats.toString(formatClass), folder));
    setTitle(folder.getName());
    this.folder = folder;
    this.formatClass = formatClass;
  }

  private static String contentId(String format, File folder) {
    return format + (sha1(folder.getAbsolutePath()) + "-" + getSafeName(folder));
  }

  private static String getSafeName(final File folder) {
    return folder.getName().replaceAll("[^a-zA-Z0-9]", "_");
  }

  public static <T extends Enum<T> & MediaFormat> T getFormat(final String value,
      final Class<T> enumClass) {
    try {
      return Enum.valueOf(enumClass, value);
    } catch (IllegalArgumentException iae) {
    }
    return null;
  }

  @Override
  public List<ContainerNode> getContainers() {
    List<ContainerNode> result = new ArrayList<ContainerNode>(super.getContainers());
    for (File file : listFiles()) {
      if (file.isDirectory()) {
        if(file.canRead()){
          FolderNode<T> subFolder = new FolderNode<T>(file, formatClass);
          if (!subFolder.getItems().isEmpty() || !subFolder.getContainers().isEmpty()) {
            result.add(subFolder);
            subFolder.setParent(this);
          }
        }else {
          LOG.warn("folder[{}] can not read",file);
        }

      }
    }
    Collections.sort(result);
    return result;
  }

  @Override
  public List<ItemNode> getItems() {
    List<ItemNode> result = new ArrayList<ItemNode>();
    for (File file : listFiles()) {
      if (file.isFile()) {
        MediaFormat format = getFormat(
            file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase(),
            this.formatClass);
        if (format == null) {
          continue;
        }
        ItemNode itemNode = new ItemNode(contentId(Formats.toString(this.formatClass), file), file,
            format);
        result.add(itemNode);
        itemNode.setParent(this);
      }
    }
    Collections.sort(result);
    return result;
  }

  private File[] listFiles() {
    LOG.info("folder={}",folder);
    if (!folder.exists()) {
      LOG.warn("{} does not exist", folder);
      return EMPTY_FILE_ARR;
    } else if (!folder.isDirectory()) {
      LOG.warn("{} is not a directory", folder);
      return EMPTY_FILE_ARR;
    } else if (!folder.canRead() && !folder.canExecute()) {
      LOG.debug("Folder {} is not accessible", folder.getAbsolutePath());
      return EMPTY_FILE_ARR;
    }
    return folder.listFiles();
  }

  public File getFolder() {
    return folder;
  }

  public Class<? extends MediaFormat> getFormatClass() {
    return formatClass;
  }

  @Override
  public ItemNode getItem(File f) {
    for (ItemNode itemNode : getItems()) {
      if (itemNode.getFile().equals(f)) {
        return itemNode;
      }
    }
    return super.getItem(f);
  }
}
