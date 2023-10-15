package org.reboot.server.secure.model;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandle {

  private InputStream inputStream;

  private OutputStream outputStream;

  private TraceContext traceContext;

  private StreamType streamType;

  public StreamHandle(InputStream inputStream, OutputStream outputStream, TraceContext traceContext, StreamType streamType) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.traceContext = traceContext;
    this.streamType = streamType;
    traceContext.setStreamType(streamType);
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public TraceContext getTraceContext() {
    return traceContext;
  }

  public StreamType getStreamType() {
    return streamType;
  }
}
