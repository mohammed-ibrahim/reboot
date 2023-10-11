package org.reboot.server.secure.core;

import org.reboot.server.secure.model.ManagedSocket;

public interface IDestinationServerSocketProvider {

  ManagedSocket getDestinationSocket() throws Exception;

  void releaseConnection(ManagedSocket managedSocket);
}
