package org.reboot.server.secure.core;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

public class DefaultDestinationServerSocketProvider implements IDestinationServerSocketProvider {
  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
  private String host;

  private int port;

  public DefaultDestinationServerSocketProvider(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public Socket getDestinationSocket() throws Exception {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

//      socket.setEnabledProtocols(protocols);
//      socket.setEnabledCipherSuites(cipher_suites);

    socket.startHandshake();

    return socket;
  }
}
