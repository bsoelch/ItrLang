package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.function.Function;

public class MappedRASequence implements RandomAccessSequence {
    private final RandomAccessSequence base;
    private final Function<Value,Value> map;

    MappedRASequence(RandomAccessSequence base,Function<Value,Value> map) {
        this.base = base;
        this.map=map;
    }

    @Override
    public Iterator<Value> iterator() {
        return new Iterator<>() {
            final Iterator<Value> baseItr=base.iterator();
            @Override
            public boolean hasNext() {
                return baseItr.hasNext();
            }
            @Override
            public Value next() {
                return map.apply(baseItr.next());
            }
        };
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
