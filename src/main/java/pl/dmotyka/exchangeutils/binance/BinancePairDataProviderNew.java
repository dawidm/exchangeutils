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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;

class BinancePairDataProviderNew implements PairDataProvider {

    private static final Logger logger = Logger.getLogger(BinancePairDataProviderNew.class.getName());

    private static final String EXCHANGE_INFO_ENDPOINT = "/api/v3/exchangeInfo";
    private static final String TICKER24_INFO_ENDPOINT = "/api/v3/ticker/24hr";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            JsonNode tickersNode = objectMapper.readValue(new URL(BinanceApiSpecs.getFullEndpointUrl(TICKER24_INFO_ENDPOINT)), JsonNode.class);
            Map<String, Double> volumesMap = new HashMap<>();
            for (JsonNode tickerNode : tickersNode) {
                volumesMap.put(tickerNode.get("symbol").textValue(), tickerNode.get("quoteVolume").asDouble(0));
            }
            return Arrays.stream(getPairsApiSymbols()).filter(symbol -> {
                Optional<PairSelectionCriteria> criterion = Arrays.stream(pairSelectionCriteria).
                        filter(crit -> symbol.endsWith(crit.getCounterCurrencySymbol())).findAny();
                return criterion.filter(selectionCriteria -> volumesMap.get(symbol) >= selectionCriteria.getMinVolume()).isPresent();
            }).toArray(String[]::new);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }

    @Override
    public String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            JsonNode infoNode = objectMapper.readValue(new URL(BinanceApiSpecs.getFullEndpointUrl(EXCHANGE_INFO_ENDPOINT)), JsonNode.class);
            JsonNode symbolsNode = infoNode.get("symbols");
            ArrayList<String> apiSymbols = new ArrayList<>();
            for (JsonNode symbolNode : symbolsNode) {
                if (symbolNode.get("status").asText().equals("TRADING")) {
                    apiSymbols.add(symbolNode.get("symbol").textValue());
                }
            }
            return apiSymbols.toArray(String[]::new);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }
}
