package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class BufferedSequence implements RandomAccessSequence {
    private final ArrayList<Value> buffer;
    private final Iterator<Value> generator;
    private final Sequence base;
    public BufferedSequence(Sequence base) {
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
    public boolean hasIndex(BigInteger i) {
        if(i.signum()<0)
            return false;
        while(BigInteger.valueOf(buffer.size()).compareTo(i)<0&&generator.hasNext())
            buffer.add(generator.next());
        return BigInteger.valueOf(buffer.size()).compareTo(i)<0;
    }

    @Override
    public Value get(BigInteger index) {
        return get(index.intValueExact());
    }
    @Override
    public int size() {
        return buffer.size()+(generator.hasNext()?1:0);
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
    public SequenceIterator iterator() {
        return new RandomAccessSequence.IndexIterator(this);
    }
}
