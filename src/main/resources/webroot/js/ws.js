class UserStateWebSocket {

  constructor() {
    this.connection = null;
    this.sessionId = null;
    this.initialized = false;
  }

  getSessionId() {
    return this.sessionId;
  }

  getConnection() {
    return this.connection;
  }


  startNewWsConnection(userId) {

    function createConnection(userId) {
      const wsUrl = getWebSocketBaseUrl() + "?userId=" + userId;
      console.log("start open websocket = [" + wsUrl + "]");
      return new WebSocket(wsUrl);
    }

    const self = this;

    let connection = createConnection(userId);
    this.connection = connection;

    $(window).on('unload', function () {
      if (connection) {
        connection.onclose = function () {
        }
        connection.close();
      }
    });


    connection.onmessage = e => {
      const json = JSON.parse(e.data);
      switch (json.method) {
        case "INITIALIZE":
          _initialize(json.content);
          break;
        case "USER_STATE":
          _userState(json.content);
          break;
        default:
          console.error("invalid method name =>" + json.method);
      }

      function _initialize(json) {
        self.sessionId = json.sessionId;
        console.log("initialize=>" + JSON.stringify(json));
      }

      function _userState(json) {
        console.log("userState=>" + JSON.stringify(json));
      }

    };

    connection.onopen = e => {
      console.log("connection is open.");
      // console.log(stringifyEvent(e));
    };

    connection.onerror = e => {
      console.error("connection has an error.");
      swalAlert("ページを再読み込みします", "", "info", e => location.reload());
    };


    connection.onclose = e => {
      console.warn("connection is closed.");
      setTimeout(() => self.startNewWsConnection(userId), 500);
    };
  }

}


