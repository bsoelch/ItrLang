package bsoelch.itrlang;

public interface Value {
    default Tuple asTuple() {
        return new Tuple(this);
    }
    default Tuple toTuple() {
        return asTuple();
    }
    default Sequence asSequence() {
        return asTuple();
    }
    default Sequence toSequence(){
        return toTuple();
    }

    boolean asBool();


    boolean isInt();

    boolean isRational();

    boolean isReal();

    boolean isNumber();

    boolean isEqual(Value v);
}
