package pl.dmotyka.exchangeutils.binance.dataobj;

public class Volume24h {

    private final String apiSymbol;
    private final double quoteVolume24h;

    public Volume24h(String apiSymbol, double quoteVolume24h) {
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
