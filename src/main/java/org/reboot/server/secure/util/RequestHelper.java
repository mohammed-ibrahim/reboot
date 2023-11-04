package org.reboot.server.secure.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.reboot.server.secure.core.IDestinationServerSocketProvider;
import org.reboot.server.secure.core.IProxyRequestProcessor;
import org.reboot.server.secure.model.HttpVersion;
import org.reboot.server.secure.model.InboundSocket;
import org.reboot.server.secure.model.ManagedSocket;
import org.reboot.server.secure.model.RequestContext;
import org.reboot.server.secure.model.SessionHandle;
import org.reboot.server.secure.model.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
public class RequestHelper {

  public static final String HTTP_1_1_ENABLE = "http1.1.enable";
  public static final String HTTP_2_ENABLE = "http2.enable";
  private static Logger log = LoggerFactory.getLogger(RequestHelper.class);

  public static final String UPDATE_SERVER_HOST = "update.server.host.header";

  public static final String HTTP_PROTOCOL_V_1_1 = "http/1.1";
  public static final String HTTP_PROTOCOL_V_2 = "h2";



  private IServerConfiguration serverConfiguration;

  private IDestinationServerSocketProvider destinationServerSocketProvider;

  private IProxyRequestProcessor proxyRequestProcessor;

  public RequestHelper(IServerConfiguration serverConfiguration,
                       @Qualifier("plainDestinationServerSocketProvider") IDestinationServerSocketProvider destinationServerSocketProvider,
                       IProxyRequestProcessor proxyRequestProcessor) {

    this.serverConfiguration = serverConfiguration;
    this.destinationServerSocketProvider = destinationServerSocketProvider;
    this.proxyRequestProcessor = proxyRequestProcessor;
  }

  public void processNewRequest(Socket socket) throws Exception {
    Optional<HttpVersion> httpVersionOptional = performHandShake((SSLSocket) socket);

    if (httpVersionOptional.isPresent()) {
      handleHttpVersionedRequest(httpVersionOptional.get(), socket);
    }
  }

  private void handleHttpVersionedRequest(HttpVersion httpVersion, Socket socket) throws Exception {

    switch (httpVersion) {
      case HTTP_1_1:
        initiateHttp11ProxyRequest(socket);
        return;

      case HTTP_2:
        initiateHttp2ProxyRequest(socket);
    }
  }

  private void initiateHttp11ProxyRequest(Socket socket) throws Exception {
    ManagedSocket managedSocket = destinationServerSocketProvider.getDestinationSocket();
    RequestContext requestContext = new RequestContext();
    requestContext.setDestinationHostName(managedSocket.getHost());
    requestContext.setUpdateHostHeader(serverConfiguration.getBooleanProperty(UPDATE_SERVER_HOST));

    Pair<TraceContext, TraceContext> traceContextPair = getTraceContext(managedSocket);
    SessionHandle sessionHandle = new SessionHandle(new InboundSocket(socket), managedSocket, traceContextPair.getLeft(), traceContextPair.getRight());
    proxyRequestProcessor.start(requestContext, sessionHandle);
  }

  private Optional<HttpVersion> performHandShake(SSLSocket sslSocket) throws IOException {
    try {
//      SSLSocket sslSocket = socket;
      SSLParameters sslParameters = sslSocket.getSSLParameters();

      List<String> serverSupportedALPN = new ArrayList<>();

      if (serverConfiguration.getBooleanProperty(HTTP_1_1_ENABLE)) {
        serverSupportedALPN.add(HTTP_PROTOCOL_V_1_1);
      }

      if (serverConfiguration.getBooleanProperty(HTTP_2_ENABLE)) {
        serverSupportedALPN.add(HTTP_PROTOCOL_V_2);
      }

      if (CollectionUtils.isEmpty(serverSupportedALPN)) {
        throw new RuntimeException("Protocols supported set is empty");
      }

      sslParameters.setApplicationProtocols(serverSupportedALPN.toArray(new String[0]));
      sslSocket.setSSLParameters(sslParameters);
      sslSocket.startHandshake();
      String agreedOnProtocol = sslSocket.getApplicationProtocol();
      log.debug("Handshake successful, with protocol agreed upon: {}", agreedOnProtocol);

      if (HTTP_PROTOCOL_V_1_1.equalsIgnoreCase(agreedOnProtocol)) {
        return Optional.of(HttpVersion.HTTP_1_1);
      } else if (HTTP_PROTOCOL_V_2.equalsIgnoreCase(agreedOnProtocol)) {
        return Optional.of(HttpVersion.HTTP_2);
      }

      log.error("Unsupported protocol: {}", agreedOnProtocol);
      return Optional.empty();
    } catch (Exception e) {
      log.error("There was a problem while handshake.", e);
      IOUtils.closeQuietly(sslSocket);
      return Optional.empty();
    }
  }

  private Pair<TraceContext, TraceContext> getTraceContext(ManagedSocket managedSocket) throws Exception {
    boolean traceEnabled = serverConfiguration.getBooleanProperty("http.tracing.enabled");
    OutputStream requestStream = null;
    OutputStream responseStream = null;

    if (traceEnabled) {
      String id = managedSocket.getConnectionId();
      String requestId = id + "-REQ.log";
      String responseId = id + "-RES.log";
      String dataDumpDirectory = serverConfiguration.getRequiredProperty("data.dump.dir");
      File outputFile = Paths.get(dataDumpDirectory, requestId).toFile();
      requestStream = new FileOutputStream(outputFile);
      File responseFile = Paths.get(dataDumpDirectory, responseId).toFile();
      responseStream = new FileOutputStream(responseFile);
    }

    return Pair.of(new TraceContext(requestStream), new TraceContext(responseStream));
  }

  private void initiateHttp2ProxyRequest(Socket socket) throws Exception {
    run(socket);
  }

  private void run(Socket socket) {
    try {
      byte[] buffer = new byte[1024*16];
      for (int i=0; i<3; i++) {
        log.info("Reading round: {}", i);
        int read = socket.getInputStream().read(buffer, 0, buffer.length);
        byte[] newbuffer = new byte[read];
        System.arraycopy(buffer, 0, newbuffer, 0, read);
        String b46String = new String(Base64.getEncoder().encode(newbuffer));
        log.info("Round: {} Number of bytes read: [{}] base64: [{}]", i,  read, b46String);
        char[] hexChar = Hex.encodeHex(buffer, 0, read, true);
        log.info("Round: {} Hex char: [{}]", i, new String(hexChar));
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
