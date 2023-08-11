package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

record Complex(BigDecimal real, BigDecimal imaginary) implements NumberValue {
    // TODO change complex to also support fraction as real/imaginary part
    public static final Complex ONE = new Complex(BigDecimal.ONE, BigDecimal.ZERO);
    public static final Complex I = new Complex(BigDecimal.ZERO, BigDecimal.ONE);

    @Override
    public BigInteger asInt() {
        return real.toBigInteger();
    }

    @Override
    public Fraction asFraction() {
        return Fraction.fromFloat(real);
    }

    @Override
    public BigDecimal asReal() {
        return real;
    }

    @Override
    public Complex asComplex() {
        return this;
    }
    @Override
    public boolean asBool() {
        return real.signum() != 0||imaginary.signum()!=0;
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
    public String toString() {
        return real.toString() + (imaginary.signum()<0?"-i*":"+i*") + imaginary.abs();
    }

    static Complex negate(Complex a) {
        return new Complex(a.real.negate(), a.imaginary.negate());
    }
    static Complex conjugate(Complex a) {
        return new Complex(a.real, a.imaginary.negate());
    }

    static Complex add(Complex a, Complex b, MathContext mathContext) {
        return new Complex(a.real.add(b.real,mathContext),a.imaginary.add(b.imaginary,mathContext));
    }

    static Complex subtract(Complex a, Complex b, MathContext mathContext) {
        return new Complex(a.real.subtract(b.real,mathContext),a.imaginary.subtract(b.imaginary,mathContext));
    }

    static Complex multiply(Complex a, Complex b, MathContext mathContext) {
        return new Complex(
                a.real.multiply(b.real,mathContext).subtract(
                        a.imaginary.multiply(b.imaginary,mathContext),mathContext),
                a.real.multiply(b.imaginary,mathContext).add(
                        a.imaginary.multiply(b.real,mathContext),mathContext));
    }

    static BigDecimal absSq(Complex z, MathContext mathContext){
        return z.real.multiply(z.real,mathContext).add(z.imaginary.multiply(z.imaginary,mathContext),mathContext);
    }
    static BigDecimal abs(Complex z, MathContext mathContext){
        return BigMath.sqrt(absSq(z,mathContext),mathContext);
    }
    static Complex divide(Complex a, Complex b, MathContext mathContext) {
        BigDecimal l2=absSq(b,mathContext);
        return new Complex(
                a.real.multiply(b.real,mathContext).add(
                        a.imaginary.multiply(b.imaginary,mathContext),mathContext).divide(l2,mathContext),
                a.imaginary.multiply(b.real,mathContext).subtract(
                        a.real.multiply(b.imaginary,mathContext),mathContext).divide(l2,mathContext));
    }

    static Complex remainder(Complex a, Complex b, MathContext mathContext) {
        Complex div=CMath.round(divide(a,b,mathContext),mathContext);
        return subtract(a, multiply(b, div,mathContext),mathContext);
    }

    static int compare(Complex a, Complex b) {
        int c=a.real.compareTo(b.real);
        if(c!=0)
            return c;
        return a.imaginary.compareTo(b.imaginary);
    }
}
