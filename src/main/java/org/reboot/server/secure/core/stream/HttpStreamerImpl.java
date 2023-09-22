package org.reboot.server.secure.core.stream;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpStreamerImpl implements IHttpStreamer {

  private InputStream inputStream;

  private OutputStream outputStream;

  private IHeaderProcessor headerProcessor;

  public HttpStreamerImpl(InputStream inputStream, OutputStream outputStream, IHeaderProcessor headerProcessor) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.headerProcessor = headerProcessor;
  }

  @Override
  public void stream() {
//    streamHeaders();
  }
}
