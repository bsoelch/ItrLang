package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Int;
import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Value;

import java.math.BigInteger;

public class SubSequence implements RandomAccessSequence {
    final RandomAccessSequence base;
    final BigInteger offset;
    final BigInteger length;

    /**
     * @param base base sequence
     * @param offset offset within the base sequence
     * @param length length of the sub-sequence, or -1 if the sub-sequence has infinite length
     * */
    public SubSequence(RandomAccessSequence base, BigInteger offset, BigInteger length) {
        this.base = base;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int size() {
        return (length.signum()<0||length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE))>0)?Integer.MAX_VALUE:length.intValueExact();
    }

    @Override
    public boolean hasIndex(BigInteger i) {
        return i.signum()>=0&&(length.signum()<0||i.compareTo(length)<0);
    }

    @Override
    public Value get(int index) {
        return get(BigInteger.valueOf(index));
    }

    @Override
    public Value get(BigInteger index) {
        if(index.signum()<0||(length.signum()>=0&&index.compareTo(length)>=0))
            return Int.ZERO;
        return base.get(index.add(offset));
    }

    @Override
    public boolean isFinite() {
        return length.signum()<=0||base.isFinite();
    }

    @Override
    public boolean isEqual(Value v) {
        return v instanceof SubSequence&&
                ((SubSequence) v).offset.equals(offset)&&((SubSequence) v).length.equals(length)&&
                ((SubSequence) v).base.isEqual(base);
    }

    @Override
    public SequenceIterator iterator() {
        return new RandomAccessSequence.BigIndexIterator(this);
    }
}
