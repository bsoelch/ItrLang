package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Int;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.util.function.BinaryOperator;

public class BinaryOperationSequence implements Sequence{
    public static Sequence from(Sequence l,Sequence r,boolean truncate, BinaryOperator<Value> map) {
        // TODO RA version
        return new BinaryOperationSequence(l,r, truncate, map);
    }

    private final Sequence left,right;
    private final boolean truncate;
    protected final BinaryOperator<Value> map;

    private BinaryOperationSequence(Sequence left, Sequence right, boolean truncate, BinaryOperator<Value> map) {
        this.left = left;
        this.right = right;
        this.truncate = truncate;
        this.map=map;
    }

    record MappedIterator(SequenceIterator leftItr,SequenceIterator rightItr,boolean truncate, BinaryOperator<Value> map) implements SequenceIterator {
        @Override
        public boolean hasNext() {
                return truncate?(leftItr.hasNext()&&rightItr.hasNext()):(leftItr.hasNext()||rightItr.hasNext());
            }
        @Override
        public Value next() {
            Value left=leftItr.hasNext()?leftItr.next():Int.ZERO;
            Value right=rightItr.hasNext()?rightItr.next():Int.ZERO;
            return map.apply(left,right);
        }
        @Override
        public void skip(int k) {
            leftItr.skip(k);
            rightItr.skip(k);
        }
        @Override
        public SequenceIterator split() {
            return new MappedIterator(leftItr.split(),rightItr.split(),truncate,map);
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new MappedIterator(left.iterator(),right.iterator(),truncate,map);
    }

    @Override
    public boolean isFinite() {
        return truncate?(left.isFinite()||right.isFinite()):(left.isFinite()&&right.isFinite());
    }

    @Override
    public boolean isEqual(Value v) {
        return v==this;//addLater? equal base&maps -> equal sequence
    }
}
