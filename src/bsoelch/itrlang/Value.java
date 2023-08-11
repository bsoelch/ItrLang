package bsoelch.itrlang;

import java.util.List;

public interface Value {
    default Tuple asTuple() {
        return new Tuple(this);
    }
    default Tuple toTuple() {
        return asTuple();
    }
    default List<Value> toList(){
        return toTuple();
    }

    boolean asBool();


    boolean isInt();

    boolean isRational();

    boolean isReal();

    boolean isNumber();


}
