package org.reboot.server.secure.core.stream;

import org.reboot.server.secure.model.RequestContext;
import org.reboot.server.secure.model.StreamHandle;
import org.reboot.server.secure.model.StreamResponse;

import java.io.InputStream;
import java.io.OutputStream;

public interface IHttpStreamer {
  StreamResponse stream(RequestContext requestContext, StreamHandle streamHandle, boolean forwardStream) throws Exception;

//  void close() throws Exception;
}
