package org.nkjmlab.nursing.ncccs.javalin.jsonrpc;

import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable.UserAccount;

public interface UserAccountRpcServiceInterface {

  UserAccount login(String userId, String password);

  boolean logout();

}
