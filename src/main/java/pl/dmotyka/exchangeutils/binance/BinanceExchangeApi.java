package pl.dmotyka.exchangeutils.binance;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import pl.dmotyka.exchangeutils.binance.dataobj.Volume24h;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.tools.ApiTools;

public class BinanceExchangeApi {

    private final ApiTools apiTools = new ApiTools();

    public List<Volume24h> getVolumes24h() throws ConnectionProblemException, ExchangeCommunicationException {
        JsonNode tickersNode = apiTools.getJsonNode(BinanceApiSpecs.getFullEndpointUrl(BinanceApiSpecs.TICKER24_INFO_ENDPOINT));
        List<Volume24h> volumesList = new ArrayList<>(tickersNode.size());
        for (JsonNode tickerNode : tickersNode) {
            volumesList.add(new Volume24h(tickerNode.get("symbol").textValue(), tickerNode.get("quoteVolume").asDouble(0)));
        }
        return volumesList;
    }

}
