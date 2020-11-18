/*
 * Cryptonose2
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

package pl.dmotyka.exchangeutils.xtb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.AllSymbolsResponse;
import pro.xstore.api.sync.ServerData;

public class XtbPairDataProvider implements PairDataProvider {

    private static final Logger logger = Logger.getLogger(XtbPairDataProvider.class.getName());

    private final XtbConnectionManager xtbConnectionManager=new XtbConnectionManager(ServerData.ServerEnum.REAL);

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws IOException {
        return new String[0];
    }

    @Override
    public String[] getPairsApiSymbols() throws IOException {
        try {
            if(!xtbConnectionManager.isConnected())
                xtbConnectionManager.connect();
            synchronized (xtbConnectionManager.getConnectorLock()) {
                AllSymbolsResponse response = APICommandFactory.executeAllSymbolsCommand(xtbConnectionManager.getConnector());
                logger.finer(String.format("got %d symbols", response.getSymbolRecords().size()));
                return response.getSymbolRecords().stream().map(symbolRecord -> String.format("%s__%s", symbolRecord.getSymbol(), symbolRecord.getCurrency())).toArray(String[]::new);
            }
        } catch (ExchangeCommunicationException | APICommunicationException | APIReplyParseException | APIErrorResponse | APICommandConstructionException e) {
            logger.log(Level.SEVERE,"when getting all symbols",e);
            throw new IOException("when getting all symbols");
        }
    }
}
