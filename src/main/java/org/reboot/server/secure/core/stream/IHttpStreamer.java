package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.StreamHandle;

import java.io.InputStream;
import java.io.OutputStream;

public interface IHttpStreamer {
  void stream(StreamHandle streamHandle) throws Exception;

//  void close() throws Exception;
}
