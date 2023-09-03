package bsoelch.itrlang;

import bsoelch.itrlang.sequence.SequenceIterator;

import java.math.BigDecimal;
import java.math.BigInteger;

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

    class NumberRange implements RandomAccessSequence {
        final int size;
        final BigInteger maxValue;
        final int sign;

        public NumberRange(BigInteger i) {
            maxValue=i.abs();
            sign=i.signum();
            size=maxValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE))>0?Integer.MAX_VALUE:maxValue.intValueExact();
        }
        @Override
        public Value get(BigInteger index) {
            if(index.signum()>=0&&index.compareTo(maxValue)<0)
                return new Int(BigInteger.valueOf(sign).multiply(index.and(BigInteger.ONE)));
            return Int.ZERO;
        }
        @Override
        public Value get(int i) {
            if(i>=0&&BigInteger.valueOf(i).compareTo(maxValue)<0)
                return new Int(BigInteger.valueOf(sign*(i+1L)));
            return Int.ZERO;
        }
        @Override
        public SequenceIterator iterator() {
            return size<Integer.MAX_VALUE?new IndexIterator(this):new BigIndexIterator(this);
        }
        @Override
        public boolean hasIndex(BigInteger i) {
            return i.signum()>=0&&i.compareTo(maxValue)<0;
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isFinite() {
            return true;
        }
        @Override
        public boolean isEqual(Value v) {
            return v instanceof NumberRange&&((NumberRange) v).sign==sign&&((NumberRange) v).maxValue.equals(maxValue);
        }
    }
    @Override
    default Sequence toSequence() {
        return new NumberRange(asInt());
    }

    @Override
    default boolean isEqual(Value v){
        return v instanceof NumberValue&&ItrLang.compareNumbers(this,(NumberValue) v)==0;
    }
}
