package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public record Int(BigInteger value) implements NumberValue {
    public static final Int ZERO=new Int(BigInteger.ZERO);
    public static final Int ONE=new Int(BigInteger.ONE);
    @Override
    public BigInteger asInt() {
        return value;
    }

    @Override
    public Fraction asFraction() {
        return new Fraction(value, BigInteger.ONE);
    }

    @Override
    public BigDecimal asReal() {
        return new BigDecimal(value);
    }

    @Override
    public boolean asBool() {
        return value.signum() != 0;
    }

    @Override
    public boolean isInt() {
        return true;
    }
    @Override
    public boolean isRational() {
        return true;
    }
    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Int anInt)) return false;
        return value.equals(anInt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
