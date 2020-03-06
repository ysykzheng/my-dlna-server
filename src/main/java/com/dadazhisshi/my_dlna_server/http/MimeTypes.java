package com.dadazhisshi.my_dlna_server.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimeTypes {

  private static final Logger LOG = LoggerFactory.getLogger(MimeTypes.class);
  private static final Map<String, String> __dftMimeMap = new HashMap<>();


  static {
    String resourceName = "mime.properties";
    try (InputStream stream = MimeTypes.class.getResourceAsStream(resourceName)) {
      if (stream == null) {
        LOG.warn("Missing mime-type resource: {}", resourceName);
      } else {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
          Properties props = new Properties();
          props.load(reader);
          props.stringPropertyNames().stream()
              .filter(x -> x != null)
              .forEach(x ->
                  __dftMimeMap.put(x.toLowerCase(), normalizeMimeType(props.getProperty(x))));

          if (__dftMimeMap.isEmpty()) {
            LOG.warn("Empty mime types at {}", resourceName);
          } else if (__dftMimeMap.size() < props.keySet().size()) {
            LOG.warn("Duplicate or null mime-type extension in resource: {}", resourceName);
          }
        } catch (IOException e) {
          LOG.warn("load mime properties fail", e);
        }
      }
    } catch (IOException e) {
      LOG.error("load mime properties fail", e);
    }
  }

  private final Map<String, String> _mimeMap = new HashMap<String, String>();

  /**
   * Constructor.
   */
  public MimeTypes() {
  }

  /**
   * Get the MIME type by filename extension. Lookup only the static default mime map.
   *
   * @param filename A file name
   * @return MIME type matching the longest dot extension of the file name.
   */
  public static String getDefaultMimeByExtension(String filename) {
    String type = null;

    if (filename != null) {
      int i = -1;
      while (type == null) {
        i = filename.indexOf(".", i + 1);

        if (i < 0 || i >= filename.length()) {
          break;
        }

        String ext =
            filename.substring(i + 1).toLowerCase();
        if (type == null) {
          type = __dftMimeMap.get(ext);
        }
      }
    }

    if (type == null) {
      type = __dftMimeMap.get("*");
    }

    return type;
  }

  public static Set<String> getKnownMimeTypes() {
    return new HashSet<>(__dftMimeMap.values());
  }

  private static String normalizeMimeType(String type) {
    return type.toLowerCase();
  }

  public synchronized Map<String, String> getMimeMap() {
    return _mimeMap;
  }

  /**
   * @param mimeMap A Map of file extension to mime-type.
   */
  public void setMimeMap(Map<String, String> mimeMap) {
    _mimeMap.clear();
    if (mimeMap != null) {
      for (Entry<String, String> ext : mimeMap.entrySet()) {
        _mimeMap.put(ext.getKey().toLowerCase(), normalizeMimeType(ext.getValue()));
      }
    }
  }

  /**
   * Get the MIME type by filename extension. Lookup the content and static default mime maps.
   *
   * @param filename A file name
   * @return MIME type matching the longest dot extension of the file name.
   */
  public String getMimeByExtension(String filename) {
    String type = null;

    if (filename != null) {
      int i = -1;
      while (type == null) {
        i = filename.indexOf(".", i + 1);

        if (i < 0 || i >= filename.length()) {
          break;
        }

        String ext = filename.substring(i + 1).toLowerCase();
        if (_mimeMap != null) {
          type = _mimeMap.get(ext);
        }
        if (type == null) {
          type = __dftMimeMap.get(ext);
        }
      }
    }

    if (type == null) {
      if (_mimeMap != null) {
        type = _mimeMap.get("*");
      }
      if (type == null) {
        type = __dftMimeMap.get("*");
      }
    }

    return type;
  }

  /**
   * Set a mime mapping
   *
   * @param extension the extension
   * @param type the mime type
   */
  public void addMimeMapping(String extension, String type) {
    _mimeMap.put(extension.toLowerCase(), normalizeMimeType(type));
  }

}
