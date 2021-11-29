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

package pl.dmotyka.exchangeutils.thegraphuniswapv3;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import pl.dmotyka.exchangeutils.thegraphdex.TheGraphHttpRequest;
import pl.dmotyka.exchangeutils.tickerprovider.Ticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uniswap3SwapsToTickersTest {

    @Test
    public void testSwapsToTickers() throws ExchangeCommunicationException, ConnectionProblemException {
        TheGraphHttpRequest req = new TheGraphHttpRequest(new Uniswap3ExchangeSpecs());
        Uniswap3ExchangeSpecs exchangeSpecs = new Uniswap3ExchangeSpecs();
        PairSymbolConverter pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        String[] tokensApiSymbols = exchangeSpecs.getPairDataProvider().getPairsApiSymbols(new PairSelectionCriteria[] {new PairSelectionCriteria("USD", 1000000)});
        int currentTimestamp = (int) Instant.now().getEpochSecond();
        int dayAgoTimestamp = currentTimestamp - 24*60*60;
        String[] tokenAddresses = Arrays.stream(tokensApiSymbols).map(pairSymbolConverter::apiSymbolToBaseCurrencySymbol).toArray(String[]::new);
        Uniswap3TokenSwapsQuery swapsQuery = new Uniswap3TokenSwapsQuery(Uniswap3TokenSwapsQuery.WhichToken.TOKEN0, tokenAddresses, dayAgoTimestamp, currentTimestamp);
        List<JsonNode> resultNodes = req.send(swapsQuery);
        for(JsonNode resultNode : resultNodes) {
            Ticker[] tickers = Uniswap3SwapsToTickers.generateTickers(resultNode, exchangeSpecs);
            assertTrue(tickers.length > 0);
            for (Ticker ticker : tickers) {
                String counterSymbol = pairSymbolConverter.apiSymbolToCounterCurrencySymbol(ticker.getPair());
                assertEquals("USD", counterSymbol);
                assertTrue(ticker.getTimestampSeconds() >= dayAgoTimestamp && ticker.getTimestampSeconds() <= currentTimestamp);
                assertTrue(ticker.getValue() >= 0.0);
            }
        }
    }

}