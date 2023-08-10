package bsoelch.itrlang;

class Tuple extends Stack<Value> implements Value {
    Tuple(Value... elts) {
        super(elts);
    }

    @Override
    public Tuple asTuple() {
        return this;
    }

    @Override
    public boolean asBool() {
        return stream().anyMatch(Value::asBool);
    }

    @Override
    public boolean isInt() {
        return false;
    }
    @Override
    public boolean isRational() {
        return false;
    }
    @Override
    public boolean isReal() {
        return false;
    }
    @Override
    public boolean isNumber() {
        return false;
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
