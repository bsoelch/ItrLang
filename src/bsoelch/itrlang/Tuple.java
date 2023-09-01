package bsoelch.itrlang;

import java.math.BigInteger;

public class Tuple extends Stack<Value> implements Value,RandomAccessSequence {
    public Tuple(Value... elts) {
        super(elts);
    }

    @Override
    public Tuple asTuple() {
        return this;
    }

    @Override
    public Value get(BigInteger index) {
        return get(index.intValueExact());
    }

    @Override
    public boolean asBool() {
        return stream().anyMatch(Value::asBool);
    }

    @Override
    public boolean isFinite() {
        return true;
    }

    @Override
    public boolean isEqual(Value v) {
        if(!(v instanceof Tuple t))
            return false;
        if(t.size()!=size())
            return false;
        for(int i=0;i<size();i++){
            if(!get(i).isEqual(((Tuple) v).get(i)))
                return false;
        }
        return true;
    }

    /**first k elements of this tuple*/
    Tuple head(int k) {
        k = Math.max(0, Math.min(size(), k));
        return new Tuple(subList(0, k).toArray(new Value[0]));
    }
    /**last k elements of this tuple*/
    Tuple tail(int k) {
        k = Math.max(0, Math.min(size(), k));
        return new Tuple(subList(size() - k, size()).toArray(new Value[0]));
    }
    void truncate(int newSize){
        if(newSize<size())
            removeRange(newSize,size());
    }
}
