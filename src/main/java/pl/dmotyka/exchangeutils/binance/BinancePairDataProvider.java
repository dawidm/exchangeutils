/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.exchangeutils.binance;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinancePrice;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

public class BinancePairDataProvider implements PairDataProvider {

    private static final Logger logger = Logger.getLogger(BinancePairDataProvider.class.getName());

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            BinanceMarketDataService binanceMarketDataService = ((BinanceMarketDataService)ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(BinanceExchange.class)).getMarketDataService());
            Map<String, Double> criteriaMap = new HashMap<>();
            Arrays.stream(pairSelectionCriteria).forEach(criterium -> criteriaMap.put(criterium.getCounterCurrencySymbol(), criterium.getMinVolume()));
            String[] pairs = Arrays.stream(binanceMarketDataService.getExchangeInfo().getSymbols()).
                    filter(symbol -> symbol.getStatus().equals("TRADING")).
                                           map(symbol -> BinanceAdapters.adaptSymbol(symbol.getSymbol())).
                                           filter(pair -> criteriaMap.containsKey(pair.counter.getSymbol())).
                                           map(BinanceAdapters::toSymbol).
                                           toArray(String[]::new);
            ArrayList<String> filteredPairs = new ArrayList<>(pairs.length);
            logger.fine("filtering currency pairs...");
            List<BinanceTicker24h> binanceTicker24hList = binanceMarketDataService.ticker24h();
            Map<String, Double> dailyVolumesMap = new HashMap<>();
            binanceTicker24hList.stream().forEach(
                    binanceTicker -> dailyVolumesMap.put(binanceTicker.getSymbol(), binanceTicker.getQuoteVolume().doubleValue()));
            for (String currentPair : pairs) {
                if (dailyVolumesMap.get(currentPair) == null) {
                    logger.warning("no daily volume data for: " + currentPair);
                    continue;
                }
                if (dailyVolumesMap.get(currentPair) > criteriaMap.get(BinanceAdapters.adaptSymbol(currentPair).counter.getSymbol()))
                    filteredPairs.add(currentPair);
            }
            return filteredPairs.toArray(new String[0]);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }

    @Override
    public String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        try {
        BinanceMarketDataService binanceMarketDataService = ((BinanceMarketDataService)ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(BinanceExchange.class)).getMarketDataService());
        return binanceMarketDataService.tickerAllPrices().stream().
                map(BinancePrice::getCurrencyPair).
                map(BinanceAdapters::toSymbol).
                toArray(String[]::new);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
}
