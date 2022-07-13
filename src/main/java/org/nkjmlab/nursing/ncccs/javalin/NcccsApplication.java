package org.nkjmlab.nursing.ncccs.javalin;

import java.io.File;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.NcccsRpcService;
import org.nkjmlab.nursing.ncccs.javalin.jsonrpc.UserAccountRpcService;
import org.nkjmlab.nursing.ncccs.javalin.model.NcccsAnswersTable;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable;
import org.nkjmlab.nursing.ncccs.javalin.model.UserAccountsTable.UserAccount;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.util.h2.H2LocalDataSourceFactory;
import org.nkjmlab.util.h2.H2ServerUtils;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.io.SystemFileUtils;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import org.nkjmlab.util.java.lang.SystemPropertyUtils;
import org.nkjmlab.util.javax.servlet.JsonRpcService;
import org.nkjmlab.util.javax.servlet.UserSession;
import org.nkjmlab.util.javax.servlet.ViewModel;
import org.nkjmlab.util.jsonrpc.JsonRpcRequest;
import org.nkjmlab.util.jsonrpc.JsonRpcResponse;
import org.nkjmlab.util.thymeleaf.TemplateEngineBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

public class NcccsApplication {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private static final File APP_ROOT_DIR = ResourceUtils.getResourceAsFile("/");
  private static final String WEBROOT_DIR_NAME = "/webroot";
  private static final File WEBROOT_DIR = new File(APP_ROOT_DIR, WEBROOT_DIR_NAME);
  private static final File USER_HOME_DIR = SystemFileUtils.getUserHomeDirectory();
  public static final File BACKUP_DIR = new File(USER_HOME_DIR, "go-bkup/");


  private final Javalin app;


  public static void main(String[] args) {
    int port = 2345;
    log.info("start (port:{}) => {}", port, SystemPropertyUtils.getJavaProperties());

    ProcessUtils.stopProcessBindingPortIfExists(port);
    H2ServerUtils.startDefaultTcpServerProcessAndWaitFor();
    H2ServerUtils.startDefaultWebConsoleServerProcessAndWaitFor();

    new NcccsApplication().start(port);
  }

  private void start(int port) {
    app.start(port);
  }

  public NcccsApplication() {

    log.info("log4j2.configurationFile={}, Logger level={}",
        System.getProperty("log4j2.configurationFile"), log.getLevel());


    H2LocalDataSourceFactory factory = createH2DataSourceFactory();
    log.info("server jdbcUrl={}", factory.getServerModeJdbcUrl());


    factory.createNewFileDatabaseIfNotExists();

    DataSource fileDbDataSource = factory.createServerModeDataSource();
    // H2Server.openBrowser(memDbDataSource, true);


    TemplateEngine engine =
        new TemplateEngineBuilder().setPrefix("/templates/").setTtlMs(1).build();
    engine.addDialect(new Java8TimeDialect());
    JavalinThymeleaf.configure(engine);

    this.app = Javalin.create(config -> {
      config.addStaticFiles(WEBROOT_DIR_NAME, Location.CLASSPATH);
      config.autogenerateEtags = true;
      config.enableCorsForAllOrigins();
      config.enableWebjars();
    });



    NcccsAnswersTable answersTable = new NcccsAnswersTable(Sorm.create(fileDbDataSource));
    answersTable.createTableIfNotExists().createIndexesIfNotExists();

    NcccsRpcService ncccsService = new NcccsRpcService(answersTable);
    JsonRpcService jsonRpcService = new JsonRpcService(getDefaultJacksonMapper());

    app.post("/app/json/NcccsRpcService", ctx -> {
      JsonRpcRequest jreq = jsonRpcService.toJsonRpcRequest(ctx.req);
      JsonRpcResponse jres = jsonRpcService.callHttpJsonRpc(ncccsService, jreq, ctx.res);
      String ret = getDefaultJacksonMapper().toJson(jres);
      ctx.result(ret).contentType("application/json");
    });

    UserAccountsTable userAccountsTable = new UserAccountsTable(Sorm.create(fileDbDataSource));
    userAccountsTable.createTableIfNotExists().createIndexesIfNotExists();

    app.post("/app/json/UserAccountRpcService", ctx -> {
      JsonRpcRequest jreq = jsonRpcService.toJsonRpcRequest(ctx.req);
      JsonRpcResponse jres = jsonRpcService
          .callHttpJsonRpc(new UserAccountRpcService(userAccountsTable, ctx.req), jreq, ctx.res);
      String ret = getDefaultJacksonMapper().toJson(jres);
      ctx.result(ret).contentType("application/json");
    });


    app.get("/app", ctx1 -> ctx1.redirect("/app/index.html"));

    app.get("/app/<pageName>", ctx -> {
      String pageName =
          ctx.pathParam("pageName") == null ? "index.html" : ctx.pathParam("pageName");
      ViewModel.Builder model = createDefaultModel(userAccountsTable, ctx.req);
      ctx.render(pageName, model.build().getMap());
    });
  }


  private H2LocalDataSourceFactory createH2DataSourceFactory() {
    try {
      return getDefaultJacksonMapper().toObject(ResourceUtils.getResourceAsFile("/conf/h2.json"),
          H2LocalDataSourceFactory.Builder.class).build();
    } catch (Exception e) {
      log.warn("Try to load h2.json.default");
      return getDefaultJacksonMapper()
          .toObject(ResourceUtils.getResourceAsFile("/conf/h2.json.default"),
              H2LocalDataSourceFactory.Builder.class)
          .build();
    }
  }

  public static String getJdbcUrlOfInMemoryDb(String dbName) {
    return "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1";
  }



  private ViewModel.Builder createDefaultModel(UserAccountsTable userAccountsTable,
      HttpServletRequest request) {
    ViewModel.Builder modelBuilder =
        ViewModel.builder().setFileModifiedDate(WEBROOT_DIR, 10, "js", "css");
    modelBuilder.put("currentUser", getCurrentUserAccount(userAccountsTable, request));
    return modelBuilder;
  }



  private UserAccount getCurrentUserAccount(UserAccountsTable UserAccountsTable,
      HttpServletRequest request) {
    Optional<UserAccount> u = UserSession.wrap(request.getSession()).getUserId()
        .map(uid -> UserAccountsTable.selectByPrimaryKey(uid));
    return u.orElse(new UserAccount());
  }



  public static JacksonMapper getDefaultJacksonMapper() {
    return JacksonMapper.getIgnoreUnknownPropertiesMapper();
  }


}
