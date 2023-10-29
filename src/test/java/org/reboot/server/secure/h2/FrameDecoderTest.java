package org.reboot.server.secure.h2;

import org.reboot.server.secure.model.FrameDetails;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Base64;

import static org.testng.Assert.*;

public class FrameDecoderTest {

  private FrameDecoder frameDecoder;
  @BeforeTest
  public void setup() {
    frameDecoder = new FrameDecoder();
  }

  @Test
  public void canDetermineFrameLength() {
    String inputB64Data = "AAAuAQUAAAABgodBiqDkHROdCbjwHg+EeogltlDDy4Vxf1MDKi8qQIIcZANkZWZAggiZAzQ1Ng==";
    int expectedLength = 46;

    FrameDetails frameDetails = frameDecoder.getFrameDetails(Base64.getDecoder().decode(inputB64Data));
    assertEquals(frameDetails.getFrameLength(), expectedLength);

    inputB64Data = "AABWAQQAAAABg4dBiqDkHROdCbjwHg8El2KxpnirHYmILFY0zxVjsTEL/K7CpT6/eogltlDDy4Vxf1MDKi8qDw0DNTg5X5gdddBiDSY9THlbx48LSnspWtsoLUQ8hZM=";
    expectedLength = 86;

    frameDetails = frameDecoder.getFrameDetails(Base64.getDecoder().decode(inputB64Data));
    assertEquals(frameDetails.getFrameLength(), expectedLength);
  }


  @DataProvider(name = "getDataSetForLengthCalculation")
  protected Object[][] getDataSetForLengthCalculation() {
    return new Object[][]{
        {new byte[] {0x00, 0x01, 0x00}, 256},
        {new byte[] {0x01, 0x00, 0x00}, 65536},
        {new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF}, 16777215},
        {new byte[] {(byte)0x80, (byte)0x80, (byte)0x80}, 8421504},
        {new byte[] {0x00, 0x00, 0x00}, 0},
    };
  }

  @Test(dataProvider = "getDataSetForLengthCalculation")
  public void isAbleToCalculateFrameLengthCorrectly(byte[] byteArray, int expected) {
    int actual = frameDecoder.getPacketLength(byteArray);
    assertEquals(actual, expected);
  }

}