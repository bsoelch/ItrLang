package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.util.Iterator;
import java.util.function.Function;

public class MappedSequence implements Sequence{
    public static Sequence from(Sequence a, Function<Value,Value> map) {
        if(a instanceof RandomAccessSequence){
            return new MappedRASequence((RandomAccessSequence)a,map);
        }
        return new MappedSequence(a,map);
    }

    private final Sequence base;
    protected final Function<Value,Value> map;

    private MappedSequence(Sequence base,Function<Value,Value> map) {
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
    public boolean isFinite() {
        return base.isFinite();
    }

    @Override
    public boolean isEqual(Value v) {
        return v==this;//addLater? equal base&maps -> equal sequence
    }
}
