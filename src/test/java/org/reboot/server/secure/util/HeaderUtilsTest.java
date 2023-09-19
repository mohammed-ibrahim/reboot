package org.reboot.server.secure.util;

import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.*;

public class HeaderUtilsTest {

  @Test
  public void canExtractCharsetFromHeader() {
    String header = String.format("Content-Type: text/html; charset=utf-8 ");
    Optional<String> charset = HeaderUtils.getHeaderFragment(header, "CHARset");
    assertTrue(charset.isPresent(), "Charset was expected to be present");
    assertEquals(charset.get(), "utf-8");
  }

  @Test
  public void canReturnEmptyFragmentIfNotPresentFromHeader() {
    String header = String.format("Content-Type: text/html; charset=utf-8 ");
    Optional<String> charset = HeaderUtils.getHeaderFragment(header, "hello");
    assertFalse(charset.isPresent(), "Fragment was not expected to be returned");
  }

}