package bsoelch.itrlang;

interface Value {
    default Tuple asTuple() {
        return new Tuple(this);
    }
    default Tuple toTuple() {
        return asTuple();
    }

    boolean asBool();


    boolean isInt();

    boolean isRational();

    boolean isReal();

    boolean isNumber();


}
