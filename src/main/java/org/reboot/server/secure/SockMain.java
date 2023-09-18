package org.reboot.server.secure;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.model.Packet;
import org.reboot.server.secure.reader.PacketReader;
import org.reboot.server.secure.util.Cfg;
import org.reboot.server.secure.util.IDGen;
import org.reboot.server.secure.util.PacketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SockMain {

  private static Logger log = LoggerFactory.getLogger(SockMain.class);
  public static final String CONFIG_FILE_PATH = "config.file.path";
  public static final String DEST_SERVER_HOST = "dest.server.host";
  public static String CRLF = "\r\n";

  private static String SEPARATOR = CRLF + "=============================================================================" + CRLF;

  private static String serverFilePath = null;
  private static String serverFilePassword = null;

  private static String dumpDir = null;


  public static void main(String[] args) throws Exception {

    String appConfigFilePath = System.getProperty(CONFIG_FILE_PATH);
    if (StringUtils.isBlank(appConfigFilePath)) {
      throw new RuntimeException(CONFIG_FILE_PATH + "NOT CONFIGURED");
    }

    Cfg.setup(appConfigFilePath);
    Cfg cfg = Cfg.getInstance();
    serverFilePath = cfg.getRequiredProperty("server.certificate");
    serverFilePassword = cfg.getRequiredProperty("server.certificate.password");
    dumpDir = cfg.getRequiredProperty("data.dump.dir");
    log.info("Starting server");
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
    Packet packet = PacketReader.readPacket(socket.getInputStream());
    String path = PacketUtils.getPathFromRequestPacket(packet);
    String newId = IDGen.newId(path);
    File file = Paths.get(dumpDir, newId).toFile();
    FileUtils.writeStringToFile(file, packet.getPacketString() + SEPARATOR, false);
    String destServer = Cfg.getInstance().getRequiredProperty(DEST_SERVER_HOST);
    PacketUtils.updateHostHeader(packet, destServer);

    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    String toServer = packet.getPacketString();
    FileUtils.writeStringToFile(file, toServer + SEPARATOR, true);
    Packet respFromServer = ClientHelper.makeAndReturnRequest(destServer, 443, toServer);
    String respFromServerStr = respFromServer.getPacketString();
    FileUtils.writeStringToFile(file, respFromServerStr + SEPARATOR, true);

    out.write(respFromServerStr);
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
