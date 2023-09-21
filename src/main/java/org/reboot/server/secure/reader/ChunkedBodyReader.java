package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChunkedBodyReader {

  private static Logger log = LoggerFactory.getLogger(ChunkedBodyReader.class);

//  private static final int DEFAULT_BUFFER_SIZE = 20 * 1024;

  public static String read(InputStream inputStream) throws Exception {
    return readEntireChunkedBody(inputStream);
  }

  private static String readEntireChunkedBody(InputStream inputStream) throws Exception {
    List<String> buffer = new ArrayList<>();
    String line;
    while (StringUtils.isNotBlank(line = StreamReader.readLine(inputStream))) {
      if ("0".equals(line)) {
        log.info("Breaking line: {}", line);
        break;
      }

      int chunkSize = Integer.parseInt(line, 16);
      log.info("Chunk size: {}", chunkSize);
//      String str = readBytes(inputStream, chunkSize, bufferSize);
//      String str = readBytes(inputStream, chunkSize, bufferSize);
      String str = StreamReader.readBytes(inputStream, chunkSize);

      log.info("Read str: [{}]", str);
      buffer.add(str);
      log.info("Flushing the residual");
//      String residual = ReaderUtils.readLine(bufferedReader);//flush the residual CRLF
      String residual = StreamReader.readLine(inputStream);
      if (residual.length() > 2) {
        throw new RuntimeException("Large residual seen.");
      }
    }

    return StringUtils.join(buffer, "");
  }
}
