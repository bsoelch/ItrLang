package bsoelch.itrlang;

import bsoelch.itrlang.sequence.BuffredSequence;

public interface Sequence extends Value,Iterable<Value>{
    int size();
    boolean isFinite();
    @Override
    default Tuple asTuple() {
        if(!isFinite())
            throw new UnsupportedOperationException("cannot convert infinite sequence to Tuple");
        Tuple res=new Tuple();
        res.ensureCapacity(size());
        for(Value v:this)
            res.add(v);
        return res;
    }
    default RandomAccessSequence asRASequence(){
        return new BuffredSequence(this);
    }

    @Override
    default Sequence toSequence() {
        return this;
    }


    @Override
    default boolean asBool() {
        return size() > 0;// TODO? infinite version of "any element is true"
    }
    @Override
    default boolean isInt() {
        return false;
    }
    @Override
    default boolean isRational() {
        return false;
    }
    @Override
    default boolean isReal() {
        return false;
    }
    @Override
    default boolean isNumber() {
        return false;
    }
}
