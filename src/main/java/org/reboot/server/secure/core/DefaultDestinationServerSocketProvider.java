package org.reboot.server.secure.core;

import org.reboot.server.secure.util.IServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

@Component
public class DefaultDestinationServerSocketProvider implements IDestinationServerSocketProvider {
  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

  public static final String DEST_SERVER_HOST = "dest.server.host";
  private String host;

  private int port;

  @Autowired
  public DefaultDestinationServerSocketProvider(IServerConfiguration serverConfiguration) {
    this.host = serverConfiguration.getRequiredProperty(DEST_SERVER_HOST);
    this.port = 443;
  }

  @Override
  public Socket getDestinationSocket() throws Exception {
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.startHandshake();
    return socket;
  }
}
