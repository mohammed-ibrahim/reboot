package org.reboot.server.secure.core.stream;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.HeaderProcessingResponse;
import org.reboot.server.secure.model.HttpHeaderContext;
import org.reboot.server.secure.model.InvalidHeaderException;
import org.reboot.server.secure.model.StreamContext;
import org.reboot.server.secure.util.HeaderUtils;
import org.reboot.server.secure.util.IServerConfiguration;
import org.reboot.server.secure.util.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HeaderProcessorImpl implements IHeaderProcessor {

  private static Logger log = LoggerFactory.getLogger(HeaderProcessorImpl.class);

  public static final String CONTENT_LENGTH = "content-length";

  public static final String TRANSFER_ENCODING = "transfer-encoding";

  public static final String HOST = "host";

//  @Autowired
//  public HeaderProcessorImpl(IServerConfiguration serverConfiguration) {
//    this.serverConfiguration = serverConfiguration;
//  }

  @Override
  public HeaderProcessingResponse processHeader(byte[] data,
                                                HttpHeaderContext httpHeaderContext,
                                                StreamContext streamContext) throws Exception {

    String line = new String(data).toLowerCase();
    String headerKey = HeaderUtils.getHeaderName(line);

    String updatedHeader = null;

    switch (headerKey) {
      case CONTENT_LENGTH:
        int calculatedContentLength = parseContentLength(line);
        httpHeaderContext.setContentLength(true);
        httpHeaderContext.setContentLength(calculatedContentLength);
        break;

      case TRANSFER_ENCODING:
        if (!line.contains("chunked")) {
          throw new InvalidHeaderException("cannot process other than chunked for transfer encoding.");
        }
        httpHeaderContext.setChunkedPacket(true);
        break;

      case HOST:
        if (streamContext.isUpdateHostHeader()) {
          updatedHeader = String.format("host: %s", streamContext.getNewHostName());
        }
        break;

      default:
        log.trace("Not processing header: {}", headerKey);
    }

    if (StringUtils.isNotBlank(updatedHeader)) {
      return new HeaderProcessingResponse(true, updatedHeader.getBytes());
    } else {
      return new HeaderProcessingResponse(false, null);
    }
  }

  private int parseContentLength(String line) {
    return Integer.parseInt(HeaderUtils.getHeaderValue(line));
  }
}
