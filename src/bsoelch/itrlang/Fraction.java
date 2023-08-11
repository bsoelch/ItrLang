package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;

record Fraction(BigInteger numerator, BigInteger denominator) implements NumberValue {

    Fraction {
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        if (gcd.signum() != 0) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
    }

    static Fraction fromFloat(BigDecimal value) {
        boolean negate = false;
        if (value.signum() < 0) {
            negate = true;
            value = value.negate();
        }
        BigInteger intPart = value.toBigInteger(), num = BigInteger.ZERO, den = BigInteger.ONE,
                num2 = BigInteger.ONE, den2 = BigInteger.ZERO;
        BigDecimal eps = BigDecimal.valueOf(1, value.scale() + ItrLang.mathContext.getPrecision());//TODO correct version
        value = value.remainder(BigDecimal.ONE, ItrLang.mathContext);//value between zero and one
        BigDecimal delta = value, x;
        boolean flipped = false;
        do {//approximate float using continued fraction expansion
            delta = BigDecimal.ONE.divide(delta, ItrLang.mathContext);
            BigInteger C = delta.toBigInteger();
            BigInteger num1 = num, den1 = den;
            num = num.multiply(C).add(num2);
            den = den.multiply(C).add(den2);
            num2 = num1;
            den2 = den1;
            delta = delta.remainder(BigDecimal.ONE, ItrLang.mathContext);
            x = value.subtract(new BigDecimal(num).divide(new BigDecimal(den), ItrLang.mathContext), ItrLang.mathContext).abs();
            flipped = !flipped;
        } while (x.compareTo(eps) > 0);
        num = num.add(intPart.multiply(den));
        return new Fraction(negate ? num.negate() : num, den);
    }

    @Override
    public BigInteger asInt() {
        return numerator.divide(denominator);
    }

    @Override
    public Fraction asFraction() {
        return this;
    }

    @Override
    public BigDecimal asReal() {
        return new BigDecimal(numerator, ItrLang.mathContext).divide(new BigDecimal(denominator), ItrLang.mathContext);
    }
    @Override
    public boolean asBool() {
        return numerator.signum() != 0;
    }

    @Override
    public boolean isInt() {
        return false;
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
        return numerator.toString() + "/" + denominator.toString();
    }

    static Fraction negate(Fraction a) {
        return new Fraction(a.numerator.negate(), a.denominator);
    }

    static Fraction add(Fraction a, Fraction b) {
        return new Fraction(a.numerator.multiply(b.denominator).add(b.numerator.multiply(a.denominator)),
                a.denominator.multiply(b.denominator));
    }

    static Fraction subtract(Fraction a, Fraction b) {
        return new Fraction(a.numerator.multiply(b.denominator).subtract(b.numerator.multiply(a.denominator)),
                a.denominator.multiply(b.denominator));
    }

    static Fraction multiply(Fraction a, Fraction b) {
        return new Fraction(a.numerator.multiply(b.numerator), a.denominator.multiply(b.denominator));
    }

    static Fraction divide(Fraction a, Fraction b) {
        return new Fraction(a.numerator.multiply(b.denominator), a.denominator.multiply(b.numerator));
    }

    static Fraction remainder(Fraction a, Fraction b) {
        return subtract(a, multiply(b, new Fraction(divide(a, b).asInt(), BigInteger.ONE)));
    }

    static int compare(Fraction a, Fraction b) {
        return a.numerator.multiply(b.denominator).compareTo(a.denominator.multiply(b.numerator));
    }
}
