package org.reboot.server.secure.model;

import java.net.Socket;

public class SessionHandle {

  private InboundSocket source;

  private ManagedSocket destination;

  private TraceContext requestTraceContext;

  private TraceContext responseTraceContext;

  public SessionHandle(InboundSocket source, ManagedSocket destination,
                       TraceContext requestTraceContext, TraceContext responseTraceContext) {
    this.source = source;
    this.destination = destination;
    this.requestTraceContext = requestTraceContext;
    this.responseTraceContext = responseTraceContext;
  }

  public InboundSocket getSource() {
    return source;
  }

  public ManagedSocket getDestination() {
    return destination;
  }

  public TraceContext getRequestTraceContext() {
    return requestTraceContext;
  }

  public TraceContext getResponseTraceContext() {
    return responseTraceContext;
  }
}
