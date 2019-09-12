package com.dci.intellij.dbn.common.util;

import java.math.BigDecimal;

public class MathResult {
    private BigDecimal sum;
    private BigDecimal count;
    private BigDecimal average;

    public MathResult(BigDecimal sum, BigDecimal count, BigDecimal average) {
        this.sum = sum;
        this.count = count;
        this.average = average;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getCount() {
        return count;
    }

    public BigDecimal getAverage() {
        return average;
    }
}
