package org.reboot.server.secure.server;

import org.reboot.server.secure.util.IServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Component
public class ServerSocketProvider implements IServerSocketProvider {
  private static Logger log = LoggerFactory.getLogger(ServerSocketProvider.class);
  private int localServerPort;

  private String serverFilePath;

  private String serverFilePassword;

  @Autowired
  public ServerSocketProvider(IServerConfiguration serverConfiguration) {
    Optional<Integer> portOptional = serverConfiguration.getPropertyAsInteger("local.server.port");

    if (!portOptional.isPresent()) {
      System.exit(1);
    }
    log.info("Starting server at port: {}", portOptional.get());
    this.localServerPort = portOptional.get();
    serverFilePath = serverConfiguration.getRequiredProperty("server.certificate");
    serverFilePassword = serverConfiguration.getRequiredProperty("server.certificate.password");
  }


  private SSLServerSocketFactory getSSLSocketFactory() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(Files.newInputStream(new File(serverFilePath).toPath()), serverFilePassword.toCharArray());
    KeyManagerFactory keyMan = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyMan.init(keyStore, serverFilePassword.toCharArray());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyMan.getKeyManagers(), null, null);
//    TrustManager tm = new X509TrustManager() {
//      public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//      }
//
//      public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//      }
//
//      public X509Certificate[] getAcceptedIssuers() {
//        return null;
//      }
//    };
//    sslContext.init(keyMan.getKeyManagers(), new TrustManager[] {tm}, null);
    SSLServerSocketFactory sslFactory = sslContext.getServerSocketFactory();
    return sslFactory;
  }

  @Override
  public SSLServerSocket getServerSocket() throws Exception {
    SSLServerSocketFactory sslSocketFactory = getSSLSocketFactory();
    SSLServerSocket sslServerSocket = (SSLServerSocket)sslSocketFactory.createServerSocket(localServerPort);
    return sslServerSocket;
  }
}
