package org.nkjmlab.nursing.ncccs.javalin.model;

import java.time.LocalDateTime;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable.UserAccount;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.annotation.OrmRecord;
import org.nkjmlab.sorm4j.util.h2.BasicH2Table;
import org.nkjmlab.sorm4j.util.table_def.annotation.PrimaryKey;

public class UserAccountsTable extends BasicH2Table<UserAccount> {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public UserAccountsTable(Sorm sorm) {
    super(sorm, UserAccount.class);
  }

  @OrmRecord
  public static record UserAccount(@PrimaryKey String id, String password,
      LocalDateTime lastLoginAt) {

    public UserAccount() {
      this("", "", LocalDateTime.MIN);
    }

    public boolean validate(String password) {
      return password.equals(password);
    }

    public UserAccount createUpdateLastLogin(LocalDateTime lastLoginAt) {
      return new UserAccount(id, password, lastLoginAt);
    }

  }


}
