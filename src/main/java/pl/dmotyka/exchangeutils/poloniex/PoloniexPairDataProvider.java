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

package pl.dmotyka.exchangeutils.poloniex;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataService;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

public class PoloniexPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        try {
        Map<String,Double> criteriaMap = new HashMap<>();
        Arrays.stream(pairSelectionCriteria).forEach(criterium->criteriaMap.put(criterium.getCounterCurrencySymbol(),criterium.getMinVolume()));
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                filter(entry -> criteriaMap.containsKey(apiSymbolToCounterCurrency(entry.getKey()))).
                filter(entry -> entry.getValue().getBaseVolume().doubleValue()>criteriaMap.get(apiSymbolToCounterCurrency(entry.getKey()))).
                map((entry)->entry.getKey()).
                toArray(String[]::new);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
    public String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        try {
        PoloniexMarketDataService poloniexMarketDataService = new PoloniexMarketDataService(ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(PoloniexExchange.class)));
        return poloniexMarketDataService.getAllPoloniexTickers().entrySet().stream().
                map((entry)->entry.getKey()).
                toArray(String[]::new);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
    private static String apiSymbolToCounterCurrency(String apiSymbol) {
        return apiSymbol.split("_")[0];
    }
}
