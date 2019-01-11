package com.dawidmotyka.exchangeutils.credentialsprovider;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialsProvider {

    private static final Logger logger = Logger.getLogger(CredentialsProvider.class.getName());
    
    public static ExchangeCredentials getCredentials(ExchangeSpecs exchangeSpecs) throws CredentialsNotAvailableException {
        return getCredentials(exchangeSpecs.getClass());
    }

    public static ExchangeCredentials getCredentials(Class<? extends ExchangeSpecs> exchangeSpecsClass) throws CredentialsNotAvailableException {
        Properties properties = new Properties();
        String filePath=exchangeSpecsClass.getSimpleName()+".settings";
        try {
            if(!Files.exists(Paths.get(filePath))) {
                properties.setProperty("login","");
                properties.setProperty("pass","");
                properties.store(new FileOutputStream(filePath),exchangeSpecsClass.getSimpleName()+" credentials");
                throw new CredentialsNotAvailableException("no credentials found in " + filePath);
            }
            properties.load(new FileInputStream(filePath));
            String login=properties.getProperty("login");
            String pass=properties.getProperty("pass");
            if(login==null || login.length()==0 || pass==null || pass.length()==0)
                throw new CredentialsNotAvailableException("no credentials found in " + filePath);
            return new ExchangeCredentials(login,pass);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"when reading credentials for " + exchangeSpecsClass.getSimpleName(),e);
            throw new CredentialsNotAvailableException("no credentials found in " + filePath);
        }
    }
}
