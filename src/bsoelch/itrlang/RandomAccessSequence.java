package bsoelch.itrlang;

import java.math.BigInteger;
import java.util.Iterator;

public interface RandomAccessSequence extends Sequence {
    Value get(int index);

    Value get(BigInteger index);
    // TODO sub-sequence


    @Override
    default RandomAccessSequence asRASequence() {
        return this;
    }

    class IndexIterator implements Iterator<Value>{
        int index=0;
        final int max;
        RandomAccessSequence seq;
        IndexIterator(RandomAccessSequence seq,int max){
            this.max=max;
            this.seq=seq;
        }
        @Override
        public boolean hasNext() {
            return index<max;
        }
        @Override
        public Value next() {
            return seq.get(index++);
        }
    }
    class BigIndexIterator implements Iterator<Value>{
        BigInteger index=BigInteger.ZERO;
        final BigInteger max;
        RandomAccessSequence seq;
        BigIndexIterator(RandomAccessSequence seq,BigInteger max){
            this.max=max;
            this.seq=seq;
        }
        @Override
        public boolean hasNext() {
            return index.compareTo(max)<0;
        }
        @Override
        public Value next() {
            BigInteger next=index;
            index=index.add(BigInteger.ONE);
            return seq.get(next);
        }
    }
}
