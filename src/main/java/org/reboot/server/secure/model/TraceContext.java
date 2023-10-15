package org.reboot.server.secure.model;

import java.io.OutputStream;

public class TraceContext {

  private OutputStream requestStream;

  private OutputStream responseStream;

  private StreamType streamType;

  public TraceContext(OutputStream requestStream, OutputStream responseStream) {
    this.requestStream = requestStream;
    this.responseStream = responseStream;
  }

  public OutputStream getRequestStream() {
    return requestStream;
  }

  public OutputStream getResponseStream() {
    return responseStream;
  }

  public StreamType getStreamType() {
    return streamType;
  }

  public void setStreamType(StreamType streamType) {
    this.streamType = streamType;
  }
}
