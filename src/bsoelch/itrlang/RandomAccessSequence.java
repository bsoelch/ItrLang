package bsoelch.itrlang;

import bsoelch.itrlang.sequence.SequenceIterator;
import bsoelch.itrlang.sequence.SubSequence;

import java.math.BigInteger;

public interface RandomAccessSequence extends Sequence {
    int size();
    boolean hasIndex(BigInteger i);
    Value get(int index);

    Value get(BigInteger index);
    // TODO sub-sequence

    @Override
    default Sequence head(int k) {
        return new SubSequence(this,BigInteger.ZERO,BigInteger.valueOf(k));
    }
    @Override
    default Sequence tail(int k) {
        return new SubSequence(this,BigInteger.valueOf(k),BigInteger.valueOf(-1));
    }

    @Override
    default RandomAccessSequence asRASequence() {
        return this;
    }

    class IndexIterator implements SequenceIterator{
        int index;
        RandomAccessSequence seq;
        public IndexIterator(RandomAccessSequence seq){
            this(seq,0);
        }
        private IndexIterator(RandomAccessSequence seq, int initValue){
            this.index=initValue;
            this.seq=seq;
        }
        @Override
        public boolean hasNext() {
            return seq.hasIndex(BigInteger.valueOf(index));
        }
        @Override
        public Value next() {
            return seq.get(index++);
        }

        @Override
        public void skip(int k) {
            long i=(long)index+k;
            index=(int)Math.min(Integer.MAX_VALUE,i);
        }
        @Override
        public SequenceIterator split() {
            return new IndexIterator(seq,index);
        }
    }
    class BigIndexIterator implements SequenceIterator {
        BigInteger index;
        RandomAccessSequence seq;

        public BigIndexIterator(RandomAccessSequence seq){
            this(seq,BigInteger.ZERO);
        }
        private BigIndexIterator(RandomAccessSequence seq,BigInteger initValue){
            this.index=initValue;
            this.seq=seq;
        }
        @Override
        public boolean hasNext() {
            return seq.hasIndex(index);
        }
        @Override
        public Value next() {
            BigInteger next=index;
            index=index.add(BigInteger.ONE);
            return seq.get(next);
        }
        @Override
        public void skip(int k) {
            index=index.add(BigInteger.valueOf(k));
        }
        @Override
        public BigIndexIterator split() {
            return new BigIndexIterator(seq,index);
        }
    }
}
