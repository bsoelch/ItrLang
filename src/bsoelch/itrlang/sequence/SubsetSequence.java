package bsoelch.itrlang.sequence;

import bsoelch.itrlang.RandomAccessSequence;
import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Tuple;
import bsoelch.itrlang.Value;

import java.math.BigInteger;
import java.util.function.Function;

public class SubsetSequence implements Sequence{
    public static Sequence from(Sequence a, Function<Value,Value> map) {
        return new SubsetSequence(a,map);
    }

    private final RandomAccessSequence base;
    protected final Function<Value,Value> key;

    private SubsetSequence(Sequence base, Function<Value,Value> key) {
        this.base = base.asRASequence();
        this.key=key;
    }

    private class SubsetItr implements SequenceIterator{
        BigInteger setId,mask;
        SubsetItr(BigInteger setId){
            this.setId=setId;
        }
        @Override
        public boolean hasNext() {
            return setId.bitLength()<=base.size();
        }
        @Override
        public Value next() {
            mask=BigInteger.ONE;
            int i=0;
            Tuple set=new Tuple();
            while(mask.bitLength()<=setId.bitLength()){
                if(setId.and(mask).signum()!=0)
                    set.push(base.get(i));
                mask=mask.shiftLeft(1);
                i++;
            }
            setId=setId.add(BigInteger.ONE);
            return key.apply(set);
        }
        @Override
        public void skip(int k) {
            setId=setId.add(BigInteger.valueOf(k));
        }
        @Override
        public SequenceIterator split() {
            return new SubsetItr(setId);
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new SubsetItr(BigInteger.ZERO);
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
