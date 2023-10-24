package org.reboot.server.secure;

import org.reboot.server.secure.core.IProxyRequestProcessor;
import org.reboot.server.secure.core.IDestinationServerSocketProvider;
import org.reboot.server.secure.server.IServerSocketProvider;
import org.reboot.server.secure.util.IServerConfiguration;
import org.reboot.server.secure.util.RequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("appEntry")
public class SockMain {

  private static Logger log = LoggerFactory.getLogger(SockMain.class);

  private RequestHelper requestHelper;

  private IServerSocketProvider serverSocketProvider;
  @Autowired
  public SockMain(RequestHelper requestHelper,
                  IServerSocketProvider serverSocketProvider) {
    this.requestHelper = requestHelper;
    this.serverSocketProvider = serverSocketProvider;
  }

  public static void main(String[] args) throws Exception {
    AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
    appContext.scan("org.reboot.server.secure");
    appContext.refresh();
    SockMain client = (SockMain) appContext.getBean("appEntry");
    client.run();
  }

  public void run() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(60);
    SSLServerSocket sslServerSocket = this.serverSocketProvider.getServerSocket();
    while (true) {
      Socket socket = sslServerSocket.accept();

      executorService.submit(() -> {
        safeProcessNewRequest(socket);
      });
    }
  }

  private void safeProcessNewRequest(Socket socket) {
    try {
      requestHelper.processNewRequest(socket);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("There was a problem: ", e);
    }
  }
}
