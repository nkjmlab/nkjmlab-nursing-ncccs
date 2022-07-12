package org.nkjmlab.nursing.ncccs.javalin.client.websocket;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;

class NcccsJconRpcClientTest {

  @BeforeAll
  static void setUp() {

    // NcccsApplication.main(new String[0]);

  }


  @Test
  void test() {
    NcccsJconRpcClient client =
        new NcccsJconRpcClient("http://localhost:2345/app/json/NcccsRpcService");

    client.sendAnswers("user1", new NcccsAnswer("user1", LocalDate.now(), 1, 1));

  }

}
