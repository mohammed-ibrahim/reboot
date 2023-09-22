package org.reboot.server.secure.core.stream;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.InvalidHeaderException;
import org.reboot.server.secure.util.HeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class HeaderProcessorImpl implements IHeaderProcessor {

  private static Logger log = LoggerFactory.getLogger(HeaderProcessorImpl.class);

  public static final String CONTENT_LENGTH = "content-length";

  public static final String TRANSFER_ENCODING = "transfer-encoding";

  public static final String HOST = "host";

  private boolean hasBody;

  private boolean isContentLength;

  private int contentLength;

  private boolean isChunkedPacket;

  private String newHost;

  public HeaderProcessorImpl(String newHost) {
    this.hasBody = false;
    this.isContentLength = false;
    this.isChunkedPacket = false;
    this.newHost = newHost;
  }

  @Override
  public void writeHeader(byte[] data, OutputStream outputStream) throws Exception {
    String line = new String(data).toLowerCase();
    String headerKey = HeaderUtils.getHeaderName(line);

    String updatedHeader = null;

    switch (headerKey) {
      case CONTENT_LENGTH:
        int calculatedContentLength = parseContentLength(line);
        this.isContentLength = true;
        this.contentLength = calculatedContentLength;
        break;

      case TRANSFER_ENCODING:
        if (!line.contains("chunked")) {
          throw new InvalidHeaderException("cannot process other than chunked for transfer encoding.");
        }
        this.isChunkedPacket = true;
        break;

      case HOST:
        updatedHeader = String.format("host: %s", newHost);
        break;

      default:
        log.trace("Not processing header: {}", headerKey);
    }

    if (StringUtils.isNotBlank(updatedHeader)) {
      outputStream.write(updatedHeader.getBytes());
    } else {
      outputStream.write(data);
    }
  }

  private int parseContentLength(String line) {
    return Integer.parseInt(HeaderUtils.getHeaderValue(line));
  }

  @Override
  public boolean hasBody() {
    return this.isContentLength || this.isChunkedPacket;
  }

  @Override
  public boolean isContentLength() {
    return this.isContentLength;
  }

  @Override
  public int getContentLength() {
    return this.contentLength;
  }

  @Override
  public boolean isChunkedPacket() {
    return this.isChunkedPacket;
  }
}
