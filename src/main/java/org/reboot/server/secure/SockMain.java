package org.reboot.server.secure;

import org.reboot.server.secure.core.IProxyRequestProcessor;
import org.reboot.server.secure.core.IDestinationServerSocketProvider;
import org.reboot.server.secure.model.InboundSocket;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.model.TraceContext;
import org.reboot.server.secure.server.IServerSocketProvider;
import org.reboot.server.secure.util.IServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLServerSocket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("appEntry")
public class SockMain {

  private static Logger log = LoggerFactory.getLogger(SockMain.class);

  private String serverFilePath = null;
  private String serverFilePassword = null;

  private IServerConfiguration serverConfiguration;

  private IDestinationServerSocketProvider destinationServerSocketProvider;

  private IProxyRequestProcessor proxyRequestProcessor;

  private IServerSocketProvider serverSocketProvider;
  @Autowired
  public SockMain(IServerConfiguration serverConfiguration,
                  IDestinationServerSocketProvider destinationServerSocketProvider,
                  IProxyRequestProcessor proxyRequestProcessor,
                  IServerSocketProvider serverSocketProvider) {
    this.serverConfiguration = serverConfiguration;
    this.destinationServerSocketProvider = destinationServerSocketProvider;
    this.proxyRequestProcessor = proxyRequestProcessor;
    this.serverSocketProvider = serverSocketProvider;
  }

  public void run() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(60);
    SSLServerSocket sslServerSocket = this.serverSocketProvider.getServerSocket();
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
      log.error("There was a problem: ", e);
    }
  }

  private void processRequestAndSendResponse(Socket socket) throws Exception {
    SessionHandle sessionHandle = new SessionHandle(new InboundSocket(socket),
        destinationServerSocketProvider.getDestinationSocket(),
        getTraceContext());

    proxyRequestProcessor.start(sessionHandle);
    proxyRequestProcessor.close(sessionHandle);
    destinationServerSocketProvider.releaseConnection(sessionHandle.getDestination());
  }

  private TraceContext getTraceContext() throws Exception {
    boolean traceEnabled = serverConfiguration.getBooleanProperty("http.tracing.enabled");
    OutputStream outputStream = null;

    if (traceEnabled) {
      String dataDumpDirectory = serverConfiguration.getRequiredProperty("data.dump.dir");
      File outputFile = Paths.get(dataDumpDirectory, UUID.randomUUID().toString() + ".log").toFile();
      outputStream = new FileOutputStream(outputFile);
    }

    return new TraceContext(outputStream);
  }
}
