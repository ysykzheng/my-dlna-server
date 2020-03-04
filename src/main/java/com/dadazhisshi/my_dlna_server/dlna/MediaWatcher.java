package com.dadazhisshi.my_dlna_server.dlna;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.dadazhisshi.my_dlna_server.Config;
import com.dadazhisshi.my_dlna_server.model.ContainerNode;
import com.dadazhisshi.my_dlna_server.model.FolderNode;
import com.dadazhisshi.my_dlna_server.model.NodesMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(MediaWatcher.class);
  private volatile boolean running = true;

  private boolean isRunning() {
    return running;
  }

  public void stop() {
    running = false;
  }

  private void reset() {
    LOG.info("reset content map");
    NodesMap.clear();
    ContainerNode rootNode = Config.get().getContent();
    rootNode.setId("0");
    NodesMap.put(rootNode.getId(), rootNode);
    rootNode.getItems();
  }

  public void start() throws IOException {
    running = true;
    final WatchService watcher = FileSystems.getDefault().newWatchService();
    ContainerNode rootNode = Config.get().getContent();
    for (ContainerNode container : rootNode.getContainers()) {
      if (container instanceof FolderNode) {
        FolderNode<?> folderNode = (FolderNode<?>) container;
        File folder = folderNode.getFolder();
        if (folder.isDirectory() && folder.canRead()) {
          Path dir = folder.toPath();
          LOG.info("watch dir[{}]", dir);
          try {
            dir.register(watcher,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY);
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (Files.isDirectory(dir) && Files.isReadable(dir) && Files.isExecutable(dir)) {
                  try {
                    dir.register(watcher,
                        ENTRY_CREATE,
                        ENTRY_DELETE,
                        ENTRY_MODIFY);
                  } catch (IOException e) {
                    LOG.error("watch dir[{}] fail", dir, e);
                  }
                  return FileVisitResult.CONTINUE;
                } else {
                  return FileVisitResult.SKIP_SUBTREE;
                }
              }
            });
          } catch (IOException x) {
            LOG.error("watch dir[{}] fail", dir, x);
          }
        }
      }
    }

    Thread thread = new Thread(() -> {
      while (isRunning()) {
        WatchKey key;
        try {
          key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
          Kind<?> kind = event.kind();
          if (kind == OVERFLOW) {
            continue;
          }
          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path filename = ev.context();
          if (kind == ENTRY_CREATE) {
            LOG.info("create file[{}]", filename.toAbsolutePath());
          }
          if (kind == ENTRY_DELETE) {
            LOG.info("delete file[{}]", filename.toAbsolutePath());
          }
          if (kind == ENTRY_MODIFY) {
            LOG.info("modify file[{}]", filename.toAbsolutePath());
          }

          reset();
          boolean valid = key.reset();
          if (!valid) {
            break;
          }
        }
      }
    });
    thread.setName("MediaWatcher");
    thread.start();
  }

}
