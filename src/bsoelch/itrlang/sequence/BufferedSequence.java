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
        for(int i=0;i<16;i++){//buffer the first 16 elements
            if(generator.hasNext())
                buffer.add(generator.next());
        }
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
        return buffer.size();
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
