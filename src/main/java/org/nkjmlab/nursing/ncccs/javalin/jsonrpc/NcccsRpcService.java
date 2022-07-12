package org.nkjmlab.nursing.ncccs.javalin.jsonrpc;

import java.time.LocalDate;
import java.util.List;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.NcccsRpcService.ChartData.DataSet;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable;
import org.nkjmlab.nursing.ncccs.javalin.websocket.WebsocketSessionsManager;

public class NcccsRpcService implements NcccsServiceInterface {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();


  private final WebsocketSessionsManager webSocketManager;
  private final UserAccountsTable userAccountsTable;
  private final NcccsAnswersTable answersTable;


  public NcccsRpcService(WebsocketSessionsManager webSocketManager,
      UserAccountsTable userAccountsTable, NcccsAnswersTable answersTable) {
    this.webSocketManager = webSocketManager;
    this.userAccountsTable = userAccountsTable;
    this.answersTable = answersTable;

    userAccountsTable.createTableIfNotExists().createIndexesIfNotExists();
    answersTable.createTableIfNotExists().createIndexesIfNotExists();
  }


  @Override
  public void sendAnswers(String userId, NcccsAnswer... answers) {
    log.debug("receive: userId={},answers={}", userId, answers);
    answersTable.merge(answers);
  }


  @Override
  public NcccsAnswer[] getAnswers(String userId, LocalDate inputDate) {
    log.debug("receive: userId={}, inputDate={}", userId, inputDate);
    NcccsAnswer[] ret = answersTable.getAnswers(userId, inputDate);
    return ret;
  }


  @Override
  public ChartData getChartData(String userId) {
    List<LocalDate> inputDates = answersTable.selectInputDateByUserId(userId);


    String[] labels = inputDates.stream().map(d -> d.toString()).toArray(String[]::new);


    DataSet d1 = new DataSet("カテゴリ1", inputDates.stream()
        .map(d -> answersTable.getScore(userId, d, 1, 2)).toArray(Integer[]::new), "#f88");
    DataSet d2 = new DataSet("カテゴリ2", inputDates.stream()
        .map(d -> answersTable.getScore(userId, d, 3, 4)).toArray(Integer[]::new), "#484");
    DataSet d3 = new DataSet("カテゴリ3", inputDates.stream()
        .map(d -> answersTable.getScore(userId, d, 5, 6)).toArray(Integer[]::new), "#48f");

    return new ChartData(labels, new DataSet[] {d1, d2, d3});
  }


  public static record ChartData(String[] labels, DataSet[] datasets) {
    public static record DataSet(String label, Integer[] data, String borderColor) {

    }
  }


}
