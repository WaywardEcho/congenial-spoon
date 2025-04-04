package app;

public enum LoginState {
    REQUEST_USERNAME,
    RECEIVE_USERNAME_RESPONSE,
    REQUEST_PASSWORD,
    RECEIVE_PASSWORD_RESPONSE,
    ACCOUNT_CREATION,
    DONE;
    
    public LoginState next(String serverResponse) {
        if (this == RECEIVE_USERNAME_RESPONSE) {
            if (serverResponse.contains("Username found")) {
                return REQUEST_PASSWORD;
            } else if (serverResponse.contains("Username not found")) {
                return ACCOUNT_CREATION;
            }
        } else if (this == RECEIVE_PASSWORD_RESPONSE) {
            if (serverResponse.contains("Login successful")) {
                return DONE;
            } else {
                // Optionally, repeat the login process or ask again.
                return REQUEST_USERNAME;
            }
        } else if (this == ACCOUNT_CREATION) {
            if (serverResponse.contains("Login successful")) {
                return DONE;
            } else {
                return REQUEST_USERNAME;
            }
        }
        return this;
    }
}
