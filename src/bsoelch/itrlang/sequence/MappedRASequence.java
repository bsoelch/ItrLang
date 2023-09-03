package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.function.Function;

public class MappedRASequence implements RandomAccessSequence {
    private final RandomAccessSequence base;
    private final Function<Value,Value> map;

    MappedRASequence(RandomAccessSequence base,Function<Value,Value> map) {
        this.base = base;
        this.map=map;
    }

    @Override
    public SequenceIterator iterator() {
        return new MappedSequence.MappedIterator(base.iterator(),map);
    }
    @Override
    public Value get(int index) {
        return map.apply(base.get(index));
    }

    @Override
    public Value get(BigInteger index) {
        return map.apply(base.get(index));
    }

    @Override
    public boolean hasIndex(BigInteger i) {
        return base.hasIndex(i);
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isFinite() {
        return base.isFinite();
    }

    @Override
    public boolean isEqual(Value v) {
        return v==this;//addLater? equal base&maps -> equal sequence
    }
}
