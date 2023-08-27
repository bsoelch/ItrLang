package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.List;

public interface NumberValue extends Value {
    BigDecimal asReal();

    /**round to nearest integer*/
    BigInteger asInt();

    Fraction asFraction();

    default Complex asComplex() {
        return new Complex(asReal(),BigDecimal.ZERO);
    }

    @Override
    default boolean isNumber(){
        return true;
    }

    @Override
    default List<Value> toList() {
        BigInteger i=asInt();
        long maxValue=i.abs().longValueExact();
        int sign=i.signum();
        return new AbstractList<>() {
            final int size=(int)Math.max(Math.min(maxValue,Integer.MAX_VALUE),0);
            @Override
            public Value get(int i) {
                if(i>=0&&i<maxValue)
                    return new Int(BigInteger.valueOf(sign*(i+1)));
                return Int.ZERO;
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    default boolean isEqual(Value v){
        return v instanceof NumberValue&&ItrLang.compareNumbers(this,(NumberValue) v)==0;
    }
}
