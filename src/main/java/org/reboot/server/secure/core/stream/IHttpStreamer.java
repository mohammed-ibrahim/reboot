package org.reboot.server.secure.core.stream;

import java.io.InputStream;
import java.io.OutputStream;

public interface IHttpStreamer {
  void stream() throws Exception;

//  void close() throws Exception;
}
