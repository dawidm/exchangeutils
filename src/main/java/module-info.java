module exchangeutils {
    requires java.sql;
    requires dmutils;
    requires xchange.core;
    requires com.fasterxml.jackson.databind;
    requires xchange.bitfinex;
    requires xapi.java.wrapper;
    requires xchange.poloniex;
    requires bittrex4j;
    requires xchange.bittrex;
    requires xchange.binance;
    requires signalr4j;
    requires Java.WebSocket;
    requires json;

    exports pl.dmotyka.exchangeutils.exchangespecs;
    exports pl.dmotyka.exchangeutils.pairsymbolconverter;
    exports pl.dmotyka.exchangeutils.tools;
    exports pl.dmotyka.exchangeutils.pairdataprovider;
    exports pl.dmotyka.exchangeutils.tickerprovider;
    exports pl.dmotyka.exchangeutils.chartdataprovider;
    exports pl.dmotyka.exchangeutils.binance;
    exports pl.dmotyka.exchangeutils.bitfinex;
    exports pl.dmotyka.exchangeutils.poloniex;
    exports pl.dmotyka.exchangeutils.chartinfo;
}