package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.MathContext;


public class BigMath {
    static final int defaultPrecision =100;

    static final double LOG_10_2=Math.log10(2);
    static final double LOG10=Math.log(10);

    static final BigDecimal ONE_HALF=BigDecimal.ONE.divide(BigDecimal.valueOf(2),MathContext.UNLIMITED);
    static final BigDecimal MINUS_ONE_HALF=ONE_HALF.negate();
    private BigMath() {}

    static BigDecimal simplify(BigDecimal x,MathContext mc){
        return x.round(mc).stripTrailingZeros();
    }

    /**square root of x*/
    public static BigDecimal sqrt(BigDecimal x,MathContext mc){
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        return simplify(x.sqrt(mc),mc);
    }

    /**cube root of x, uses real branch of root*/
    public static BigDecimal cbrt(BigDecimal x,MathContext mc){
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        if(x.signum()==0) {//cbrt(0)=0
            return BigDecimal.ZERO;
        }
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        BigDecimal cbrt=BigDecimal.ONE,delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
        while((delta.abs()).compareTo(d0)>0) {
            delta=cbrt;
            cbrt=(cbrt.multiply(BigDecimal.valueOf(2)).add(x.divide(cbrt.multiply(cbrt,mc2), mc2), mc2)).divide(BigDecimal.valueOf(3),mc2);
            delta=cbrt.subtract(delta);
        }
        return simplify(cbrt,mc);
    }

    /**e<sup>x</sup>*/
    public static BigDecimal exp(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        if(x.compareTo(BigDecimal.ZERO)<0) {
            return BigDecimal.ONE.divide(exp(x.negate(),mc),mc);
        }else {
            MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
            if(x.signum()==0) {//exp(0)=1
                return BigDecimal.ONE;
            }
            BigDecimal exp=new BigDecimal(0,mc),z=BigDecimal.ONE,f=BigDecimal.ONE,m=BigDecimal.ONE,delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
            while((delta.abs()).compareTo(d0)>0) {
                exp=exp.add(delta=z.divide(f, mc2), mc2);
                f=f.multiply(m,mc2);
                m=m.add(BigDecimal.ONE, mc2);
                z=z.multiply(x, mc2);
            }
            return simplify(exp,mc);
        }
    }
    /**natural logarithm of x*/
    public static BigDecimal ln(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        if(x.signum()!=1) {throw new ArithmeticException("ln("+x+")");}
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        long approx2Log10 = Math.round(LOG10*(x.unscaledValue().bitLength() * LOG_10_2 - x.scale()));
        BigDecimal ln=BigDecimal.valueOf(approx2Log10),delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1),mc2), mc2),exp;
        while((delta.abs()).compareTo(d0)>0) {
            exp = exp(ln,mc2);
            delta=(exp.subtract(x, mc2)).divide(exp, mc2);
            ln=ln.subtract(delta,mc2);
        }
        return simplify(ln,mc);
    }

    /**sin(x)*/
    public static BigDecimal sin(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        if(x.signum()==0) {//sin(0)=0
            return BigDecimal.ZERO;
        }
        BigDecimal sin=new BigDecimal(0,mc),z=x,f=BigDecimal.ONE,m=BigDecimal.valueOf(2),delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
        boolean negate=false;
        while((delta.abs()).compareTo(d0)>0) {
            delta=z.divide(f, mc2);
            if(negate) {delta=delta.negate();}
            sin=sin.add(delta, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            negate=!negate;
            z=z.multiply(x,mc2).multiply(x,mc2);
        }
        return simplify(sin,mc);
    }
    /**cos(x)*/
    public static BigDecimal cos(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        if(x.signum()==0) {//cos(0)=1
            return BigDecimal.ONE;
        }
        BigDecimal cos=new BigDecimal(0,mc),z=BigDecimal.ONE,f=BigDecimal.ONE,m=BigDecimal.ONE,delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
        boolean negate=false;
        while((delta.abs()).compareTo(d0)>0) {
            delta=z.divide(f, mc2);
            if(negate) {delta=delta.negate();}
            cos=cos.add(delta, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            negate=!negate;
            z=z.multiply(x,mc2).multiply(x,mc2);
        }
        return simplify(cos,mc);
    }
    public static BigDecimal tan(BigDecimal x,MathContext mc) {
        return sin(x,mc).divide(cos(x,mc),mc);//TODO direct calculation
    }

    public static BigDecimal asin(BigDecimal x,MathContext mc) {
        if(x.abs().compareTo(BigDecimal.ONE)>0)
            throw new ArithmeticException("asin("+x+") is not defined");
        return CMath.asin(new Complex(x,BigDecimal.ZERO),mc).real();
    }
    public static BigDecimal acos(BigDecimal x,MathContext mc) {
        if(x.abs().compareTo(BigDecimal.ONE)>0)
            throw new ArithmeticException("acos("+x+") is not defined");
        return CMath.acos(new Complex(x,BigDecimal.ZERO),mc).real();
    }
    public static BigDecimal atan(BigDecimal x,MathContext mc) {
        return CMath.atan(new Complex(x,BigDecimal.ZERO),mc).real();
    }

    public static BigDecimal sinh(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        if(x.signum()==0) {//sinh(0)=0
            return BigDecimal.ZERO;
        }
        BigDecimal sinh=new BigDecimal(0,mc),z=x,f=BigDecimal.ONE,m=BigDecimal.valueOf(2),delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
        while((delta.abs()).compareTo(d0)>0) {
            delta=z.divide(f, mc2);
            sinh=sinh.add(delta, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            z=z.multiply(x,mc2).multiply(x,mc2);
        }
        return simplify(sinh,mc);
    }
    public static BigDecimal cosh(BigDecimal x,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(defaultPrecision,mc.getRoundingMode());
        }
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        if(x.signum()==0) {//cosh(0)=1
            return BigDecimal.ONE;
        }
        BigDecimal cosh=new BigDecimal(0,mc),z=BigDecimal.ONE,f=BigDecimal.ONE,m=BigDecimal.ONE,delta=BigDecimal.ONE,d0=x.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1), mc2),mc2);
        while((delta.abs()).compareTo(d0)>0) {
            delta=z.divide(f, mc2);
            cosh=cosh.add(delta, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            f=f.multiply(m,mc2);
            m=m.add(BigDecimal.ONE, mc2);
            z=z.multiply(x,mc2).multiply(x,mc2);
        }
        return simplify(cosh,mc);
    }
    public static BigDecimal tanh(BigDecimal x,MathContext mc) {
        return sinh(x,mc).divide(cosh(x,mc),mc);//TODO direct calculation
    }
    //addLater? higher precision for intermediate results
    public static BigDecimal asinh(BigDecimal x,MathContext mc) {
        //log(x+sqrt(x²+1))
        return ln(x.add(sqrt(x.multiply(x,mc).add(BigDecimal.ONE,mc),mc),mc),mc);
    }
    public static BigDecimal acosh(BigDecimal x,MathContext mc) {
        //log(x+sqrt(x²-1))
        return ln(x.add(sqrt(x.multiply(x,mc).subtract(BigDecimal.ONE,mc),mc),mc),mc);
    }
    public static BigDecimal atanh(BigDecimal x,MathContext mc) {
        //0.5*(log(1+x)-log(1-x))
        return ONE_HALF.multiply(ln(BigDecimal.ONE.add(x,mc),mc).subtract(ln(BigDecimal.ONE.subtract(x,mc),mc),mc),mc);
    }

    public static BigDecimal floor(BigDecimal x,MathContext mc) {
        BigDecimal fractional=x.remainder(BigDecimal.ONE,mc);
        if(fractional.signum()<0)
            fractional=BigDecimal.ONE.add(fractional,mc);
        return x.subtract(fractional);
    }
    public static BigDecimal ceil(BigDecimal x,MathContext mc) {
        BigDecimal fractional=x.remainder(BigDecimal.ONE,mc);
        if(fractional.signum()>0)
            fractional=BigDecimal.ONE.subtract(fractional,mc);
        return x.subtract(fractional);
    }
    /**round to the nearest integer*/
    public static BigDecimal round(BigDecimal x,MathContext mc) {
        BigDecimal fractional=x.remainder(BigDecimal.ONE,mc);
        if(fractional.compareTo(ONE_HALF)>=0)
            return x.add(BigDecimal.ONE.subtract(fractional,mc),mc);
        if(fractional.compareTo(MINUS_ONE_HALF)<=0)
            return x.subtract(BigDecimal.ONE.add(fractional,mc),mc);
        return x.subtract(fractional,mc);
    }



    //addLater? gamma function
}
