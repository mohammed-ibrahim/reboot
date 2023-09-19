package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.SockMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class ChunkedBodyReader {

  private static Logger log = LoggerFactory.getLogger(ChunkedBodyReader.class);

  private static final int DEFAULT_BUFFER_SIZE = 20 * 1024;

  public static String read(BufferedReader bufferedReader) throws Exception {
    return read(bufferedReader, DEFAULT_BUFFER_SIZE);
  }

  private static String readV2(BufferedReader bufferedReader, int bufferSize) {
    String line;
    List<String> list = new ArrayList<>();
    while (true) {
      line = ReaderUtils.readLine(bufferedReader);
      if ("0".equals(line)) {
        log.info("Breaking line: {}", line);
        break;
      }

      list.add(line);
      log.info("************: [{}]", line);
    }

    log.info("LAST STATUS: ************: [{}]", line);
    log.info("PRINT: ************: [{}]", StringUtils.join(list, SockMain.CRLF));
    throw new RuntimeException("ERROR");
  }

  public static String read(BufferedReader bufferedReader, int bufferSize) throws Exception {
    List<String> buffer = new ArrayList<>();
    String line;
    while (StringUtils.isNotBlank(line = ReaderUtils.readLine(bufferedReader))) {
      if ("0".equals(line)) {
        log.info("Breaking line: {}", line);
        break;
      }

      int chunkSize = Integer.parseInt(line, 16);
      log.info("Chunk size: {}", chunkSize);
      String str = readBytes(bufferedReader, chunkSize, bufferSize);
      log.info("Read str: [{}]", str);
      buffer.add(str);
      log.info("Flushing the residual");
      ReaderUtils.readLine(bufferedReader); //flush the residual CRLF
    }

    return StringUtils.join(buffer, "");
  }

  public static String readBytes(BufferedReader bufferedReader, int chunkSize, int bufferSize) throws Exception {
    char[] buffer = new char[bufferSize];
    int numBytesRead = 0;
    StringBuilder sb = new StringBuilder();

    while (numBytesRead < chunkSize) {
      int pendingBytes = calculatePendingBytes(numBytesRead, buffer.length, chunkSize);
      log.info("Reading from: {} to: {}", 0, pendingBytes);
      int read = ReaderUtils.read(bufferedReader, buffer, 0, pendingBytes);
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
