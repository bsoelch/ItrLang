package bsoelch.itrlang.sequence;

import bsoelch.itrlang.*;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

public class ProductSequence implements RandomAccessSequence {
    public static Sequence from(RandomAccessSequence l,RandomAccessSequence r,boolean wrapDiagonals,BinaryOperator<Value> map) {
        return new ProductSequence(l,r, wrapDiagonals, map);
    }

    private final RandomAccessSequence left,right;
    private final boolean wrapDiagonals;
    protected final BinaryOperator<Value> map;

    private ProductSequence(RandomAccessSequence left, RandomAccessSequence right, boolean wrapDiagonals, BinaryOperator<Value> map) {
        this.left = left;
        this.right = right;
        this.wrapDiagonals = wrapDiagonals;
        this.map=map;
    }

    @Override
    public int size() {
        if(wrapDiagonals){
            return (int)Math.min(((long)left.size())+right.size()-1,Integer.MAX_VALUE);
        }
        return (int)Math.min(((long)left.size())*right.size(),Integer.MAX_VALUE);
    }

    private BigInteger[] getPosition(BigInteger i) {
        //floor((sqrt(8*n+1)-1)/2) -> largest n such that n-th triangle number is less than n
        BigInteger d=i.shiftLeft(3).add(BigInteger.ONE).sqrt().subtract(BigInteger.ONE).shiftRight(1);
        if(!(left.hasIndex(d)&&right.hasIndex(d)))//FIXME find correct index for finite size sequences
            throw new UnsupportedOperationException("unimplemented");
        BigInteger r=i.subtract(d.multiply(d).add(d).shiftRight(1));
        return new BigInteger[]{r,d.subtract(r)};
    }
    @Override
    public boolean hasIndex(BigInteger i) {
        if(i.signum()<0||size()==0)
            return false;
        if(BigInteger.valueOf(size()).compareTo(i)<0)
            return true;
        if(left.hasIndex(i)||right.hasIndex(i))
            return true;
        if(wrapDiagonals){
            if(left.hasIndex(i.subtract(BigInteger.valueOf(right.size()))))
                return true;
            if(right.hasIndex(i.subtract(BigInteger.valueOf(left.size()))))
                return true;
            for(BigInteger k=BigInteger.ONE;k.compareTo(i)<0;k=k.add(BigInteger.ONE))
                if(left.hasIndex(k)&&right.hasIndex(i.subtract(k)))
                    return true;
            return false;
        }
        BigInteger[] pos=getPosition(i);
        return left.hasIndex(pos[0])&&right.hasIndex(pos[1]);
    }

    @Override
    public Value get(int index) {
        return get(BigInteger.valueOf(index));
    }

    @Override
    public Value get(BigInteger index) {
        if(wrapDiagonals){
            Tuple res=new Tuple();
            for(BigInteger k=BigInteger.ZERO;k.compareTo(index)<=0;k=k.add(BigInteger.ONE))
                if(left.hasIndex(k)&&right.hasIndex(index.subtract(k)))
                    res.add(map.apply(left.get(k),right.get(index.subtract(k))));
            return res;
        }
        BigInteger[] pos=getPosition(index);
        return map.apply(left.get(pos[0]),right.get(pos[1]));
    }

    @Override
    public SequenceIterator iterator() {
        return new BigIndexIterator(this);
    }

    @Override
    public boolean isFinite() {
        return left.size()==0||right.size()==0||(left.isFinite()&&right.isFinite());
    }

    @Override
    public boolean isEqual(Value v) {
        return v==this;//addLater? equal base&maps -> equal sequence
    }
}
