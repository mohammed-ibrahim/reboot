package org.reboot.server.secure;

import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.reader.PacketReader;
import org.reboot.server.secure.util.ConfigurationManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SockMain {

  public static final String CONFIG_FILE_PATH = "config.file.path";
  public static String CRLF = "\r\n";
  private static String serverFilePath = null;
  private static String serverFilePassword = null;

  private static String destServer = null;

  public static void main(String[] args) throws Exception {

    String appConfigFilePath = System.getProperty(CONFIG_FILE_PATH);
    if (StringUtils.isBlank(appConfigFilePath)) {
      throw new RuntimeException(CONFIG_FILE_PATH + "NOT CONFIGURED");
    }

    ConfigurationManager.setup(appConfigFilePath);
    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    serverFilePath = configurationManager.getRequiredProperty("server.certificate");
    serverFilePassword = configurationManager.getRequiredProperty("server.certificate.password");
    destServer = configurationManager.getRequiredProperty("dest.server.host");

    SSLServerSocketFactory sslSocketFactory = getSSLSocketFactory();
    SSLServerSocket sslServerSocket = (SSLServerSocket)sslSocketFactory.createServerSocket(8081);
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    while (true) {
      Socket socket = sslServerSocket.accept();

      executorService.submit(() -> {
        safeProcessRequestAndSendResponse(socket);
      });
    }
  }

  private static void safeProcessRequestAndSendResponse(Socket socket) {
    try {
      processRequestAndSendResponse(socket);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void processRequestAndSendResponse(Socket socket) throws Exception {
    Packet packet = PacketReader.readPacket(socket, destServer);
    System.out.println("READ COMPLETE FROM CLIENT:");

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    String respFromServer = ClientHelper.makeAndReturnRequest(destServer, 443, packet.getPacketString());
    System.out.println("Forwarding: " + packet.getPacketString());
    System.out.println("Response: " + respFromServer);
    out.write(respFromServer);
    out.flush();
    out.close();
    socket.close();
  }


  private static SSLServerSocketFactory getSSLSocketFactory() throws Exception {
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
