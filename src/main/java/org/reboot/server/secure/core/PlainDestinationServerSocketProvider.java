package org.reboot.server.secure.core;

import org.apache.commons.lang3.tuple.Pair;
import org.reboot.server.secure.model.ManagedSocket;
import org.reboot.server.secure.model.SocketState;
import org.reboot.server.secure.util.IServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;
import java.util.Optional;

@Service("plainDestinationServerSocketProvider")
public class PlainDestinationServerSocketProvider implements IDestinationServerSocketProvider {

  public static final String DEST_SERVER_PORT = "dest.server.port";
  private static Logger log = LoggerFactory.getLogger(PlainDestinationServerSocketProvider.class);
  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

  public static final String DEST_SERVER_HOST = "dest.server.host";
//  private String host;

  private int port;

  private IServerConfiguration serverConfiguration;

  private IConnectionFactory connectionFactory;

  @Autowired
  public PlainDestinationServerSocketProvider(IServerConfiguration serverConfiguration, IConnectionFactory connectionFactory) {
//    this.host = serverConfiguration.getRequiredProperty(DEST_SERVER_HOST);
    this.serverConfiguration = serverConfiguration;
    this.port = 443;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public ManagedSocket getDestinationSocket() throws Exception {
    String host = serverConfiguration.getRequiredProperty(DEST_SERVER_HOST);
    Optional<Integer> propertyAsInteger = serverConfiguration.getPropertyAsInteger(DEST_SERVER_PORT);
    log.debug("Got host: {} is port mentioned: {} port: {} from config",
        host, propertyAsInteger.isPresent(), propertyAsInteger.orElse(443));

    Pair<String, SSLSocket> newConnection = connectionFactory.getNewConnection(host, propertyAsInteger.orElse(443));
    log.info("Creating new connection with id: {}", newConnection.getLeft());
    ManagedSocket managedSocket = new ManagedSocket(newConnection.getLeft(), newConnection.getRight(), host, port, SocketState.IN_USE);
    return managedSocket;
  }

  @Override
  public void releaseConnection(ManagedSocket managedSocket) {

  }

  @Override
  public void deleteConnection(ManagedSocket managedSocket) {

  }
}
