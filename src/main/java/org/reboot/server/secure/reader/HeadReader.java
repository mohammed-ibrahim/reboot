package org.reboot.server.secure.reader;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.util.HeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeadReader {

  public static final String CHARSET = "CHARSET";
  private static Logger log = LoggerFactory.getLogger(HeadReader.class);

  public static final String CONTENT_LENGTH = "content-length";

  public static final String TRANSFER_ENCODING = "transfer-encoding";

  public static final String CONTENT_TYPE = "content-type";

  public static PacketHead readHead(InputStream inputStream) {
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    return readWithExistingBuffer(in);
  }

  public static PacketHead readWithExistingBuffer(BufferedReader in) {
    PacketHead packetHead = new PacketHead();
    packetHead.setContentLength(0);
    packetHead.setChunked(false);
    packetHead.setEncoding(null);

    List<String> buffer = new ArrayList<>();
    String line;
    int contentLength = 0;

    while (true) {
      line = ReaderUtils.readLine(in);

      System.out.println("Line read: " + line);
      if (StringUtils.isBlank(line)) {
        break;
      }
      String headerKey = HeaderUtils.getHeaderName(line);
      String lowerCaseLine = line.toLowerCase();

      switch (headerKey) {
        case CONTENT_LENGTH:
          contentLength = parseContentLength(line);
          packetHead.setContentLength(contentLength);
          break;

        case TRANSFER_ENCODING:
          if (!lowerCaseLine.contains("chunked")) {
            throw new RuntimeException("cannot process other than chunked for transfer encoding.");
          }
          packetHead.setChunked(true);
          break;

        case CONTENT_TYPE:
          Optional<String> encoding = HeaderUtils.getHeaderFragment(line, CHARSET);
          encoding.ifPresent(value -> {
            log.debug("Found encoding: {}", value);
            packetHead.setEncoding(value);
          });
          break;

        default:
          log.trace("Not processing header: {}", headerKey);
      }

      buffer.add(line);
    }

    packetHead.setHeadLines(buffer);

    return packetHead;
  }

  private static int parseContentLength(String line) {
    return Integer.parseInt(HeaderUtils.getHeaderValue(line));
  }
}
