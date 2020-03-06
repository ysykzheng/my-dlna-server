package com.dadazhisshi.my_dlna_server.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VirtualFolderNode extends ContainerNode {

  private List<ContainerNode> containers = Collections.emptyList();

  protected VirtualFolderNode() {
    super(null, null);
  }

  public VirtualFolderNode(
      String title) {
    super("Virtual-Folder-" + idGenerator.getAndIncrement(), title);
  }

  public VirtualFolderNode(List<ContainerNode> containers) {
    this("<NO TITLE>");
    setContainers(containers);
  }

  public List<ContainerNode> getContainers() {
    return containers;
  }

  public void setContainers(List<ContainerNode> containers) {
    this.containers = containers;
    for (ContainerNode container : containers) {
      container.setParent(this);
    }
  }

  @Override
  public List<ItemNode> getItems() {
    return Collections.emptyList();
  }

  public ContainerNode addContainer(ContainerNode container) {
    if (!(containers instanceof ArrayList<?>)) {
      containers = new ArrayList<>(containers);
    }
    containers.add(container);
    container.setParent(this);
    return this;
  }

  public ContainerNode removeContainer(ContainerNode container) {
    if (!(containers instanceof ArrayList<?>)) {
      containers = new ArrayList<>(containers);
    }
    containers.remove(container);
    return this;
  }

  public ItemNode getItem(File f) {
    for (ContainerNode containerNode : getContainers()) {
      ItemNode item = containerNode.getItem(f);
      if (item != null) {
        return item;
      }
    }
    return super.getItem(f);
  }
}
