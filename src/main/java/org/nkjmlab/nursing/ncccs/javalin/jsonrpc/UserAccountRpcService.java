package org.nkjmlab.nursing.ncccs.javalin.jsonrpc;

import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable.UserAccount;
import org.nkjmlab.util.javax.servlet.UserSession;

public class UserAccountRpcService implements UserAccountRpcServiceInterface {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private final UserAccountsTable userAccountsTable;

  private final HttpServletRequest request;

  public UserAccountRpcService(UserAccountsTable userAccountsTable, HttpServletRequest request) {
    this.userAccountsTable = userAccountsTable;
    this.request = request;
  }

  @Override
  public UserAccount login(String userId, String password) {
    UserSession userSession = UserSession.wrap(request.getSession());
    UserAccount userAccount = userAccountsTable.selectByPrimaryKey(userId);
    if (userAccount == null) {
      throw new RuntimeException(userId + " is not registered.");
    }
    if (!userAccount.validate(password)) {
      throw new RuntimeException("Password is not correct.");
    }
    userAccountsTable.update(userAccount.createUpdateLastLogin(LocalDateTime.now()));
    userSession.setMaxInactiveInterval(10 * 60 * 60);
    userSession.setUserId(userId);

    log.info("{} is logined. login session id={}", userSession.getSessionId(),
        userSession.getSession().getId());
    return userAccount;
  }


  @Override
  public boolean logout() {
    UserSession.wrap(request.getSession()).invalidate();
    return true;
  }

}
