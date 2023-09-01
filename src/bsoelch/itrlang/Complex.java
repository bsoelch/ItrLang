package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Iterator;

public record Complex(BigDecimal real, BigDecimal imaginary) implements NumberValue {
    // TODO change complex to also support fraction as real/imaginary part
    public static final Complex ONE = new Complex(BigDecimal.ONE, BigDecimal.ZERO);
    public static final Complex I = new Complex(BigDecimal.ZERO, BigDecimal.ONE);

    @Override
    public BigInteger asInt() {
        return BigMath.round(real,ItrLang.mathContext).toBigInteger();
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

    public static class ComplexRange implements RandomAccessSequence{
        BigInteger rows;
        BigInteger size;
        int realSign,imagSign;
        int length;
        public ComplexRange(BigDecimal real,BigDecimal imaginary){
            rows=BigMath.floor(imaginary,ItrLang.mathContext).toBigIntegerExact().abs().add(BigInteger.ONE);
            size=rows.multiply(BigMath.floor(real,ItrLang.mathContext).toBigIntegerExact().abs().add(BigInteger.ONE)).subtract(BigInteger.ONE);
            realSign=real.signum();
            imagSign=imaginary.signum();
            length=size.compareTo(BigInteger.valueOf(Integer.MAX_VALUE))>0?Integer.MAX_VALUE:size.intValueExact();
        }
        @Override
        public Value get(BigInteger i) {
            if(i.signum()<0&&i.compareTo(size)>=0)
                return Int.ZERO;
            BigInteger[] dm=i.add(BigInteger.ONE).divideAndRemainder(rows);
            BigInteger re=BigInteger.valueOf(realSign).multiply(dm[0]);
            BigInteger im=BigInteger.valueOf(imagSign).multiply(dm[1]);
            return new Complex(new BigDecimal(re),new BigDecimal(im));
        }
        @Override
        public Value get(int index) {
            return get(BigInteger.valueOf(index));
        }

        @Override
        public int size() {
            return length;
        }
        @Override
        public Iterator<Value> iterator() {
            return length<Integer.MAX_VALUE?new IndexIterator(this,length):new BigIndexIterator(this,size);
        }
        @Override
        public boolean isFinite() {
            return true;
        }
        @Override
        public boolean isEqual(Value v) {
            return v instanceof ComplexRange&&
                    ((ComplexRange) v).realSign==realSign&&((ComplexRange) v).imagSign==imagSign&&
                    ((ComplexRange) v).size.equals(size)&&((ComplexRange) v).rows.equals(rows);
        }
    }

    @Override
    public Sequence toSequence() {
        return new ComplexRange(real,imaginary);
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
