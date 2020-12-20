/*
 * Cryptonose
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.credentialsprovider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

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
