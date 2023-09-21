package org.reboot.server.secure.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StreamReader {
  private static Logger log = LoggerFactory.getLogger(StreamReader.class);
  public static String CRLF = "\r\n";

  private static final int DEFAULT_BUFFER_SIZE = 20 * 1024;

  public static String readLine(InputStream inputStream) throws Exception {
    byte[] bytes = readLineBytes(inputStream);
    return new String(bytes);
  }

  public static byte[] readLineBytes(InputStream inputStream) throws IOException {
    //TODO Add check if header greater than actual size.
    byte[] buffer = new byte[16*1024]; //16 KB for now.
    int index = 0;
    int readItem = inputStream.read();

    while (readItem != -1) {

      if (readItem == '\r') {
        int nextItem = inputStream.read();

        if (nextItem == '\n') {
          break;
        }
      } else {
        buffer[index] = (byte)readItem;
        index++;
        readItem = inputStream.read();
      }
    }

    byte[] fullBuffer = new byte[index];
    System.arraycopy(buffer, 0, fullBuffer, 0, index);
    return fullBuffer;
  }

  public static String readBytes(InputStream inputStream, int chunkSize) throws Exception {
    return readBytes(inputStream, chunkSize, DEFAULT_BUFFER_SIZE);
  }

  public static String readBytes(InputStream inputStream, int chunkSize, int bufferSize) throws Exception {
    byte[] buffer = new byte[bufferSize];
    int numBytesRead = 0;
    StringBuilder sb = new StringBuilder();

    while (numBytesRead < chunkSize) {
      int pendingBytes = calculatePendingBytes(numBytesRead, buffer.length, chunkSize);
      log.info("Reading from: {} to: {}", 0, pendingBytes);
      int read = inputStream.read(buffer, 0, pendingBytes);
      log.info("Read complete, num bytes: {}", read);

      if (read == -1) {
        log.error("Unexpected break: {}", read);
        break;
      }

      if (read > 0) {
        numBytesRead += read;
        sb.append(new String(buffer, 0, read));
      }
    }

    log.info("Successfully read chunk bytes");
    return sb.toString();
  }

  private static int calculatePendingBytes(int numBytesAlreadyRead, int bufferSize, int chunkSize) {
    if ((numBytesAlreadyRead + bufferSize) > chunkSize) {
      return chunkSize - numBytesAlreadyRead;
    }

    return bufferSize;
  }
}
