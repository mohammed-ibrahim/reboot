package org.reboot.server.secure.model;

import java.net.Socket;

public class SessionHandle {

  private InboundSocket source;

  private ManagedSocket destination;

  private TraceContext traceContext;

  public SessionHandle(InboundSocket source, ManagedSocket destination, TraceContext traceContext) {
    this.source = source;
    this.destination = destination;
    this.traceContext = traceContext;
  }

  public InboundSocket getSource() {
    return source;
  }

  public ManagedSocket getDestination() {
    return destination;
  }

  public TraceContext getTraceContext() {
    return traceContext;
  }
}
