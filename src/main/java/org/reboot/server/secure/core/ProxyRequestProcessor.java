package org.reboot.server.secure.core;

import org.reboot.server.secure.core.stream.IHttpStreamer;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.model.StreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;

@Component
public class ProxyRequestProcessor implements IProxyRequestProcessor {

  private static Logger log = LoggerFactory.getLogger(ProxyRequestProcessor.class);

  private IHttpStreamer httpStreamer;
  @Autowired
  public ProxyRequestProcessor(IHttpStreamer httpStreamer) {
    this.httpStreamer = httpStreamer;
  }

  public void start(SessionHandle sessionHandle) throws Exception {

    try {
      log.info("Starting to read from client");
      streamForwardRequest(sessionHandle.getSource().getInputStream(), sessionHandle.getDestination().getOutputStream());
      log.info("Forwarded request");
    } catch (Exception e) {
      log.error("Error: ", e);
    }

    try {
      streamResponse(sessionHandle.getDestination().getInputStream(), sessionHandle.getSource().getOutputStream());
      log.info("Completed response");
    } catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public void close(SessionHandle sessionHandle) throws Exception {
    sessionHandle.getSource().close();
    sessionHandle.getDestination().close();
  }

  private void streamForwardRequest(InputStream inputStream, OutputStream outputStream) throws Exception {
    httpStreamer.stream(new StreamHandle(inputStream, outputStream));
  }

  private void streamResponse(InputStream inputStream, OutputStream outputStream) throws Exception {
    httpStreamer.stream(new StreamHandle(inputStream, outputStream));
  }
}
