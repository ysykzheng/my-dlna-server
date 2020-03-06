package com.dadazhisshi.my_dlna_server.http;

import com.dadazhisshi.my_dlna_server.Config;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends NanoHTTPD {

  private final String basePath;
  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  public Server(String basePath) {
    super(Config.get().getPort());
    this.basePath = basePath;
  }

  @Override
  public Response serve(IHTTPSession session) {
    Map<String, String> header = session.getHeaders();
    String uri = session.getUri();
    LOG.info("uri={}", uri);

    Path path1 = Paths.get(basePath, File.separator, uri);
    File file = path1.toFile();
    String mime = MimeTypes.getDefaultMimeByExtension(
        file.getName().substring(file.getName().lastIndexOf('.') + 1));
    try {
      String etag = Integer.toHexString(
          (file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

      // Support (simple) skipping:
      long startFrom = 0;
      long endAt = -1;
      String range = header.get("range");
      if (range != null) {
        if (range.startsWith("bytes=")) {
          range = range.substring("bytes=".length());
          int minus = range.indexOf('-');
          try {
            if (minus > 0) {
              startFrom = Long.parseLong(range.substring(0, minus));
              endAt = Long.parseLong(range.substring(minus + 1));
            }
          } catch (NumberFormatException ignored) {
          }
        }
      }

      // Change return code and add Content-Range header when skipping is requested
      long fileLen = file.length();
      if (range != null && startFrom >= 0) {
        if (startFrom >= fileLen) {
          Response res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
              NanoHTTPD.MIME_PLAINTEXT, "");
          res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
          res.addHeader("ETag", etag);
          return res;
        } else {
          if (endAt < 0) {
            endAt = fileLen - 1;
          }

          RandomAccessFileInputStream fis = new RandomAccessFileInputStream(file);
          fis.seek(startFrom);
          fis.limit(endAt + 1);
          int contentLength = fis.available();

          Response res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
              fis, contentLength);
          res.addHeader("Content-Length", Integer.toString(contentLength));
          res.addHeader("File-Size", Long.toString(fileLen));
          res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
          res.addHeader("ETag", etag);
          //ViewLog.log(file, node.getParent().getFormatClass());
          return res;
        }
      } else {
        if (etag.equals(header.get("if-none-match"))) {
          return createResponse(Response.Status.NOT_MODIFIED, mime, "");
        }

        Response res = createResponse(Response.Status.OK, mime,
            new RandomAccessFileInputStream(file), fileLen);
        res.addHeader("Content-Length", Long.toString(fileLen));
        res.addHeader("File-Size", Long.toString(fileLen));
        res.addHeader("ETag", etag);
        //ViewLog.log(file, node.getParent().getFormatClass());
        return res;
      }
    } catch (IOException ioe) {
      return createResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
          "FORBIDDEN: Reading file failed.");
    }
  }

  // Announce that the file server accepts partial content requests
  private Response createResponse(Response.Status status, String mimeType, InputStream message,
      long contentLength) {
    Response res = newFixedLengthResponse(status, mimeType, message, contentLength);
    res.addHeader("Accept-Ranges", "bytes");
    return res;
  }

  private Response createResponse(Response.Status status, String mimeType, String message) {
    Response res = newFixedLengthResponse(status, mimeType, message);
    res.addHeader("Accept-Ranges", "bytes");
    return res;
  }
}
