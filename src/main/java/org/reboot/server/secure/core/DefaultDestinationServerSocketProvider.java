package org.reboot.server.secure.core;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.ManagedSocket;
import org.reboot.server.secure.model.SocketState;
import org.reboot.server.secure.util.IServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultDestinationServerSocketProvider implements IDestinationServerSocketProvider {


  public static final String DEST_SERVER_HOST = "dest.server.host";
  private String host;

  private int port;

  private IConnectionFactory connectionFactory;

  private Map<String, List<ManagedSocket>> socketMap;

  private Object lock;

  @Autowired
  public DefaultDestinationServerSocketProvider(IServerConfiguration serverConfiguration, IConnectionFactory connectionFactory) {
    this.host = serverConfiguration.getRequiredProperty(DEST_SERVER_HOST);
    this.port = 443;
    this.connectionFactory = connectionFactory;
    this.socketMap = new HashMap<>();
    this.lock = new Object();
  }

  @Override
  public ManagedSocket getDestinationSocket() throws Exception {
    return getDestSocket(this.host, this.port);
  }

  private ManagedSocket getDestSocket(String host, int port) throws Exception {
    String key = getKey(host, port);
    synchronized (lock) {
      if (this.socketMap.containsKey(key)) {
        List<ManagedSocket> existingConnections = this.socketMap.get(key);
        Optional<ManagedSocket> idleConnection = existingConnections.stream().filter(s -> s.getSocketState().equals(SocketState.IDLE)).findFirst();

        ManagedSocket managedSocket;

        if (idleConnection.isPresent()) {
          managedSocket = idleConnection.get();
          managedSocket.setSocketState(SocketState.IN_USE);
        } else {
          managedSocket = new ManagedSocket(connectionFactory.getNewConnection(host, port), SocketState.IN_USE);
          this.socketMap.get(key).add(managedSocket);
        }
        return managedSocket;
      } else {
        ManagedSocket managedSocket = new ManagedSocket(connectionFactory.getNewConnection(host, port), SocketState.IN_USE);
        this.socketMap.put(key, new ArrayList(Collections.singleton(managedSocket)));
        return managedSocket;
      }
    }

  }

  @Override
  public void releaseConnection(ManagedSocket managedSocket) {
    synchronized (lock) {
      managedSocket.setSocketState(SocketState.IDLE);
    }
  }

  private String getKey(String host, int port) {
    return StringUtils.toRootLowerCase(host);
  }
}
