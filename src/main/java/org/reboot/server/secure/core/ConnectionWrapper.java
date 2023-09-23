package org.reboot.server.secure.core;

import org.reboot.server.secure.core.stream.HeaderProcessorImpl;
import org.reboot.server.secure.core.stream.HttpStreamerImpl;
import org.reboot.server.secure.util.IConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionWrapper {

  private static Logger log = LoggerFactory.getLogger(ConnectionWrapper.class);

  private Socket incomingSocket;

  private Socket destinationSocket;

  private IDestinationServerSocketProvider destinationServerSocketProvider;

  private IConfigurationProvider configurationProvider;

  private String newHost;
  public ConnectionWrapper(Socket incomingSocket,
                           Socket destinationSocket,
                           IConfigurationProvider configurationProvider,
                           String newHost) {

    this.incomingSocket = incomingSocket;
    this.destinationSocket = destinationSocket;
    this.configurationProvider = configurationProvider;
    this.newHost = newHost;
  }

  public void start() throws Exception {
//    Socket destinationSocket = destinationServerSocketProvider.getDestinationSocket();

    try {
      log.info("Startin to read from client");
      streamForwardRequest(incomingSocket.getInputStream(), destinationSocket.getOutputStream());
      log.info("Forwarded request");
    } catch (Exception e) {
      log.error("Error: ", e);
    }

    try {
      streamResponse(destinationSocket.getInputStream(), incomingSocket.getOutputStream());
      log.info("Completed response");
    } catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public void close() throws Exception {
    this.incomingSocket.close();

  }

  private void streamForwardRequest(InputStream inputStream, OutputStream outputStream) throws Exception {
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(newHost, true);
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(inputStream, outputStream, headerProcessor);
    httpStreamer.stream();
  }

  private void streamResponse(InputStream inputStream, OutputStream outputStream) throws Exception {
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(null, false);
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(inputStream, outputStream, headerProcessor);
    httpStreamer.stream();
  }
}
