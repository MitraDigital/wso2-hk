function onLoginRequest(context) {
    var token = context.request.headers["token"];

    if(token != null || token != undefined){
        Log.info("Token found in header and activated Token authenticator..!");
        executeStep(2);
    } else {
        Log.info("Token not found in header and activated Extended basic authenticator..!");
        executeStep(1);
    }
}