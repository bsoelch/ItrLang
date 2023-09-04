package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Tuple;
import bsoelch.itrlang.Value;

import java.math.BigInteger;

public class RepeatSequence implements Sequence{
    public static Sequence from(Sequence seq, BigInteger count){
        if(count.signum()==0)
            return new Tuple();
        // TODO RA version
        if(seq instanceof RepeatSequence){
            BigInteger newCount;
            if(count.signum()<0||((RepeatSequence) seq).count.signum()<0) {
                newCount=BigInteger.valueOf(-1);
            }else{
                newCount=((RepeatSequence) seq).count.multiply(count);
            }
            return new RepeatSequence(((RepeatSequence) seq).base,newCount);
        }
        return new RepeatSequence(seq,count);
    }
    final Sequence base;
    final BigInteger count;
    /**@param count number of repetitions or -1 if the sequence should be repeated indefinately*/
    private RepeatSequence(Sequence base, BigInteger count) {
        this.base = base;
        this.count = count;
    }

    @Override
    public boolean isFinite() {
        return count.signum()>=0&&base.isFinite();
    }

    class RepeatIterator implements SequenceIterator{
        BigInteger index;
        SequenceIterator baseItr;
        RepeatIterator(SequenceIterator baseItr,BigInteger index){
            this.baseItr=baseItr;
            this.index=index;
        }
        @Override
        public boolean hasNext() {
            return baseItr.hasNext();
        }
        @Override
        public Value next() {
            Value next=baseItr.next();
            if(!baseItr.hasNext()&&(count.signum()<0||index.compareTo(count)<0)){
                baseItr=base.iterator();//reset base iterator
                index=index.add(BigInteger.ONE);
            }
            return next;
        }
        @Override
        public SequenceIterator split() {
            return new RepeatIterator(baseItr.split(),index);
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new RepeatIterator(base.iterator(),BigInteger.ONE);
    }

    @Override
    public boolean isEqual(Value v) {
        return v instanceof RepeatSequence&&((RepeatSequence) v).count.equals(count)&&((RepeatSequence) v).base.isEqual(base);
    }
}
