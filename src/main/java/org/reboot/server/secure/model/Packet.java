package org.reboot.server.secure.model;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reboot.server.secure.SockMain;

import java.util.List;
public class Packet {

  private List<String> head;

  private String body;

  public List<String> getHead() {
    return head;
  }

  public void setHead(List<String> head) {
    this.head = head;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getPacketString() {
    if (CollectionUtils.isEmpty(this.head)) {
      throw new RuntimeException("Empty head");
    }

    String headStr = StringUtils.join(head, SockMain.CRLF);

    if (StringUtils.isNotBlank(this.body)) {
      return headStr + SockMain.CRLF + this.body;
    }

    return headStr + SockMain.CRLF;
  }

  public byte[] getBytes() {
    return getPacketString().getBytes();
  }
}
