package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.AbstractList;
import java.util.List;

public record Complex(BigDecimal real, BigDecimal imaginary) implements NumberValue {
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

    @Override
    public List<Value> toList() {
        long rows=BigMath.floor(imaginary,ItrLang.mathContext).abs().longValueExact()+1;
        long size=rows*(BigMath.floor(real,ItrLang.mathContext).abs().longValueExact()+1)-1;
        return new AbstractList<>() {
            final int length=(int)Math.max(Math.min(size,Integer.MAX_VALUE),0);
            @Override
            public Value get(int i) {
                if(i<0&&i>=length)
                    return Int.ZERO;
                long re=real.signum()*((i+1)/rows);
                long im=imaginary.signum()*((i+1)%rows);
                return new Complex(BigDecimal.valueOf(re),BigDecimal.valueOf(im));
            }

            @Override
            public int size() {
                return length;
            }
        };
    }

    public static Value range(Complex to, boolean zeroBased) {
        Tuple elts=new Tuple();
        for(BigDecimal r = BigDecimal.ZERO; r.compareTo(to.real())<=0; r=r.add(BigDecimal.ONE)){
            for(BigDecimal i = BigDecimal.ZERO; i.compareTo(to.imaginary())<=0; i=i.add(BigDecimal.ONE)){
                if(!zeroBased&&r.signum()==0&&i.signum()==0)
                    continue;
                if(zeroBased&&r.compareTo(to.real())==0&&i.compareTo(to.real())==0)
                    continue;
                elts.push(new Complex(r,i));
            }
        }
        return elts;
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

    static BigDecimal absSq(Complex z){
        return z.real.multiply(z.real).add(z.imaginary.multiply(z.imaginary));
    }
    static BigDecimal abs(Complex z, MathContext mathContext){
        return BigMath.sqrt(absSq(z),mathContext);
    }
    static Complex divide(Complex a, Complex b, MathContext mathContext) {
        BigDecimal l2=absSq(b);
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
