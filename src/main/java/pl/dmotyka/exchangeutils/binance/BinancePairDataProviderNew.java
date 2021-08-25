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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.binance.dataobj.Volume24h;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.tools.ApiTools;

class BinancePairDataProviderNew implements PairDataProvider {

    private static final Logger logger = Logger.getLogger(BinancePairDataProviderNew.class.getName());

    private final ApiTools apiTools = new ApiTools();
    BinanceExchangeApi binanceExchangeApi = new BinanceExchangeApi();

    @Override
    public String[] getPairsApiSymbols(PairSelectionCriteria[] pairSelectionCriteria) throws ConnectionProblemException, ExchangeCommunicationException {
        List<Volume24h> volumesList = binanceExchangeApi.getVolumes24h();
        Map<String, Volume24h> volumesMap = volumesList.stream().collect(Collectors.toMap(Volume24h::getApiSymbol, Function.identity()));
        return Arrays.stream(getPairsApiSymbols()).filter(symbol -> {
            Optional<PairSelectionCriteria> criterion = Arrays.stream(pairSelectionCriteria).
                    filter(crit -> symbol.endsWith(crit.getCounterCurrencySymbol())).findAny();
            return criterion.filter(selectionCriteria -> volumesMap.get(symbol).getQuoteVolume24h() >= selectionCriteria.getMinVolume()).isPresent();
        }).toArray(String[]::new);
    }

    @Override
    public String[] getPairsApiSymbols() throws ConnectionProblemException, ExchangeCommunicationException {
        JsonNode infoNode = apiTools.getJsonNode(BinanceApiSpecs.getFullEndpointUrl(BinanceApiSpecs.EXCHANGE_INFO_ENDPOINT));
        JsonNode symbolsNode = infoNode.get("symbols");
        ArrayList<String> apiSymbols = new ArrayList<>();
        for (JsonNode symbolNode : symbolsNode) {
            if (symbolNode.get("status").asText().equals("TRADING")) {
                apiSymbols.add(symbolNode.get("symbol").textValue());
            }
        }
        return apiSymbols.toArray(String[]::new);
    }
}
