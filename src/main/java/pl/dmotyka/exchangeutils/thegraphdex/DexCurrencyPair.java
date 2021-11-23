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

public class DexCurrencyPair {

    private String token0Symbol;
    private String token0Address;
    private String token1Symbol;
    private String token1Address;
    private String poolAddress;

    public DexCurrencyPair(String token0Symbol, String token0Address, String token1Symbol, String token1Address, String poolAddress) {
        this.token0Symbol = token0Symbol;
        this.token0Address = token0Address;
        this.token1Symbol = token1Symbol;
        this.token1Address = token1Address;
        this.poolAddress = poolAddress;
    }

    public String getToken0Symbol() {
        return token0Symbol;
    }

    public String getToken0Address() {
        return token0Address;
    }

    public String getToken1Symbol() {
        return token1Symbol;
    }

    public String getToken1Address() {
        return token1Address;
    }

    public String getPoolAddress() {
        return poolAddress;
    }

    @Override
    public String toString() {
        return poolAddress;
    }
}
