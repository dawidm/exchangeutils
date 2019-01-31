package com.dawidmotyka.exchangeutils.xtb;

import com.dawidmotyka.exchangeutils.ExchangeCommunicationException;
import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsNotAvailableException;
import com.dawidmotyka.exchangeutils.credentialsprovider.CredentialsProvider;
import com.dawidmotyka.exchangeutils.credentialsprovider.ExchangeCredentials;
import com.dawidmotyka.exchangeutils.exchangespecs.XtbExchangeSpecs;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.LoginResponse;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XtbConnectionManager {

    Logger logger = Logger.getLogger(XtbConnectionManager.class.getName());

    public static final int PING_API_PERIOD_SECONDS=60;

    private SyncAPIConnector connector;
    private Object connectorLock=new Object();
    private final ServerData.ServerEnum serverEnum;
    private ExchangeCredentials exchangeCredentials;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture pingApiScheduledFuture;
    private final AtomicBoolean connected = new AtomicBoolean(false);


    public XtbConnectionManager(ServerData.ServerEnum serverEnum) {
        this.serverEnum=serverEnum;
    }

    public XtbConnectionManager(ServerData.ServerEnum serverEnum, ExchangeCredentials exchangeCredentials) {
        this.serverEnum=serverEnum;
        this.exchangeCredentials=exchangeCredentials;
    }

    public void connect() throws ExchangeCommunicationException{
        try {
            synchronized (connectorLock) {
                connector = new SyncAPIConnector(serverEnum);
                if(exchangeCredentials==null) {
                    exchangeCredentials = CredentialsProvider.getCredentials(XtbExchangeSpecs.class);
                }
                Credentials credentials = new Credentials(exchangeCredentials.getLogin(), exchangeCredentials.getPass(), "exchangeutils");
                LoginResponse loginResponse = APICommandFactory.executeLoginCommand(
                        connector,
                        credentials);
                logger.info(loginResponse.toString());
                if(loginResponse.getStatus()==true) {
                    connected.set(true);
                    pingApiScheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::pingAPIExecutor, PING_API_PERIOD_SECONDS, PING_API_PERIOD_SECONDS, TimeUnit.SECONDS);
                } else {
                    throw new ExchangeCommunicationException("login failed");
                }
            }
        } catch (IOException | APIReplyParseException | APICommunicationException | APICommandConstructionException | APIErrorResponse | CredentialsNotAvailableException e) {
            logger.log(Level.SEVERE,"when connecting to xtb",e);
            throw new ExchangeCommunicationException(e.getClass().getName()+e.getMessage());
        }
    }

    public void disconnect() throws ExchangeCommunicationException {
        if(pingApiScheduledFuture!=null)
            pingApiScheduledFuture.cancel(true);
        synchronized (connectorLock) {
            if (connector != null) {
                logger.info("disconnecting SyncAPIConnector stream");
                connector.disconnectStream();
                try {
                    connector.close();
                } catch (APICommunicationException e) {
                    logger.log(Level.WARNING, "when disconnecting SyncAPIConnector", e);
                    throw new ExchangeCommunicationException(e.getClass().getName()+e.getLocalizedMessage());
                }
            }
        }
    }

    private void pingAPIExecutor() {
        logger.fine("pinging API");
        synchronized (connectorLock) {
            try {
                APICommandFactory.executePingCommand(connector);
            } catch (APICommunicationException e) {
                logger.log(Level.WARNING,"when pinging api",e);
                connected.set(false);
                try {
                    disconnect();
                    connect();
                } catch (ExchangeCommunicationException e1) {
                    logger.warning("when reconnecting "+e1.getMessage() + e1.getMessage());
                }
            } catch (APICommandConstructionException | APIErrorResponse | APIReplyParseException e) {
                logger.log(Level.WARNING,"when pinging api",e);
            }
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public SyncAPIConnector getConnector() {
        return connector;
    }

    public Object getConnectorLock() {
        return connectorLock;
    }

}
