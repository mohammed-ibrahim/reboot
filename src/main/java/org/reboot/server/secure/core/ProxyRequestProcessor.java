package org.reboot.server.secure.core;

import com.google.common.base.Stopwatch;
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
import java.util.concurrent.TimeUnit;

@Component
public class ProxyRequestProcessor implements IProxyRequestProcessor {

  private static final boolean REQUEST_STREAM = true;
  private static final boolean RESPONSE_STREAM = false;
  private static Logger log = LoggerFactory.getLogger(ProxyRequestProcessor.class);

  private static final ExecutorService executorService = Executors.newCachedThreadPool();

  private IHttpStreamer httpStreamer;
  @Autowired
  public ProxyRequestProcessor(IHttpStreamer httpStreamer) {
    this.httpStreamer = httpStreamer;
  }

  public void start(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {

    executorService.submit(() -> {
      Thread.currentThread().setName(sessionHandle.getDestination().getConnectionId() + "-REQ");
      safeStreamForwardRequest(requestContext, sessionHandle);
    });

    executorService.submit(() -> {
      Thread.currentThread().setName(sessionHandle.getDestination().getConnectionId() + "-RES");
      safeStreamResponse(requestContext, sessionHandle);
    });

  }

  public void close(SessionHandle sessionHandle) throws Exception {
    sessionHandle.getSource().getSocket().close();
  }

  private void safeStreamForwardRequest(RequestContext requestContext, SessionHandle sessionHandle) {
    try {
      streamForwardRequest(requestContext, sessionHandle);
    } catch (Exception e) {
      log.error("Error while forwarding: ", e);
      IOUtils.closeQuietly(sessionHandle.getSource().getSocket());
      IOUtils.closeQuietly(sessionHandle.getDestination().getSocket());
    }
  }

  private void streamForwardRequest(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {
    StreamHandle streamHandle = new StreamHandle(
        sessionHandle.getSource().getSocket().getInputStream(),
        sessionHandle.getDestination().getSocket().getOutputStream(),
        sessionHandle.getRequestTraceContext(), StreamType.REQUEST);

//    StreamResponse streamResponse = null;
    while (true) {
       httpStreamer.stream(requestContext, streamHandle);
    }
  }


  private void safeStreamResponse(RequestContext requestContext, SessionHandle sessionHandle)  {
    try {
      streamResponse(requestContext, sessionHandle);
    } catch (Exception e) {
      log.error("Error while responding: ", e);
      IOUtils.closeQuietly(sessionHandle.getSource().getSocket());
      IOUtils.closeQuietly(sessionHandle.getDestination().getSocket());
    }
  }

  private void streamResponse(RequestContext requestContext, SessionHandle sessionHandle) throws Exception {
    StreamHandle streamHandle = new StreamHandle(sessionHandle.getDestination().getSocket().getInputStream(),
        sessionHandle.getSource().getSocket().getOutputStream(),
        sessionHandle.getResponseTraceContext(), StreamType.RESPONSE);

    StreamResponse streamResponse = null;
    do {
      streamResponse = httpStreamer.stream(requestContext, streamHandle);
    } while (ConnectionState.KEEP_ALIVE.equals(streamResponse.getConnectionState()));
  }
}
