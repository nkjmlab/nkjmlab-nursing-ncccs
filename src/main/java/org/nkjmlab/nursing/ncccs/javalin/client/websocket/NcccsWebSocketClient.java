package org.nkjmlab.nursing.ncccs.javalin.client.websocket;

import java.net.URI;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.util.java.net.UriUtils;

public class NcccsWebSocketClient {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private final URI uri;
  private final WebSocketClient client = new WebSocketClient();

  public NcccsWebSocketClient(String uri) {
    this.uri = UriUtils.of(uri);
  }

  public void start() {
    try {
      client.start();
    } catch (Exception e) {
      throw Try.rethrow(e);
    }
  }

  public void connect() {
    try {
      client.connect(new NcccsWebSocket(uri), uri, new ClientUpgradeRequest());
    } catch (Exception e) {
      throw Try.rethrow(e);
    }
  }

  public void close() {
    client.destroy();
  }


  @WebSocket
  public static class NcccsWebSocket {

    private final URI uri;

    public NcccsWebSocket(URI uri) {
      this.uri = uri;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
      log.info("onConnect url={}, session={}", uri, session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
      log.info("onMessage uri={}, session={}, message={}", uri, session, message);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
      log.info("{}, {},{},{}", uri, session, statusCode, reason);

    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
      log.error("{}", uri);
      log.error(cause, cause);
    }
  }


}
