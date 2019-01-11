package com.dawidmotyka.exchangeutils.credentialsprovider;

public class CredentialsNotAvailableException extends Exception {
    CredentialsNotAvailableException(String msg) {
        super(msg);
    }
}
