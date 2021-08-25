package pl.dmotyka.exchangeutils.binance;

import java.util.List;

import org.junit.jupiter.api.Test;
import pl.dmotyka.exchangeutils.binance.dataobj.Volume24h;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinanceExchangeApiTest {

    @Test
    void getVolumes24h() throws ConnectionProblemException, ExchangeCommunicationException {
        List<Volume24h> volumes24h = new BinanceExchangeApi().getVolumes24h();
        assertNotNull(volumes24h);
        volumes24h.forEach(vol -> {
            assertNotNull(vol);
            assertTrue(vol.getApiSymbol() != null && !vol.getApiSymbol().isEmpty());
            assertTrue(vol.getQuoteVolume24h() >= 0);
        });
    }
}