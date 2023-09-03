package bsoelch.itrlang;

import bsoelch.itrlang.sequence.BufferedSequence;
import bsoelch.itrlang.sequence.SequenceIterator;
import bsoelch.itrlang.sequence.TailSequence;

import java.util.Iterator;

public interface Sequence extends Value,Iterable<Value>{
    boolean isFinite();
    @Override
    default Tuple asTuple() {
        if(!isFinite())
            throw new UnsupportedOperationException("cannot convert infinite sequence to Tuple");
        Tuple res=new Tuple();
        if(this instanceof RandomAccessSequence)
            res.ensureCapacity(((RandomAccessSequence)this).size());
        for(Value v:this)
            res.add(v);
        return res;
    }
    default RandomAccessSequence asRASequence(){
        return new BufferedSequence(this);
    }
    default Sequence head(int k) {
        Tuple elts=new Tuple();
        Iterator<Value> values=iterator();
        for(int i=0;values.hasNext()&&i<k;i++){
            elts.add(values.next());
        }
        return elts;
    }

    default Sequence tail(int k) {
      return new TailSequence(this,k);
    }

    @Override
    default Sequence toSequence() {
        return this;
    }


    @Override
    default boolean asBool() {
        return iterator().hasNext();// TODO? infinite version of "any element is true"
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

    @Override
    SequenceIterator iterator();
}
