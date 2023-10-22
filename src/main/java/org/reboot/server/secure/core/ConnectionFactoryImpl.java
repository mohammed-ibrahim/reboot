package org.reboot.server.secure.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@Component
public class ConnectionFactoryImpl implements IConnectionFactory {

  private static final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

  private long numConnections = 0;

  private Object sync;

  @Autowired
  public ConnectionFactoryImpl() {
    numConnections = 0;
    this.sync = new Object();
  }

  @Override
  public Pair<String, SSLSocket> getNewConnection(String host, int port) throws Exception {

    synchronized (sync) {
      SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
      socket.startHandshake();
      return Pair.of(getNewId(), socket);
    }

  }

  private String getNewId() {
//    return String.format("%s-%07d", RandomStringUtils.randomAlphabetic(10) , ++numConnections);
    return String.format("%09d" , ++numConnections);
  }
}
