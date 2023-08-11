package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;

public record Real(BigDecimal value) implements NumberValue {
    @Override
    public BigInteger asInt() {
        return value.toBigInteger();
    }

    @Override
    public Fraction asFraction() {
        return Fraction.fromFloat(value);
    }

    @Override
    public BigDecimal asReal() {
        return value;
    }

    @Override
    public boolean asBool() {
        return value.signum() != 0;
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
        return true;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
