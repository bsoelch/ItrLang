package bsoelch.itrlang;

import java.math.BigDecimal;
import java.math.MathContext;


public class CMath {
    private static final Complex ONE_HALF = new Complex(BigMath.ONE_HALF,BigDecimal.ZERO);

    private CMath() {}

    static Complex simplify(Complex z,MathContext mc){
        BigDecimal re=z.real().abs();
        BigDecimal im=z.imaginary().abs();
        if(re.compareTo(im)>0&&re.ulp().compareTo(im)>=0)//imaginary part is insignificant
            return new Complex(BigMath.simplify(z.real(),mc),BigDecimal.ZERO);
        if(im.compareTo(re)>0&&im.ulp().compareTo(re)>=0)//real part is insignificant
            return new Complex(BigDecimal.ZERO,BigMath.simplify(z.imaginary(),mc));
        return new Complex(BigMath.simplify(z.real(),mc),BigMath.simplify(z.imaginary(),mc));
    }

    //addLater? higher precision for intermediate results

    /**square root of x*/
    public static Complex sqrt(Complex z,MathContext mc){
        // sqrt(z)=sqrt((|z|+re(z))/2)+i*sqrt((|z|-re(z))/2) im(z)>=0
        // sqrt(z)=sqrt((|z|+re(z))/2)-i*sqrt((|z|-re(z))/2) im(z)<0
        BigDecimal abs=Complex.abs(z,mc);
        return simplify(new Complex(
                BigMath.sqrt(abs.add(z.real(),mc).multiply(BigMath.ONE_HALF,mc),mc),
                BigMath.sqrt(abs.subtract(z.real(),mc).multiply(BigMath.ONE_HALF,mc),mc)
                        .multiply(BigDecimal.valueOf(z.imaginary().signum()<0?-1:1))
        ),mc);
    }

    /**cube root of x, uses principal branch of root*/
    public static Complex cbrt(Complex z,MathContext mc){
        if(mc.getPrecision()==0) {
            mc=new MathContext(BigMath.defaultPrecision,mc.getRoundingMode());
        }
        throw new UnsupportedOperationException();//TODO
    }

    /**e<sup>x</sup>*/
    public static Complex exp(Complex z,MathContext mc) {
        //addLater? higher precision for intermediate results
        //addLater? direct calculation
        BigDecimal scale=BigMath.exp(z.real(),mc);
        return simplify(
                new Complex(scale.multiply(BigMath.cos(z.imaginary(),mc),mc),scale.multiply(BigMath.sin(z.imaginary(),mc),mc)),
                mc);
    }
    /**natural logarithm of x*/
    public static Complex ln(Complex z,MathContext mc) {
        if(mc.getPrecision()==0) {
            mc=new MathContext(BigMath.defaultPrecision,mc.getRoundingMode());
        }
        BigDecimal r=Complex.abs(z,mc);
        MathContext mc2=new MathContext(mc.getPrecision()+10,mc.getRoundingMode());
        long approx2Log10 = Math.round(BigMath.LOG10*(r.unscaledValue().bitLength() * BigMath.LOG_10_2 - r.scale()));
        BigDecimal approxAngle;
        if(z.imaginary().signum()==0){
            approxAngle=z.real().signum()<0?BigDecimal.valueOf(Math.PI):BigDecimal.ZERO;
        }else{
            double angle=Math.atan2(z.imaginary().doubleValue(),z.real().doubleValue());
            if(Double.isFinite(angle)){
                approxAngle=BigDecimal.valueOf(angle);
            }else{//TODO more precise angle approximation
                approxAngle=BigDecimal.valueOf(z.imaginary().signum()*Math.PI/2);
            }
        }
        Complex ln=new Complex(BigDecimal.valueOf(approx2Log10),approxAngle),
                delta= Complex.ONE,exp;
        BigDecimal d0=r.abs(mc2).multiply(BigDecimal.TEN.pow(-(mc.getPrecision()+1),mc2), mc2);
        while((Complex.abs(delta,mc)).compareTo(d0)>0) {
            exp = exp(ln,mc2);
            delta=Complex.divide(Complex.subtract(exp,z,mc2),exp, mc2);
            ln=Complex.subtract(ln,delta,mc2);
        }
        return simplify(ln,mc);
    }

    /**sin(x)*/
    public static Complex sin(Complex z,MathContext mc) {
        // 1/2i * (exp(iz)-exp(-iz))
        Complex eiz=exp(new Complex(z.imaginary().negate(),z.real()),mc);
        Complex emiz=exp(new Complex(z.imaginary(),z.real().negate()),mc);
        Complex t=Complex.multiply(ONE_HALF,Complex.subtract(eiz,emiz,mc),mc);
        return simplify(new Complex(t.imaginary(),t.real().negate()),mc);
    }
    /**cos(x)*/
    public static Complex cos(Complex z,MathContext mc) {
        // 1/2 * (exp(iz)+exp(-iz))
        Complex eiz=exp(new Complex(z.imaginary().negate(),z.real()),mc);
        Complex emiz=exp(new Complex(z.imaginary(),z.real().negate()),mc);
        return simplify(Complex.multiply(ONE_HALF,Complex.add(eiz,emiz,mc),mc),mc);
    }
    public static Complex tan(Complex z,MathContext mc) {
        //tan(z)=-i (e^2iz-1)/(e^2iz+1)
        Complex iz=new Complex(z.imaginary().negate(),z.real());
        Complex two_iz=Complex.add(iz,iz,mc);
        Complex e2iz=exp(two_iz,mc);
        Complex i_res=Complex.divide(Complex.subtract(e2iz,Complex.ONE,mc),Complex.add(e2iz,Complex.ONE,mc),mc);
        return simplify(new Complex(i_res.imaginary(),i_res.real().negate()),mc);
    }
    public static Complex asin(Complex z,MathContext mc) {
        //asin(z)=-i*ln(i*z+sqrt(1-z²))
        Complex iz=new Complex(z.imaginary().negate(),z.real());
        Complex rt=sqrt(Complex.subtract(Complex.ONE,Complex.multiply(z,z,mc),mc),mc);
        Complex i_res=ln(Complex.add(iz,rt,mc),mc);
        return simplify(new Complex(i_res.imaginary(),i_res.real().negate()),mc);
    }
    public static Complex acos(Complex z,MathContext mc) {
        //acos(z)=-i*ln(z+sqrt(z²-1))
        Complex rt=sqrt(Complex.subtract(Complex.multiply(z,z,mc),Complex.ONE,mc),mc);
        Complex i_res=ln(Complex.add(z,rt,mc),mc);
        return simplify(new Complex(i_res.imaginary(),i_res.real().negate()),mc);
    }
    public static Complex atan(Complex z,MathContext mc) {
        //atan(z)=-i/2*ln((1+iz)/(1-iz))
        Complex iz=new Complex(z.imaginary().negate(),z.real());
        Complex p=Complex.add(Complex.ONE,iz,mc);
        Complex m=Complex.subtract(Complex.ONE,iz,mc);
        Complex i_res=Complex.multiply(ONE_HALF,ln(Complex.divide(p,m,mc),mc),mc);
        return simplify(new Complex(i_res.imaginary(),i_res.real().negate()),mc);
    }

    public static Complex sinh(Complex z,MathContext mc) {
        // 1/2 * (exp(z)-exp(-z))
        return simplify(
                Complex.multiply(ONE_HALF,Complex.subtract(exp(z,mc),exp(Complex.negate(z),mc),mc),mc),
                mc);
    }
    public static Complex cosh(Complex z,MathContext mc) {
        // 1/2 * (exp(z)+exp(-z))
        return simplify(
            Complex.multiply(ONE_HALF,Complex.add(exp(z,mc),exp(Complex.negate(z),mc),mc),mc),
                mc);
    }
    public static Complex tanh(Complex z,MathContext mc) {
        //tanh(z)=(e^2z-1)/(e^2z+1)
        Complex two_z=Complex.add(z,z,mc);
        Complex e2z=exp(two_z,mc);
        return simplify(
                Complex.divide(Complex.subtract(e2z,Complex.ONE,mc),Complex.add(e2z,Complex.ONE,mc),mc),
                mc);
    }
    public static Complex asinh(Complex z,MathContext mc) {
        //log(x+sqrt(x²+1))
        return simplify(
                ln(Complex.add(z,sqrt(Complex.add(Complex.multiply(z,z,mc), Complex.ONE,mc),mc),mc),mc),
                mc);
    }
    public static Complex acosh(Complex z,MathContext mc) {
        //log(x+sqrt(x+1)sqrt(x-1))
        return simplify(
                ln(Complex.add(z,Complex.multiply(sqrt(Complex.add(z,Complex.ONE,mc),mc),sqrt(Complex.subtract(z,Complex.ONE,mc),mc),mc),mc),mc),
                mc);
    }
    public static Complex atanh(Complex z,MathContext mc) {
        //0.5*(log(1+x)-log(1-x))
        return simplify(
                Complex.multiply(ONE_HALF,Complex.subtract(ln(Complex.add(Complex.ONE,z,mc),mc),
                    ln(Complex.subtract(Complex.ONE,z,mc),mc),mc),mc),
                mc);
    }

    public static Complex floor(Complex z,MathContext mc) {
        return new Complex(BigMath.floor(z.real(),mc),BigMath.floor(z.imaginary(),mc));
    }
    public static Complex ceil(Complex z,MathContext mc) {
        return new Complex(BigMath.ceil(z.real(),mc),BigMath.ceil(z.imaginary(),mc));
    }
    /**round to the nearest gaussian integer*/
    public static Complex round(Complex z,MathContext mc) {
        return new Complex(BigMath.round(z.real(),mc),BigMath.round(z.imaginary(),mc));
    }

    //addLater? gamma function
}
