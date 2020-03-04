package com.dadazhisshi.my_dlna_server.dlna;

import com.dadazhisshi.my_dlna_server.model.ContainerNode;
import com.dadazhisshi.my_dlna_server.model.ContentNode;
import com.dadazhisshi.my_dlna_server.model.ItemNode;
import com.dadazhisshi.my_dlna_server.model.NodesMap;
import java.util.List;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentDirectoryService extends AbstractContentDirectoryService {

  private static final Logger LOG = LoggerFactory.getLogger(ContentDirectoryService.class);

  public ContentDirectoryService() {
    super();
  }

  @Override
  public BrowseResult browse(final String objectID, final BrowseFlag browseFlag,
      final String filter, final long firstResult, final long maxResults,
      final SortCriterion[] orderBy) throws ContentDirectoryException {
    LOG.info("browse: {} ({}, {})", objectID, firstResult, maxResults);
    long _maxResults = maxResults;
    if (_maxResults == 0) {
      _maxResults = 128 * 1024;
      if (LOG.isDebugEnabled()) {
        LOG.debug("adjusted max results {} -> {}", maxResults, _maxResults);
      }
    }
    try {
      final DIDLContent didl = new DIDLContent();
      final ContentNode node = NodesMap.get(objectID);

      if (node == null) {
        return new BrowseResult("", 0, 0);
      }

      if (node instanceof ItemNode) {
        didl.addItem(((ItemNode) node).getItem());
        return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
      }

      final ContainerNode containerNode = (ContainerNode) node;
      final Container container = containerNode.getContainer();

      if (browseFlag == BrowseFlag.METADATA) {
        didl.addContainer(container);
        return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
      }

      List<? extends ContainerNode> containerNodes = containerNode.getContainers();
      List<? extends ItemNode> itemNodes = containerNode.getItems();

      if (containerNodes.size() > firstResult) {
        final int from = (int) firstResult;
        final int to = Math.min((int) (firstResult + _maxResults), containerNodes.size());
        for (ContainerNode containerNodeX : containerNodes.subList(from, to)) {
          NodesMap.put(containerNodeX.getId(), containerNodeX);
          Container containerX = containerNodeX.getContainer();
          container.addContainer(containerX);
          didl.addContainer(containerX);
        }
      }
      if (didl.getContainers().size() < _maxResults) {
        final int from = (int) Math.max(firstResult - containerNodes.size(), 0);
        final int to = Math
            .min(itemNodes.size(), from + (int) (_maxResults - containerNodes.size()));
        if (from <= to) {
          for (ItemNode itemNode : itemNodes.subList(from, to)) {
            NodesMap.put(itemNode.getId(), itemNode);
            Item item = itemNode.getItem();
            container.addItem(item);
            didl.addItem(item);
          }
        }
      }

      return new BrowseResult(new DIDLParser().generate(didl),
          didl.getContainers().size() + didl.getItems().size(),
          container.getChildCount());
    } catch (final Exception e) {
      LOG.warn("Failed to generate directory listing.", e);
      throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
          e.toString()); // NOSONAR
    }
  }

  @Override
  public BrowseResult search(final String containerId, final String searchCriteria,
      final String filter, final long firstResult, final long maxResults,
      final SortCriterion[] orderBy) throws ContentDirectoryException {
    // You can override this method to implement searching!
    return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
  }
}
