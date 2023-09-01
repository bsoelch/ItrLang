package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Int;
import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.Iterator;

public class Integers implements RandomAccessSequence {
    public static final Integers N=new Integers();
    private Integers(){}
    @Override
    public Value get(int index) {
        return new Int(BigInteger.valueOf(index));
    }
    @Override
    public Value get(BigInteger index) {
        return new Int(index);
    }
    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }
    @Override
    public boolean isFinite() {
        return false;
    }

    @Override
    public Iterator<Value> iterator() {
        return new Iterator<>() {
            BigInteger current=BigInteger.ZERO;
            @Override
            public boolean hasNext() {
                return true;
            }
            @Override
            public Value next() {
                BigInteger prev=current;
                current=current.add(BigInteger.ONE);
                return new Int(prev);
            }
        };
    }

    @Override
    public boolean isEqual(Value v) {
        return v instanceof Integers;
    }
    @Override
    public String toString() {
        return "i -> i";// TODO? better to string method
    }
}