package pl.dmotyka.exchangeutils.binance;

import java.util.Arrays;
import java.util.List;

import pl.dmotyka.exchangeutils.binance.dataobj.Volume24h;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;
import pl.dmotyka.exchangeutils.tools.ApiTools;

public class BinanceExchangeApi {

    private final ApiTools apiTools = new ApiTools();

    public List<Volume24h> getVolumes24h() throws ConnectionProblemException, ExchangeCommunicationException {
        Volume24h[] volumes = apiTools.getDataObj(BinanceApiSpecs.getFullEndpointUrl(BinanceApiSpecs.TICKER24_INFO_ENDPOINT), Volume24h[].class);
        return Arrays.asList(volumes);
    }

}
