package org.nkjmlab.nursing.ncccs.javalin.websocket;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.nkjmlab.nursing.ncccs.javalin.NcccsApplication;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;
import org.nkjmlab.nursing.ncccs.javalin.websocket.WebsocketSessionsManager.WebsoketSessionsTable.WebSocketSession;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKey;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.concurrent.ForkJoinPoolUtils;
import org.nkjmlab.util.java.json.JsonMapper;
import io.javalin.websocket.WsMessageContext;

public class WebsocketSessionsManager {

  private static final JacksonMapper mapper = NcccsApplication.getDefaultJacksonMapper();

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();


  private final WebsoketSessionsTable websoketSessionsTable;
  private final WebSocketJsonSenderService jsonSenderService = new WebSocketJsonSenderService();


  public WebsocketSessionsManager(DataSource memDbDataSource) {
    this.websoketSessionsTable = new WebsoketSessionsTable(memDbDataSource);
    this.websoketSessionsTable.createTableIfNotExists().createIndexesIfNotExists();

  }

  public void updateState(String userId, NcccsAnswer... answers) {
    List<Session> sessions = websoketSessionsTable.selectSessionsByUserId(userId);
    log.debug("Size of sessions for {} is {}", userId, sessions.size());
    jsonSenderService.submit(sessions, MethodName.USER_STATE, answers);
  }


  public void onMessage(WsMessageContext ctx) {}

  public void onClose(Session session, int statusCode, String reason) {
    session.close();
    log.info("@{} is closed. status code={}, reason={}", session.hashCode(), statusCode, reason);

    websoketSessionsTable.removeSession(session).ifPresent(userId -> {
    });

  }

  public void updateSession(int sessionId, String userId) {
    websoketSessionsTable.updateSession(sessionId, userId);
  }


  public void onConnect(Session session, String userId) {
    websoketSessionsTable.registerSession(session, userId);
    jsonSenderService.submit(session, MethodName.INITIALIZE, userId);
  }



  public void onError(Session session, Throwable cause) {
    log.error(cause);
  }

  private enum MethodName {
    USER_STATE, INITIALIZE
  }

  private static class WebSocketJsonSenderService {
    private static final org.apache.logging.log4j.Logger log =
        org.apache.logging.log4j.LogManager.getLogger();

    private static final ExecutorService srv =
        Executors.newFixedThreadPool(ForkJoinPoolUtils.getAvailableProcessorsMinus(2));
    private static final JsonMapper mapper = NcccsApplication.getDefaultJacksonMapper();


    public WebSocketJsonSenderService() {}

    private void submit(List<Session> sessions, MethodName methodName, Object json) {
      String text = mapper.toJson(new WebsocketJson(methodName.toString(), json));
      sessions.forEach(session -> submitText(session, methodName, text));
    }


    private void submit(Session session, MethodName methodName, Object json) {
      String text = mapper.toJson(new WebsocketJson(methodName.toString(), json));
      submitText(session, methodName, text);
    }

    private void submitText(Session session, MethodName methodName, String text) {
      srv.submit(() -> sendText(session, text));
    }

    private void sendText(Session session, String text) {
      RemoteEndpoint b = session.getRemote();
      synchronized (b) {
        b.sendString(text, new WriteCallback() {
          @Override
          public void writeFailed(Throwable x) {
            try {
              b.sendString(text);
            } catch (IOException e) {
              log.error(e.getMessage());
            }
          }

          @Override
          public void writeSuccess() {}
        });
      }
    }

    private static class WebsocketJson {
      public final String method;
      public final Object content;

      public WebsocketJson(String method, Object content) {
        this.method = method;
        this.content = content;
      }

      @Override
      public String toString() {
        return "WebsocketJson [method=" + method + ", content=" + content + "]";
      }
    }
  }

  public static class WebsoketSessionsTable extends BasicH2Table<WebSocketSession> {


    private static Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    public WebsoketSessionsTable(DataSource dataSource) {
      super(Sorm.create(dataSource), WebSocketSession.class);
    }

    List<Session> selectSessionsByUserId(String userId) {
      return selectListAllEqual("USER_ID", userId).stream().map(ws -> sessions.get(ws.sessionId))
          .toList();
    }

    void registerSession(Session session, String userId) {
      int sessionId = session.hashCode();
      WebSocketSession ws = new WebSocketSession(sessionId, userId, LocalDateTime.now());
      if (exists(ws)) {
        log.warn("{} already exists.", ws);
        return;
      }
      insert(ws);
      sessions.put(sessionId, session);
      log.info("WebSocket is registered={}", ws);
    }

    void updateSession(int sessionId, String userId) {
      update(new WebSocketSession(sessionId, userId, LocalDateTime.now()));

    }

    Optional<String> removeSession(Session session) {
      if (sessions.entrySet().removeIf(e -> e.getKey() == session.hashCode())) {
        WebSocketSession gs = selectByPrimaryKey(session.hashCode());
        delete(gs);
        return Optional.of(gs.userId());
      }
      return Optional.empty();

    }



    @OrmRecord
    public static record WebSocketSession(@PrimaryKey int sessionId, String userId,
        LocalDateTime createdAt) {
    }

  }



}
