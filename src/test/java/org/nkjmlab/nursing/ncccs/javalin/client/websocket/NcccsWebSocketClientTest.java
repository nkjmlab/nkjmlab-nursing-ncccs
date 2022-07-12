package org.nkjmlab.nursing.ncccs.javalin.client.websocket;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NcccsWebSocketClientTest {

  @BeforeAll
  static void setUp() {
    // NcccsApplication.main(new String[0]);
  }

  @Test
  void checkConTest() throws InterruptedException {
    NcccsWebSocketClient ws = new NcccsWebSocketClient("ws://localhost:2345/websocket/checkcon");
    ws.start();
    ws.connect();
    TimeUnit.SECONDS.sleep(1);
    ws.close();
  }


  @Test
  void stateTest() throws InterruptedException {
    NcccsWebSocketClient ws =
        new NcccsWebSocketClient("ws://localhost:2345/websocket/state?userId=user1");
    ws.start();
    ws.connect();
    TimeUnit.SECONDS.sleep(1);
    ws.close();
  }

}
