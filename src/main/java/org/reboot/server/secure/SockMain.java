package org.reboot.server.secure;

import org.reboot.server.secure.core.IProxyRequestProcessor;
import org.reboot.server.secure.core.ProxyRequestProcessor;
import org.reboot.server.secure.core.IDestinationServerSocketProvider;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.util.IServerConfiguration;
import org.reboot.server.secure.util.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("appEntry")
public class SockMain {
  /*
  TODO:
      Make read with timeout.
      Handle SocketClosed/Timeout exception.
      Write async
      Multi-part file read/write.
      Make: StreamReader as object holder.
   */

  private static Logger log = LoggerFactory.getLogger(SockMain.class);


  public static String CRLF = "\r\n";

  private String serverFilePath = null;
  private String serverFilePassword = null;

  private IServerConfiguration serverConfiguration;

  private IDestinationServerSocketProvider destinationServerSocketProvider;

  private IProxyRequestProcessor proxyRequestProcessor;
  @Autowired
  public SockMain(IServerConfiguration serverConfiguration,
                  IDestinationServerSocketProvider destinationServerSocketProvider,
                  IProxyRequestProcessor proxyRequestProcessor) {
    this.serverConfiguration = serverConfiguration;
    this.destinationServerSocketProvider = destinationServerSocketProvider;
    this.proxyRequestProcessor = proxyRequestProcessor;
  }

  public void run() throws Exception {
    serverFilePath = serverConfiguration.getRequiredProperty("server.certificate");
    serverFilePassword = serverConfiguration.getRequiredProperty("server.certificate.password");
    Optional<Integer> portOptional = serverConfiguration.getPropertyAsInteger("local.server.port");
    if (!portOptional.isPresent()) {
      throw new RuntimeException("Local Port not configured.");
    }
    log.info("Starting server at port: {}", portOptional.get());
    SSLServerSocketFactory sslSocketFactory = getSSLSocketFactory();
    SSLServerSocket sslServerSocket = (SSLServerSocket)sslSocketFactory.createServerSocket(portOptional.get());
    ExecutorService executorService = Executors.newFixedThreadPool(60);

    while (true) {
      Socket socket = sslServerSocket.accept();

      executorService.submit(() -> {
        safeProcessRequestAndSendResponse(socket);
      });
    }
  }

  public static void main(String[] args) throws Exception {

    AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
    appContext.scan("org.reboot.server.secure");
    appContext.refresh();
    SockMain client = (SockMain) appContext.getBean("appEntry");
    client.run();
  }

  private void safeProcessRequestAndSendResponse(Socket socket) {
    try {
      processRequestAndSendResponse(socket);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processRequestAndSendResponse(Socket socket) throws Exception {
    SessionHandle sessionHandle = new SessionHandle(socket, destinationServerSocketProvider.getDestinationSocket());
    proxyRequestProcessor.start(sessionHandle);
    proxyRequestProcessor.close(sessionHandle);
  }


  private SSLServerSocketFactory getSSLSocketFactory() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(Files.newInputStream(new File(serverFilePath).toPath()), serverFilePassword.toCharArray());
    KeyManagerFactory keyMan = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyMan.init(keyStore, serverFilePassword.toCharArray());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyMan.getKeyManagers(), null, null);
    SSLServerSocketFactory sslFactory = sslContext.getServerSocketFactory();
    return sslFactory;
  }
}
