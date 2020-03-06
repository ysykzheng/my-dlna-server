package com.dadazhisshi.my_dlna_server.model;

import com.dadazhisshi.my_dlna_server.Config;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;

public abstract class ContainerNode extends ContentNode {

  static final AtomicInteger idGenerator = new AtomicInteger(0);
  private String title;

  public ContainerNode(String id, String title) {
    super(id);
    this.title = title;
  }

  public final Container getContainer() {
    Container container = createContainer();
    container.setChildCount(getChildCount());
    return container;
  }

  final Container createContainer() {
    Container container = new Container();
    container.setId(getId());
    container.setTitle(title);
    container.setParentID(getParent() == null ? "-1" : getParent().getId());
    container.setClazz(new DIDLObject.Class("object.container"));
    if (getParent() == null) {
      container.setCreator(Config.METADATA_MODEL_NAME);
    }
    container.setRestricted(true);
    container.setSearchable(true);
    container.setWriteStatus(WriteStatus.NOT_WRITABLE);
    container.setChildCount(0);
    return container;
  }

  public abstract List<ContainerNode> getContainers();

  public abstract List<ItemNode> getItems();

  public final int getChildCount() {
    return getContainers().size() + getItems().size();
  }

  public ItemNode getItem(File f) {
    return null;
  }

  @Override
  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }
}
