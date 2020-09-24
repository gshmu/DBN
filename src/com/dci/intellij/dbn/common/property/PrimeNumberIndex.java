package com.dci.intellij.dbn.common.property;

/**
 * @deprecated intended for smaller footprint property holder
 * exceeds int max value after 9th prime
 */
public class PrimeNumberIndex {
    private final int[] index;

    public PrimeNumberIndex(int size) {
        index = new int[size];
        int idx = 0;
        int number = 2;
        while (idx < size) {
            if (isPrime(number)) {
                index[idx] = number;
                idx++;
            }
            number++;
        }
    }

    public int getPrime(int idx) {
        return this.index[idx];
    }


    private static boolean isPrime(int number) {
        for (int i = 2; i < number; i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        PrimeNumberIndex primeNumberIndex = new PrimeNumberIndex(100);

        int p0 = primeNumberIndex.getPrime(0);
        int p1 = primeNumberIndex.getPrime(1);
        int p2 = primeNumberIndex.getPrime(2);
        int p3 = primeNumberIndex.getPrime(3);
        int p4 = primeNumberIndex.getPrime(4);
        int p5 = primeNumberIndex.getPrime(5);

        int computed = p0 * p1 * p2 * p4 * p5;
        System.out.println(computed % p0);
        System.out.println(computed % p1);
        System.out.println(computed % p2);
        System.out.println(computed % p3);
        System.out.println(computed % p4);
        System.out.println(computed % p5);

        for (int index : primeNumberIndex.index) {
            System.out.println(index);
        }


    }
}
