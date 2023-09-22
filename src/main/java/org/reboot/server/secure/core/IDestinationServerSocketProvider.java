package org.reboot.server.secure.core;

import java.net.Socket;

public interface IDestinationServerSocketProvider {

  Socket getDestinationSocket();
}
