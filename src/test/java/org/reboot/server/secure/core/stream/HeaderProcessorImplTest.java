package org.reboot.server.secure.core.stream;

import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;

import static org.testng.Assert.*;

public class HeaderProcessorImplTest {


  private static String CONTENT_LENGTH = "Content-length: 80";
  private static String TRANSFER_ENCODING = "transfer-encoding: chunked";
  private static String NEW_HOST = "newhost";
  private static String OLD_HOST_HEADER = "hoST: oldhost";
  private static String NEW_HOST_HEADER = "host: " + NEW_HOST;


  @Test
  public void canDetectContentLength() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(NEW_HOST);

    headerProcessor.writeHeader(CONTENT_LENGTH.getBytes(), byteArrayOutputStream);

    assertTrue(headerProcessor.hasBody());
    assertEquals(headerProcessor.getContentLength(), 80);
    assertFalse(headerProcessor.isChunkedPacket());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, CONTENT_LENGTH);
  }

  @Test
  public void canDetectTransferEncoding() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(NEW_HOST);

    headerProcessor.writeHeader(TRANSFER_ENCODING.getBytes(), byteArrayOutputStream);

    assertTrue(headerProcessor.hasBody());
    assertFalse(headerProcessor.isContentLength());
    assertTrue(headerProcessor.isChunkedPacket());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, TRANSFER_ENCODING);
  }

  @Test
  public void canReplaceNewHost() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HeaderProcessorImpl headerProcessor = new HeaderProcessorImpl(NEW_HOST);

    headerProcessor.writeHeader(OLD_HOST_HEADER.getBytes(), byteArrayOutputStream);

    assertFalse(headerProcessor.hasBody());
    String processed = new String(byteArrayOutputStream.toByteArray());
    assertEquals(processed, NEW_HOST_HEADER);
  }

}