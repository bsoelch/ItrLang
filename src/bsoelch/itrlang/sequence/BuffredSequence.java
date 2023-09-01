package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class BuffredSequence implements RandomAccessSequence {
    private final ArrayList<Value> buffer;
    private final Iterator<Value> generator;
    private final Sequence base;
    public BuffredSequence(Sequence base) {
        buffer=new ArrayList<>();
        generator=base.iterator();
        this.base=base;
    }

    @Override
    public Value get(int index) {
        buffer.ensureCapacity(index);
        while(buffer.size()<=index&&generator.hasNext())
            buffer.add(generator.next());
        return buffer.get(index);
    }

    @Override
    public Value get(BigInteger index) {
        return get(index.intValueExact());
    }
    @Override
    public int size() {
        return Math.max(base.size(), base.size());
    }
    @Override
    public boolean isFinite() {
        return base.isFinite();
    }
    @Override
    public boolean isEqual(Value v) {
        return base.isEqual(v);
    }
    @Override
    public Iterator<Value> iterator() {
        return buffer.iterator();
    }
}
