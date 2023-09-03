package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

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

    record MappedIterator(SequenceIterator baseItr, Function<Value, Value> map) implements SequenceIterator {
        @Override
        public boolean hasNext() {
                return baseItr.hasNext();
            }
        @Override
        public Value next() {
                return map.apply(baseItr.next());
            }
        @Override
        public void skip(int k) {
            baseItr.skip(k);
        }
        @Override
        public SequenceIterator split() {
            return new MappedIterator(baseItr.split(), map);
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new MappedIterator(base.iterator(),map);
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
