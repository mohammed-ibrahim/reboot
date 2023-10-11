package org.reboot.server.secure.core;

import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@Component
public class ConnectionFactoryImpl implements IConnectionFactory {

  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

  @Override
  public SSLSocket getNewConnection(String host, int port) throws Exception {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.startHandshake();
    return socket;
  }
}
