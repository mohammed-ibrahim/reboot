package org.reboot.server.secure.core.stream;

import java.io.OutputStream;

public interface IHeaderProcessor {

  void writeHeader(byte[] data, OutputStream outputStream) throws Exception ;

  boolean hasBody();

  boolean isContentLength();

  int getContentLength();

  boolean isChunkedPacket();
}
