package org.reboot.server.secure.h2;

import org.reboot.server.secure.model.FrameDetails;
import org.reboot.server.secure.model.FrameType;
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
  public void canDetermineFrameDetails() {
    String inputB64Data = "AAAuAQUAAAABgodBiqDkHROdCbjwHg+EeogltlDDy4Vxf1MDKi8qQIIcZANkZWZAggiZAzQ1Ng==";
    int expectedLength = 46;

    FrameDetails frameDetails = frameDecoder.getFrameDetails(Base64.getDecoder().decode(inputB64Data));
    assertEquals(frameDetails.getFrameLength(), expectedLength);
    assertEquals(frameDetails.getFrameType(), FrameType.HEADERS);
    assertEquals(frameDetails.getStreamId(), 1);

    inputB64Data = "AABWAQQAAAABg4dBiqDkHROdCbjwHg8El2KxpnirHYmILFY0zxVjsTEL/K7CpT6/eogltlDDy4Vxf1MDKi8qDw0DNTg5X5gdddBiDSY9THlbx48LSnspWtsoLUQ8hZM=";
    expectedLength = 86;

    frameDetails = frameDecoder.getFrameDetails(Base64.getDecoder().decode(inputB64Data));
    assertEquals(frameDetails.getFrameLength(), expectedLength);
    assertEquals(frameDetails.getFrameType(), FrameType.HEADERS);
    assertEquals(frameDetails.getStreamId(), 1);
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

  @DataProvider(name = "msbMaskData")
  protected Object[][] getMsbMaskData() {
    return new Object[][]{
        {(byte)0x00, (byte)0X00},
        {(byte)0xD0, (byte)0X50},
        {(byte)0xF0, (byte)0X70},
        {(byte)0xFF, (byte)0X7F},
        {(byte)0xEE, (byte)0X6E},
        {(byte)0x80, (byte)0X00},
        {(byte)0xCF, (byte)0X4F}
    };
  }
  @Test(dataProvider = "msbMaskData")
  public void canMaskMsbBitToZero(byte before, byte expectedAfterMasking) {
    byte actual = frameDecoder.maskMsbToZero(before);
    assertEquals(actual, expectedAfterMasking);
  }

  @DataProvider(name = "streamIdentifierData")
  protected Object[][] getStreamIdentifierData() {
    return new Object[][]{
        {getHeaderFrameArray((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00), 0},
        {getHeaderFrameArray((byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00), 0},

        {getHeaderFrameArray((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01), 1},
        {getHeaderFrameArray((byte)0x80, (byte)0x00, (byte)0x00, (byte)0x01), 1},

        {getHeaderFrameArray((byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF), 255},
        {getHeaderFrameArray((byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF), 65535},
        {getHeaderFrameArray((byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF), 16777215},
        {getHeaderFrameArray((byte)0x11, (byte)0xFF, (byte)0xFF, (byte)0xFF), 301989887},

        {getHeaderFrameArray((byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE), 1861152494},

        {getHeaderFrameArray((byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00), 1073741824},
        {getHeaderFrameArray((byte)0x40, (byte)0x00, (byte)0x00, (byte)0x00), 1073741824},

        {getHeaderFrameArray((byte)0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF), 2147483647},
        {getHeaderFrameArray((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF), 2147483647}
    };
  }

  private byte[] getHeaderFrameArray(byte b1, byte b2, byte b3, byte b4) {
    byte[] buffer = new byte[20];

    buffer[5] = b1;
    buffer[6] = b2;
    buffer[7] = b3;
    buffer[8] = b4;

    return buffer;
  }

  @Test(dataProvider = "streamIdentifierData")
  public void canIdentifyStreamId(byte[] frameBuffer, int expectedIntegerIdentifier) {
    int actualIdentifier = frameDecoder.getStreamIdentifier(frameBuffer);
    assertEquals(actualIdentifier, expectedIntegerIdentifier);
  }

}