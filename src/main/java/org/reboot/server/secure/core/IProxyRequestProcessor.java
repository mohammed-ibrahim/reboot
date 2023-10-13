package org.reboot.server.secure.core;

import org.reboot.server.secure.model.RequestContext;
import org.reboot.server.secure.model.SessionHandle;

public interface IProxyRequestProcessor {
  void start(RequestContext requestContext, SessionHandle sessionHandle) throws Exception;

  void close(SessionHandle sessionHandle) throws Exception;
}
