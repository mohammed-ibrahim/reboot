package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.SockMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LineBasedReader {

  private static Logger log = LoggerFactory.getLogger(LineBasedReader.class);
  public static String read(InputStream inputStream, String encoding) throws Exception {
    BufferedReader in = StringUtils.isBlank(encoding)
        ? new BufferedReader(new InputStreamReader(inputStream))
        : new BufferedReader(new InputStreamReader(inputStream, encoding));

    return readWithExistingBuffer(in);
  }

  public static String readWithExistingBuffer(BufferedReader in) {
    List<String> buffer = new ArrayList<>();
    String line;
    while(true) {
      line = ReaderUtils.readLine(in);
      log.info("READ LINE: {}", line);

      if ("0".equals(line) || line == null) {
        log.info("Breaking line");
        break;
      }

      buffer.add(line);
    }

    log.info("Number of lines: {}", buffer.size());
    String body = StringUtils.join(buffer, SockMain.CRLF);
    log.info("Body: {}", body);
    return body;
  }
}
