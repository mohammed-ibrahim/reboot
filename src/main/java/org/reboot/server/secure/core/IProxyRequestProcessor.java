package org.reboot.server.secure.core;

import org.reboot.server.secure.model.SessionHandle;

public interface IProxyRequestProcessor {
  void start(SessionHandle sessionHandle) throws Exception;

  void close(SessionHandle sessionHandle) throws Exception;
}
