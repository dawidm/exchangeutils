package pl.dmotyka.exchangeutils.binance.dataobj;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Volume24h {

    private final String apiSymbol;
    private final double quoteVolume24h;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Volume24h(@JsonProperty("symbol") String apiSymbol, @JsonProperty("quoteVolume") double quoteVolume24h) {
        this.apiSymbol = apiSymbol;
        this.quoteVolume24h = quoteVolume24h;
    }

    public String getApiSymbol() {
        return apiSymbol;
    }

    public double getQuoteVolume24h() {
        return quoteVolume24h;
    }
}
