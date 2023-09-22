package org.reboot.server.secure.core;

import org.reboot.server.secure.util.IConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionWrapper {

  private static Logger log = LoggerFactory.getLogger(ConnectionWrapper.class);

  private Socket incomingSocket;

  private IDestinationServerSocketProvider destinationServerSocketProvider;

  private IConfigurationProvider configurationProvider;

  public ConnectionWrapper(Socket incomingSocket, IDestinationServerSocketProvider destinationServerSocketProvider, IConfigurationProvider configurationProvider) {
    this.incomingSocket = incomingSocket;
    this.destinationServerSocketProvider = destinationServerSocketProvider;
    this.configurationProvider = configurationProvider;
  }

  public void start() {
    Socket destinationSocket = destinationServerSocketProvider.getDestinationSocket();

    try {
      streamForwardRequest(incomingSocket.getInputStream(), destinationSocket.getOutputStream());
    } catch (Exception e) {

    }

    try {
      streamResponse(destinationSocket.getInputStream(), incomingSocket.getOutputStream());
    } catch (Exception e) {

    }
  }

  private void streamResponse(InputStream inputStream, OutputStream outputStream) {
  }

  private void streamForwardRequest(InputStream inputStream, OutputStream outputStream) {

  }
}
