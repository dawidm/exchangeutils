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

package pl.dmotyka.exchangeutils.kucoin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.exceptions.ExchangeException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class KuCoinPairDataProvider implements PairDataProvider {
    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ExchangeCommunicationException {
        PairSymbolConverter converter = new KuCoinExchangeSpecs().getPairSymbolConverter();
        Map<String, PairSelectionCriteria> criteriaMap = new HashMap<>();
        for (PairSelectionCriteria pairSelectionCriterion : pairSelectionCriteria) {
            criteriaMap.put(pairSelectionCriterion.getCounterCurrencySymbol(), pairSelectionCriterion);
        }
        try {
            List<Ticker> tickers = new KuCoinExchangeSpecs().getXchangeExchange().getMarketDataService().getTickers(null);
            return tickers.stream().filter(ticker -> {
                PairSelectionCriteria criterion = criteriaMap.get(((CurrencyPair)ticker.getInstrument()).counter.getCurrencyCode());
                if (criterion != null) {
                    return ticker.getQuoteVolume().doubleValue() > criterion.getMinVolume();
                }
                return false;
            }).map(ticker -> converter.toApiSymbol((CurrencyPair)ticker.getInstrument())).toArray(String[]::new);
        } catch (ExchangeException | IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
    public String[] getPairsApiSymbols() throws ExchangeCommunicationException {
        PairSymbolConverter converter = new KuCoinExchangeSpecs().getPairSymbolConverter();
        try {
            List<CurrencyPair> symbols = new KuCoinExchangeSpecs().getXchangeExchange().getExchangeSymbols();
            return symbols.stream().map(converter::toApiSymbol).toArray(String[]::new);
        } catch (ExchangeException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
}
