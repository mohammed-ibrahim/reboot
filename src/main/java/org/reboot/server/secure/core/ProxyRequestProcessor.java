package org.reboot.server.secure.core;

import com.google.common.base.Stopwatch;
import org.reboot.server.secure.core.stream.IHttpStreamer;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.model.StreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ProxyRequestProcessor implements IProxyRequestProcessor {

  private static Logger log = LoggerFactory.getLogger(ProxyRequestProcessor.class);

  private IHttpStreamer httpStreamer;
  @Autowired
  public ProxyRequestProcessor(IHttpStreamer httpStreamer) {
    this.httpStreamer = httpStreamer;
  }

  public void start(SessionHandle sessionHandle) throws Exception {

    Stopwatch streamForward = Stopwatch.createStarted();
    try {
      log.debug("Starting to read from client");
      streamForwardRequest(sessionHandle);
      streamForward.stop();
      log.debug("Forwarded request");
    } catch (Exception e) {
      log.error("Error during streaming forward, managed connectionId: {}", sessionHandle.getDestination().getConnectionId(), e);
      throw new RuntimeException("Error during streaming forward.");
    }

    Stopwatch streamResponse = Stopwatch.createStarted();
    try {
      log.debug("Starting stream response");
      streamResponse(sessionHandle);
      streamResponse.stop();
      log.debug("Stream response completed");
    } catch (Exception e) {
      log.error("Error during returning, managed connectionId: {}", sessionHandle.getDestination().getConnectionId(), e);
      throw new RuntimeException("Error during returning.");
    }

    log.info("Forward stream time: {} ms, response stream time: {} ms",
        streamForward.elapsed(TimeUnit.MILLISECONDS),
        streamResponse.elapsed(TimeUnit.MILLISECONDS));
  }

  public void close(SessionHandle sessionHandle) throws Exception {
    sessionHandle.getSource().getSocket().close();
//    sessionHandle.getDestination().close();
  }

  private void streamForwardRequest(SessionHandle sessionHandle) throws Exception {
    httpStreamer.stream(new StreamHandle(sessionHandle.getSource().getSocket().getInputStream(), sessionHandle.getDestination().getSocket().getOutputStream(), sessionHandle.getTraceContext()));
  }

  private void streamResponse(SessionHandle sessionHandle) throws Exception {
    httpStreamer.stream(new StreamHandle(sessionHandle.getDestination().getSocket().getInputStream(), sessionHandle.getSource().getSocket().getOutputStream(), sessionHandle.getTraceContext()));
  }
}
