package org.reboot.server.secure.core;

import org.apache.commons.io.IOUtils;
import org.reboot.server.secure.core.stream.IHttpStreamer;
import org.reboot.server.secure.model.ConnectionState;
import org.reboot.server.secure.model.RequestContext;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.model.StreamHandle;
import org.reboot.server.secure.model.StreamResponse;
import org.reboot.server.secure.model.StreamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ProxyRequestProcessor implements IProxyRequestProcessor {

  private static Logger log = LoggerFactory.getLogger(ProxyRequestProcessor.class);

  private static final ExecutorService executorService = Executors.newCachedThreadPool();

  private IHttpStreamer httpStreamer;
  @Autowired
  public ProxyRequestProcessor(IHttpStreamer httpStreamer) {
    this.httpStreamer = httpStreamer;
  }

  public void start(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {

    executorService.submit(() -> {
      Thread.currentThread().setName(sessionHandle.getDestination().getConnectionId() + "-FWD");
      initiateReqResCycle(requestContext, sessionHandle);
    });

  }

  public void close(SessionHandle sessionHandle) throws Exception {
//    sessionHandle.getSource().getSocket().close();
  }

  public void cleanSession(SessionHandle sessionHandle) {
    IOUtils.closeQuietly(sessionHandle.getSource().getSocket());
    IOUtils.closeQuietly(sessionHandle.getDestination().getSocket());
  }

  private void initiateReqResCycle(RequestContext requestContext, SessionHandle sessionHandle) {
    try {
      startStreamCycle(requestContext, sessionHandle);
    } catch (Exception e) {
      log.error("Error while forwarding: ", e);
      cleanSession(sessionHandle);
    }
  }

  private void startStreamCycle(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {
    StreamHandle requestHandle = new StreamHandle(
        sessionHandle.getSource().getSocket().getInputStream(),
        sessionHandle.getDestination().getSocket().getOutputStream(),
        sessionHandle.getRequestTraceContext(), StreamType.REQUEST);


    StreamHandle responseHandle = new StreamHandle(sessionHandle.getDestination().getSocket().getInputStream(),
        sessionHandle.getSource().getSocket().getOutputStream(),
        sessionHandle.getResponseTraceContext(), StreamType.RESPONSE);

    int numCycles = 0;
    StreamResponse streamResponse;

    while (true) {
      try {
        streamResponse = httpStreamer.stream(requestContext, requestHandle);
        log.info("Total REQ: {}, bytes: {}", ++numCycles, (streamResponse.getNumHeaderBytes() + streamResponse.getNumBodyBytes()));
      } catch (Exception e) {
        log.error("Problem during forwarding request." , e);
        cleanSession(sessionHandle);
        break;
      }

      try {
        streamResponse = httpStreamer.stream(requestContext, responseHandle);
        log.info("Total RES: {}, bytes: {}", numCycles, (streamResponse.getNumHeaderBytes() + streamResponse.getNumBodyBytes()));
      } catch (Exception e) {
        log.error("Problem during responding to client." , e);
        cleanSession(sessionHandle);
        break;
      }

      if (!ConnectionState.KEEP_ALIVE.equals(streamResponse.getConnectionState())) {
        break;
      }
    }
  }
}
