package org.reboot.server.secure.server;

import javax.net.ssl.SSLServerSocket;

public interface IServerSocketProvider {

  SSLServerSocket getServerSocket() throws Exception;
}
