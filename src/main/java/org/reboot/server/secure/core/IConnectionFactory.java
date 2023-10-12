package org.reboot.server.secure.core;

import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.SSLSocket;

public interface IConnectionFactory {

  Pair<String, SSLSocket> getNewConnection(String host, int port) throws Exception;
}
