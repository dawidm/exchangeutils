package com.dawidmotyka.exchangeutils.credentialsprovider;

public class ExchangeCredentials {
    private String login;
    private String pass;

    public ExchangeCredentials(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}
