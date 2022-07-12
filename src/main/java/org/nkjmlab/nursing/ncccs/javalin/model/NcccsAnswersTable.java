package org.nkjmlab.nursing.ncccs.javalin.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable.NcccsAnswer;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.sql.ParameterizedSqlParser;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKeyColumns;

public class NcccsAnswersTable extends BasicH2Table<NcccsAnswer> {

  public NcccsAnswersTable(Sorm sorm) {
    super(sorm, NcccsAnswer.class);
  }

  @OrmRecord
  @PrimaryKeyColumns({"user_id", "input_date", "question_number"})
  public static record NcccsAnswer(String userId, LocalDate inputDate, int questionNumber,
      int questionValue) {

  }

  public NcccsAnswer[] getAnswers(String userId, LocalDate inputDate) {
    return selectListAllEqual("user_id", userId, "input_date", inputDate)
        .toArray(NcccsAnswer[]::new);
  }

  public List<LocalDate> selectInputDateByUserId(String userId) {
    return getOrm().readList(LocalDate.class,
        "select distinct input_date from " + getTableName() + " where user_id=?", userId);
  }

  public int getScore(String userId, LocalDate inputDate, Integer... questionNumbers) {

    return getOrm().readOne(int.class,
        ParameterizedSqlParser.parse(
            "select sum(question_value) from " + getTableName()
                + " where user_id=? and input_date=? and question_number in (<?>)",
            userId, inputDate, Stream.of(questionNumbers).toList()));
  }

}
