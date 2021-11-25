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

package pl.dmotyka.exchangeutils.thegraphdex;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public class DexCurrencyPair {

    private static final int POOLS_LIMIT = 3;

    private final String baseSymbol;
    private final String baseTokenAddress;
    private final String counterSymbol;
    private final LinkedList<DexPool> poolsAddresses = new LinkedList<>();

    public DexCurrencyPair(String baseSymbol, String baseTokenAddress, String counterSymbol, DexPool dexPool) {
        this.baseSymbol = baseSymbol;
        this.baseTokenAddress = baseTokenAddress;
        this.counterSymbol = counterSymbol;
        poolsAddresses.add(dexPool);
    }

    public void addPool(DexPool dexPool) {
        poolsAddresses.add(dexPool);
        Collections.sort(poolsAddresses, Comparator.comparingDouble(DexPool::getVolume));
        if (poolsAddresses.size() > POOLS_LIMIT) {
            poolsAddresses.pop();
        }
    }

    public String getBaseSymbol() {
        return baseSymbol;
    }

    public String getBaseTokenAddress() {
        return baseTokenAddress;
    }

    public String getCounterSymbol() {
        return counterSymbol;
    }

    public Set<String> getPoolsAddresses() {
        return poolsAddresses.stream().map(DexPool::getAddress).collect(Collectors.toUnmodifiableSet());
    }

}
