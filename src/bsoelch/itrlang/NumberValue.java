package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface NumberValue extends Value {
    BigDecimal asReal();

    BigInteger asInt();

    Fraction asFraction();

    default Complex asComplex() {
        return new Complex(asReal(),BigDecimal.ZERO);
    }

    @Override
    default boolean isNumber(){
        return true;
    }
}
