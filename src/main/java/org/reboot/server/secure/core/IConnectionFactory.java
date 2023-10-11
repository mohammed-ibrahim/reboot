package org.reboot.server.secure.core;

import javax.net.ssl.SSLSocket;

public interface IConnectionFactory {

  SSLSocket getNewConnection(String host, int port) throws Exception;
}
