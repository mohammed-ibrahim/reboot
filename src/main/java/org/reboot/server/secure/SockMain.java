package org.reboot.server.secure;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.reader.PacketReader;
import org.reboot.server.secure.util.Cfg;
import org.reboot.server.secure.util.IDGen;
import org.reboot.server.secure.util.PacketUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SockMain {

  public static final String CONFIG_FILE_PATH = "config.file.path";
  public static String CRLF = "\r\n";

  private static String SEPARATOR = CRLF + "=============================================================================" + CRLF;

  private static String serverFilePath = null;
  private static String serverFilePassword = null;

  private static String dumpDir = null;

  private static String destServer = null;

  public static void main(String[] args) throws Exception {

    String appConfigFilePath = System.getProperty(CONFIG_FILE_PATH);
    if (StringUtils.isBlank(appConfigFilePath)) {
      throw new RuntimeException(CONFIG_FILE_PATH + "NOT CONFIGURED");
    }

    Cfg.setup(appConfigFilePath);
    Cfg cfg = Cfg.getInstance();
    serverFilePath = cfg.getRequiredProperty("server.certificate");
    serverFilePassword = cfg.getRequiredProperty("server.certificate.password");
    destServer = cfg.getRequiredProperty("dest.server.host");
    dumpDir = cfg.getRequiredProperty("data.dump.dir");

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
    Packet packet = PacketReader.readPacket(socket);
    String path = PacketUtils.getPathFromRequestPacket(packet);
    String newId = IDGen.newId(path);
    File file = Paths.get(dumpDir, newId).toFile();
    FileUtils.writeStringToFile(file, packet.getPacketString() + SEPARATOR, false);
    PacketUtils.updateHostHeader(packet, destServer);

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    String toServer = packet.getPacketString();
    FileUtils.writeStringToFile(file, toServer + SEPARATOR, true);
    String respFromServer = ClientHelper.makeAndReturnRequest(destServer, 443, toServer);
    FileUtils.writeStringToFile(file, respFromServer + SEPARATOR, true);

    out.write(respFromServer);
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