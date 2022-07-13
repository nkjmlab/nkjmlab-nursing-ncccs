package org.nkjmlab.nursing.ncccs.javalin.client.jsonrpc;

import java.net.URL;
import java.time.LocalDate;
import org.nkjmlab.nursing.ncccs.javalin.NcccsApplication;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.NcccsRpcService.NcccsChartData;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.NcccsServiceInterface;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;
import org.nkjmlab.util.java.net.UrlUtils;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class NcccsJconRpcClient implements NcccsServiceInterface {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private final URL url;

  private final NcccsServiceInterface client;

  public NcccsJconRpcClient(String url) {
    this.url = UrlUtils.of(url);

    this.client = JsonRpcClientFactory.create(NcccsApplication.getDefaultJacksonMapper(),
        NcccsServiceInterface.class, this.url);
  }

  @Override
  public void sendAnswers(String userId, NcccsAnswer... answers) {
    client.sendAnswers(userId, answers);
  }

  @Override
  public NcccsAnswer[] getAnswers(String userId, LocalDate inputDate) {
    return client.getAnswers(userId, inputDate);
  }

  @Override
  public NcccsChartData getChartData(String userId) {
    return client.getChartData(userId);
  }


}
