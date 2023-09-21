package org.reboot.server.secure.reader;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.*;

public class StreamReaderTest {

  @Test
  public void canReadSingleLine() throws IOException {
    String input = "abcd";
    String sample = input + StreamReader.CRLF;
    InputStream inputStream = new ByteArrayInputStream(sample.getBytes());
    byte[] read = StreamReader.readLineBytes(inputStream);

    String obtained = new String(read);
    assertEquals(obtained, input);
  }

  @Test
  public void canReadSingleLineWithoutNewLine() throws IOException {
    String input = "abcd";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes());
    byte[] read = StreamReader.readLineBytes(inputStream);

    String obtained = new String(read);
    assertEquals(obtained, input);
  }

  @Test
  public void canReadEmptyLine() throws Exception {
    String input = StreamReader.CRLF;
    InputStream inputStream = new ByteArrayInputStream(input.getBytes());
    byte[] read = StreamReader.readLineBytes(inputStream);

    String obtained = new String(read);
    assertEquals(obtained, "");
  }


  @Test
  public void canReadBytes() throws Exception {
    String input = "hello world";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes());
    String str = StreamReader.readBytes(inputStream, 5);

    assertEquals(str, "hello");
  }



}