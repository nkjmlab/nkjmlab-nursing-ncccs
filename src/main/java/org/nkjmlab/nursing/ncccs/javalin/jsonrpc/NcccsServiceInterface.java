package org.nkjmlab.nursing.ncccs.javalin.jsonrpc;

import java.time.LocalDate;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.NcccsRpcService.ChartData;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;

public interface NcccsServiceInterface {


  void sendAnswers(String userId, NcccsAnswer... answers);

  NcccsAnswer[] getAnswers(String userId, LocalDate inputDate);

  ChartData getChartData(String userId);



}
