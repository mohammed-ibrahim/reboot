package org.reboot.server.secure.core.stream;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.testng.Assert.*;

public class HttpStreamerImplTest {

  public static String TEMPLATE = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";

  @Test
  public void canStreamSimpleGet() throws Exception {
    String input = String.format(TEMPLATE, "5", "abcde");

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(byteArrayInputStream, byteArrayOutputStream, getHeadProcessor());
    httpStreamer.stream();

    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }


  private IHeaderProcessor getHeadProcessor() {
    return new HeaderProcessorImpl(null, false);
  }
}