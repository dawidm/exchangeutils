package com.dawidmotyka.exchangeutils.hitbtc;

import java.math.BigDecimal;

/**
 * Created by dawid on 9/30/17.
 */
public class StepLotInfo {

    public StepLotInfo(String pairName, BigDecimal step, BigDecimal lot) {
        this.pairName = pairName;
        this.step = step;
        this.lot = lot;
    }

    private String pairName;
    private BigDecimal step;
    private BigDecimal lot;

    public String getPairName() {
        return pairName;
    }

    public BigDecimal getStep() {
        return step;
    }

    public BigDecimal getLot() {
        return lot;
    }

    public BigDecimal roundPrice(BigDecimal price) {
        return price.setScale(getStep().scale(),BigDecimal.ROUND_DOWN);
    }

    public BigDecimal inLots(BigDecimal amount) {
        return amount.divide(lot);
    }

}
