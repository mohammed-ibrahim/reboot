package org.reboot.server.secure.core.stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.reboot.server.secure.core.stream.HttpStreamerImpl.CRLF;
import static org.testng.Assert.*;

public class HttpStreamerImplTest {

  public static String TEMPLATE = "HTTP/1.1 200 OK\r\nContent-Length: %s\r\n\r\n%s";

  @Test
  public void canStreamContentLength() throws Exception {
    String input = String.format(TEMPLATE, "5", "abcde");

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(byteArrayInputStream, byteArrayOutputStream, getHeadProcessor());
    httpStreamer.stream();

    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }

  @Test
  public void canStreamSimpleGetLength() throws Exception {
    String input = "GET / HTTP/2" + CRLF +
        "Host: hello.com" + CRLF +
        "User-Agent: curl/8.1.2" + CRLF +
        "Accept: */*" + CRLF;

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes());
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(byteArrayInputStream, byteArrayOutputStream, getHeadProcessor());
    httpStreamer.stream();

    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }

  @Test
  public void canStreamChunkedEncoding() throws Exception {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    InputStream resourceAsStream = this.getClass().getResourceAsStream("/chunked-sample-1.txt");
    HttpStreamerImpl httpStreamer = new HttpStreamerImpl(resourceAsStream, byteArrayOutputStream, getHeadProcessor());
    httpStreamer.stream();

    String input = IOUtils.toString(this.getClass().getResourceAsStream("/chunked-sample-1.txt"));
    String output = new String(byteArrayOutputStream.toByteArray());
    assertEquals(output, input);
  }

  private IHeaderProcessor getHeadProcessor() {
    return new HeaderProcessorImpl(null, false);
  }
}