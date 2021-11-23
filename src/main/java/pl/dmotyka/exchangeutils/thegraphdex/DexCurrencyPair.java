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

    private String token1Symbol;
    private String token2Symbol;
    private String poolAddress;

    public DexCurrencyPair(String token1Symbol, String token2Symbol, String poolAddress) {
        this.token1Symbol = token1Symbol;
        this.token2Symbol = token2Symbol;
        this.poolAddress = poolAddress;
    }

    public String getToken1Symbol() {
        return token1Symbol;
    }

    public String getToken2Symbol() {
        return token2Symbol;
    }

    public String getPoolAddress() {
        return poolAddress;
    }

    @Override
    public String toString() {
        return poolAddress;
    }
}
