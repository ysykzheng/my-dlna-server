package com.dadazhisshi.my_dlna_server.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends InputStream {

  private final RandomAccessFile randomAccessFile;
  /**
   * Absolute position in file to finish reading on (exclusive)
   */
  private long limit = -1;

  public RandomAccessFileInputStream(File file) throws FileNotFoundException {
    this(new RandomAccessFile(file, "r"));
  }

  public RandomAccessFileInputStream(RandomAccessFile randomAccessFile) {
    this.randomAccessFile = randomAccessFile;
  }

  @Override
  public int read() throws IOException {
    return randomAccessFile.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return randomAccessFile.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return randomAccessFile.read(b, off, len);
  }

  @Override
  public int available() throws IOException {
    long a = (limit >= 0 ? Math.min(limit, randomAccessFile.length()) : randomAccessFile.length())
        - randomAccessFile.getFilePointer();
    return (int) Math.min(Integer.MAX_VALUE, Math.max(a, 0));
  }

  @Override
  public synchronized void reset() throws IOException {
    limit = -1;
    randomAccessFile.seek(0);
  }

  @Override
  public void close() throws IOException {
    randomAccessFile.close();
  }

  public void seek(long pos) throws IOException {
    randomAccessFile.seek(pos);
  }

  public void limit(long limit) {
    this.limit = limit;
  }
}
