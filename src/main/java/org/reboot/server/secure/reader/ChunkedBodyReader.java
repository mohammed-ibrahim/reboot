package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class ChunkedBodyReader {

  private static final int DEFAULT_BUFFER_SIZE = 1024;

  public static String read(BufferedReader bufferedReader) throws Exception {
    return read(bufferedReader, DEFAULT_BUFFER_SIZE);
  }

  public static String read(BufferedReader bufferedReader, int bufferSize) throws Exception {
    List<String> buffer = new ArrayList<>();
    String line;
    while (StringUtils.isNotBlank(line = bufferedReader.readLine())) {
      if ("0".equals(line)) {
        System.out.println("Breaking line: " + line);
        break;
      }

      int chunkSize = Integer.parseInt(line, 16);
      System.out.println("Chunk size: " + chunkSize);
      String str = readBytes(bufferedReader, chunkSize, bufferSize);
      buffer.add(str);
      bufferedReader.readLine(); //flush the residual CRLF
    }

    return StringUtils.join(buffer, "");
  }

  public static String readBytes(BufferedReader bufferedReader, int chunkSize, int bufferSize) throws Exception {
    char[] buffer = new char[bufferSize];
    int numBytesRead = 0;
    StringBuilder sb = new StringBuilder();

    while (numBytesRead < chunkSize) {
      int read = bufferedReader.read(buffer, 0, calculatePendingBytes(numBytesRead, buffer.length, chunkSize));
      numBytesRead += read;
      sb.append(new String(buffer, 0, read));
    }

    return sb.toString();
  }

  private static int calculatePendingBytes(int numBytesAlreadyRead, int bufferSize, int chunkSize) {
    if ((numBytesAlreadyRead + bufferSize) > chunkSize) {
      return chunkSize - numBytesAlreadyRead;
    }

    return bufferSize;
  }
}
