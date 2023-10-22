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

    //Submit the request thread and proceed.
    //Request thread will create new thread once the first http request was written successfully.
    executorService.submit(() -> {
      Thread.currentThread().setName(sessionHandle.getDestination().getConnectionId() + "-REQ");
      safeStreamForwardRequest(requestContext, sessionHandle);
    });

  }

  public void close(SessionHandle sessionHandle) throws Exception {
    sessionHandle.getSource().getSocket().close();
  }

  public void cleanSession(SessionHandle sessionHandle) {
    IOUtils.closeQuietly(sessionHandle.getSource().getSocket());
    IOUtils.closeQuietly(sessionHandle.getDestination().getSocket());
  }

  private void safeStreamForwardRequest(RequestContext requestContext, SessionHandle sessionHandle) {
    try {
      streamForwardRequest(requestContext, sessionHandle);
    } catch (Exception e) {
      log.error("Error while forwarding: ", e);
      cleanSession(sessionHandle);
    }
  }

  private void streamForwardRequest(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {
    StreamHandle streamHandle = new StreamHandle(
        sessionHandle.getSource().getSocket().getInputStream(),
        sessionHandle.getDestination().getSocket().getOutputStream(),
        sessionHandle.getRequestTraceContext(), StreamType.REQUEST);

    int numRequests = 0;
    boolean responseThreadInitiated = false;
    while (true) {
      StreamResponse streamResponse = httpStreamer.stream(requestContext, streamHandle);

      if (!responseThreadInitiated) {
        submitResponseStreamHandlerThread(requestContext, sessionHandle);
        responseThreadInitiated = true;
      }

      log.info("Total REQ: {}, bytes: {}", ++numRequests, (streamResponse.getNumHeaderBytes() + streamResponse.getNumBodyBytes()));
    }
  }

  private void submitResponseStreamHandlerThread(RequestContext requestContext, SessionHandle sessionHandle) {
    executorService.submit(() -> {
      Thread.currentThread().setName(sessionHandle.getDestination().getConnectionId() + "-RES");
      safeStreamResponse(requestContext, sessionHandle);
    });
  }


  private void safeStreamResponse(RequestContext requestContext, SessionHandle sessionHandle)  {
    try {
      streamResponse(requestContext, sessionHandle);
    } catch (Exception e) {
      log.error("Error while responding: ", e);
      cleanSession(sessionHandle);
    }
  }

  private void streamResponse(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {
    StreamHandle streamHandle = new StreamHandle(sessionHandle.getDestination().getSocket().getInputStream(),
        sessionHandle.getSource().getSocket().getOutputStream(),
        sessionHandle.getResponseTraceContext(), StreamType.RESPONSE);

    int numResponses = 0;
    StreamResponse streamResponse = null;
    do {
      streamResponse = httpStreamer.stream(requestContext, streamHandle);
      log.info("Total RES: {}, bytes: {}", ++numResponses, (streamResponse.getNumHeaderBytes() + streamResponse.getNumBodyBytes()));
    } while (ConnectionState.KEEP_ALIVE.equals(streamResponse.getConnectionState()));
  }
}
