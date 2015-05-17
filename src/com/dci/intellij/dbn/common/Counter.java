package com.dci.intellij.dbn.common;

public class Counter {
    private int value;

    public void increment() {
        value++;
        onIncrement();
    }

    public void decrement() {
        value--;
        onDecrement();
    }

    public int getValue() {
        return value;
    }

    public void onIncrement() {}
    public void onDecrement() {}
}
