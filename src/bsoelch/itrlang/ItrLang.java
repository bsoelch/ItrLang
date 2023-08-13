package bsoelch.itrlang;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class ItrLang {
    static MathContext mathContext = new MathContext(BigMath.defaultPrecision);
    /**inverse of logarithm of 2, needed for binary logarithm*/
    static BigDecimal LOG2INV;
    static BigDecimal PI;

    static{
        recomputeConstants();
    }
    static void recomputeConstants(){
        MathContext extended=new MathContext(mathContext.getPrecision()+2, mathContext.getRoundingMode());
        LOG2INV=BigDecimal.ONE.divide(BigMath.ln(BigDecimal.valueOf(2),extended),extended);
        PI=CMath.ln(new Complex(BigDecimal.valueOf(-1),BigDecimal.ZERO),extended).imaginary();
    }

    static Value unaryNumberOp(Value a,  Function<NumberValue,Value> f){
        if(a.isNumber()){
            return f.apply((NumberValue) a);
        }
        if(a instanceof Tuple){
            return new Tuple(((Tuple) a).stream().map(x->unaryNumberOp(x,f)).toArray(Value[]::new));
        }
        throw new IllegalArgumentException("unsupported operands of binary number operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue negateNumber(NumberValue a){
        if(a instanceof Int)
            return new Int(a.asInt().negate());
        if(a instanceof Fraction)
            return Fraction.negate(a.asFraction());
        if(a instanceof Real)
            return new Real(a.asReal().negate());
        if(a instanceof Complex)
            return Complex.negate(a.asComplex());
        throw new IllegalArgumentException("unsupported value type: "+a.getClass().getName());
    }
    static Value negate(Value a){
        return unaryNumberOp(a, ItrLang::negateNumber);
    }
    static NumberValue invertNumber(NumberValue a){
        if(a instanceof Int)
            return new Fraction(BigInteger.ONE,a.asInt().negate());
        if(a instanceof Fraction){
            Fraction f=a.asFraction();
            return new Fraction(f.denominator(),f.numerator());
        }
        if(a instanceof Real)
            return new Real(BigDecimal.ONE.divide(a.asReal(),mathContext));
        if(a instanceof Complex)
            return Complex.divide(Complex.ONE,a.asComplex(),mathContext);
        throw new IllegalArgumentException("unsupported value type: "+a.getClass().getName());
    }
    static Value invert(Value a){
        if(a.isNumber())
            return invertNumber((NumberValue) a);
        if(a instanceof Matrix)
            return Matrix.invert((Matrix) a);
        if(a instanceof Tuple)
            return new Tuple(((Tuple) a).stream().map(ItrLang::invert).toArray(Value[]::new));
        throw new IllegalArgumentException("unsupported value type for invert: "+a.getClass().getName());
    }

    static Value binaryNumberOp(Value a, Value b, BiFunction<NumberValue,NumberValue,Value> f){
        if(a.isNumber()&&b.isNumber()){
            return f.apply((NumberValue)a,(NumberValue)b);
        }
        if(a instanceof Matrix&&b.isNumber()){
            Tuple res=new Tuple();
            res.ensureCapacity(((Matrix) a).nrows());
            for(int i=0;i<((Matrix) a).nrows();i++){
                Tuple row=new Tuple();
                row.ensureCapacity(((Matrix) a).ncolumns());
                for(int j=0;j<((Matrix) a).ncolumns();j++)
                    row.push(binaryNumberOp(((Matrix) a).at(i,j),b,f));
                res.push(row);
            }
            return new Matrix(res);
        }
        if(a.isNumber()&&b instanceof Matrix){
            Tuple res=new Tuple();
            res.ensureCapacity(((Matrix)b).nrows());
            for(int i=0;i<((Matrix) b).nrows();i++){
                Tuple row=new Tuple();
                row.ensureCapacity(((Matrix) b).ncolumns());
                for(int j=0;j<((Matrix) b).ncolumns();j++)
                    row.push(binaryNumberOp(a,((Matrix)b).at(i,j),f));
                res.push(row);
            }
            return new Matrix(res);
        }
        if(a instanceof Matrix&&b instanceof Matrix){
            int rows=Math.max(((Matrix) a).nrows(),((Matrix) b).nrows());
            int columns=Math.max(((Matrix) a).ncolumns(),((Matrix) b).ncolumns());
            Tuple res=new Tuple();
            res.ensureCapacity(rows);
            for(int i=0;i<rows;i++){
                Tuple row=new Tuple();
                row.ensureCapacity(columns);
                for(int j=0;j<columns;j++)
                   row.push(binaryNumberOp(((Matrix) a).at(i,j),((Matrix) b).at(i,j),f));
                res.push(row);
            }
            return new Matrix(res);
        }
        if(a.isNumber()&&b instanceof Tuple){
            return new Tuple(((Tuple) b).stream().map(x->binaryNumberOp(a,x,f)).toArray(Value[]::new));
        }
        if(a instanceof Tuple&&b.isNumber()){
            return new Tuple(((Tuple) a).stream().map(x->binaryNumberOp(x, b,f)).toArray(Value[]::new));
        }
        if(a instanceof Tuple&&b instanceof Tuple){
            Tuple res=new Tuple();
            res.ensureCapacity(Math.max(((Tuple) a).size(),((Tuple) b).size()));
            int i=0;
            for(;i<((Tuple) a).size()&&i<((Tuple) b).size();i++){
                res.add(binaryNumberOp(((Tuple) a).get(i),((Tuple) b).get(i),f));
            }
            for(;i<((Tuple) a).size();i++){
                res.add(binaryNumberOp(((Tuple) a).get(i),Int.ZERO,f));
            }
            for(;i<((Tuple) b).size();i++){
                res.add(binaryNumberOp(Int.ZERO,((Tuple) b).get(i),f));
            }
            return res;
        }
        throw new IllegalArgumentException("unsupported operands of binary number operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue addNumbers(NumberValue a,NumberValue b){
        if(a instanceof Int&&b instanceof Int)
            return new Int(a.asInt().add(b.asInt()));
        if(a.isRational()&&b.isRational())
            return Fraction.add(a.asFraction(),b.asFraction());
        if(a.isReal()&&b.isReal())
            return new Real(a.asReal().add(b.asReal(),mathContext));
        if(a.isNumber()&&b.isNumber())
            return Complex.add(a.asComplex(),b.asComplex(),mathContext);
        throw new IllegalArgumentException("unsupported operands of addition: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static Value add(Value a,Value b){
        return binaryNumberOp(a,b,ItrLang::addNumbers);
    }
    static NumberValue subtractNumbers(NumberValue a,NumberValue b){
        if(a instanceof Int&&b instanceof Int)
            return new Int(a.asInt().subtract(b.asInt()));
        if(a.isRational()&&b.isRational())
            return Fraction.subtract(a.asFraction(),b.asFraction());
        if(a.isReal()&&b.isReal())
            return new Real(a.asReal().subtract(b.asReal(),mathContext));
        if(a.isNumber()&&b.isNumber())
            return Complex.subtract(a.asComplex(),b.asComplex(),mathContext);
        throw new IllegalArgumentException("unsupported operands of subtraction: "+a.getClass().getName()+"   "+a.getClass().getName());
    }

    static NumberValue multiplyNumbers(NumberValue a,NumberValue b){
        if(a instanceof Int&&b instanceof Int)
            return new Int(a.asInt().multiply(b.asInt()));
        if(a.isRational()&&b.isRational())
            return Fraction.multiply(a.asFraction(),b.asFraction());
        if(a.isReal()&&b.isReal())
            return new Real(a.asReal().multiply(b.asReal(),mathContext));
        if(a.isNumber()&&b.isNumber())
            return Complex.multiply(a.asComplex(),b.asComplex(),mathContext);
        throw new IllegalArgumentException("unsupported operands of multiplication: "+a.getClass().getName()+"   "+a.getClass().getName());
    }

    static Value binaryMatrixOp(Value a,Value b,BiFunction<Matrix,Matrix,Value> fM,BiFunction<NumberValue,NumberValue,Value> fN){
        if(a.isNumber()&&b.isNumber()){
            return fN.apply((NumberValue) a, (NumberValue) b);
        }
        if(a instanceof Matrix&&b instanceof Matrix){
            return fM.apply((Matrix) a,(Matrix) b);
        }
        if(a instanceof Matrix&&b.isNumber()){
            Tuple res=new Tuple();
            res.ensureCapacity(((Matrix) a).nrows());
            for(int i=0;i<((Matrix) a).nrows();i++){
                Tuple row=new Tuple();
                row.ensureCapacity(((Matrix) a).ncolumns());
                for(int j=0;j<((Matrix) a).ncolumns();j++)row.add(binaryMatrixOp(((Matrix) a).at(i,j),b,fM,fN));
                res.add(row);
            }
            return new Matrix(res);
        }
        if(a.isNumber()&&b instanceof Matrix){
            Tuple res=new Tuple();
            res.ensureCapacity(((Matrix) b).nrows());
            for(int i=0;i<((Matrix) b).nrows();i++){
                Tuple row=new Tuple();
                row.ensureCapacity(((Matrix)b).ncolumns());
                for(int j=0;j<((Matrix) b).ncolumns();j++)row.add(binaryMatrixOp(a,((Matrix) b).at(i,j),fM,fN));
                res.add(row);
            }
            return new Matrix(res);
        }
        if(a.isNumber()&&b instanceof Tuple){
            return new Tuple(((Tuple) b).stream().map(x->binaryMatrixOp(a,x,fM,fN)).toArray(Value[]::new));
        }
        if(a instanceof Tuple&&b.isNumber()){
            return new Tuple(((Tuple) a).stream().map(x->binaryMatrixOp(x,b,fM,fN)).toArray(Value[]::new));
        }
        if(a instanceof Tuple&&b instanceof Tuple){
            Tuple res=new Tuple();
            res.ensureCapacity(Math.max(((Tuple) a).size(),((Tuple) b).size()));
            int i=0;
            for(;i<((Tuple) a).size()&&i<((Tuple) b).size();i++){
                res.add(binaryMatrixOp(((Tuple) a).get(i),((Tuple) b).get(i),fM,fN));
            }
            for(;i<((Tuple) a).size();i++){
                res.add(binaryMatrixOp(((Tuple) a).get(i),Int.ZERO,fM,fN));
            }
            for(;i<((Tuple) b).size();i++){
                res.add(binaryMatrixOp(Int.ZERO,((Tuple) b).get(i),fM,fN));
            }
            return res;
        }
        throw new IllegalArgumentException("unsupported operands of binary matrix operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static Value multiply(Value a,Value b){
        return binaryMatrixOp(a,b,Matrix::multiply,ItrLang::multiplyNumbers);
    }
    static Value divide_left(Value a, Value b){
        return binaryMatrixOp(a,b,Matrix::divide_left,(x, y)->realDivide(y,x));
    }
    static Value divide_right(Value a, Value b){
        return binaryMatrixOp(a,b,Matrix::divide_right,ItrLang::realDivide);
    }

    static NumberValue realDivide(NumberValue a,NumberValue b){
        if(!b.asBool())
            return Int.ZERO;//addLater? return zero of same type as a
        if(a instanceof Int&&b instanceof Int)
            return new Fraction(a.asInt(),b.asInt());
        if(a.isRational()&&b.isRational())
            return Fraction.divide(a.asFraction(),b.asFraction());
        if(a.isReal()&&b.isReal())
            return new Real(a.asReal().divide(b.asReal(),mathContext));
        if(a.isNumber()&&b.isNumber())
            return Complex.divide(a.asComplex(),b.asComplex(),mathContext);
        throw new IllegalArgumentException("unsupported operands of division: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue intDivide(NumberValue a,NumberValue b){
        if(!b.asBool())
            return Int.ZERO;
        if(a instanceof Int&&b instanceof Int)
            return new Int(Fraction.floorDivide(a.asInt(),b.asInt()));
        if(a.isRational()&&b.isRational())
            return new Int(Fraction.floorDivide(a.asFraction(),b.asFraction()));
        if(a.isReal()&&b.isReal())
            return new Int(BigMath.floor(a.asReal().divide(b.asReal(),mathContext),mathContext).toBigInteger());
        if(a.isNumber()&&b.isNumber())
            return CMath.round(Complex.divide(a.asComplex(),b.asComplex(),mathContext),mathContext);
        throw new IllegalArgumentException("unsupported operands of division: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue remainder(NumberValue a,NumberValue b){
        if(!b.asBool())
            return a;
        if(a.isNumber()&&b.isNumber()){
            NumberValue d=intDivide(a,b);
            return subtractNumbers(a,multiplyNumbers(b,d));
        }
        throw new IllegalArgumentException("unsupported operands of remainder: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static Value pow(Value a,Value b){
        if((a.isNumber()||a instanceof Matrix)&&b.isInt()){
            BigInteger e=((NumberValue)b).asInt();
            if(e.signum()<0){
                return invert(pow(a,negate(b)));
            }
            if(a.isInt()){
                return new Int(((NumberValue)a).asInt().pow(e.intValueExact()));
            }
            Value res=Int.ONE,p=a;
            while(e.signum()!=0){
                if(e.testBit(0))
                    res=multiply(res,p);
                p=multiply(p,p);
                e=e.shiftRight(1);
            }
            return res;
        }
        if(a instanceof Tuple||b instanceof Tuple){
            Tuple arrayA=a.asTuple(),arrayB=b.asTuple(),res=new Tuple();
            res.ensureCapacity(Math.max(arrayA.size(),arrayB.size()));
            int i=0;
            for(;i<arrayA.size()&&i<arrayB.size();i++)
                res.add(pow(arrayA.get(i),arrayB.get(i)));
            for(;i<arrayA.size();i++)
                res.add(pow(arrayA.get(i),(b.isNumber()||b instanceof Matrix)?b:Int.ONE));
            for(;i<arrayB.size();i++)
                res.add(pow((a.isNumber()||a instanceof Matrix)?a:Int.ZERO,arrayB.get(i)));
            return res;
        }
        throw new Error("incompatible types for exponentiation: "+a.getClass().getName()+" and "+b.getClass().getName());
    }

    static int compareNumbers(NumberValue a,NumberValue b){
        if(a instanceof Int&&b instanceof Int)
            return a.asInt().compareTo(b.asInt());
        if(a.isRational()&&b.isRational())
            return Fraction.compare(a.asFraction(),b.asFraction());
        if(a.isReal()&&b.isReal())
            return a.asReal().compareTo(b.asReal());
        if(a.isNumber()&&b.isNumber())
            return Complex.compare(a.asComplex(),b.asComplex());
        throw new IllegalArgumentException("unsupported operands for comparison: "+a.getClass().getName()+"   "+a.getClass().getName());
    }

    static NumberValue andNumbers(NumberValue a,NumberValue b){//addLater? apply logical operations to real and imaginary part separately
        if(a.isNumber()&&b.isNumber())
            return new Int(a.asInt().and(b.asInt()));
        throw new IllegalArgumentException("unsupported operands for boolean operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue orNumbers(NumberValue a,NumberValue b){
        if(a.isNumber()&&b.isNumber())
            return new Int(a.asInt().or(b.asInt()));
        throw new IllegalArgumentException("unsupported operands for boolean operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }
    static NumberValue xorNumbers(NumberValue a,NumberValue b){
        if(a.isNumber()&&b.isNumber())
            return new Int(a.asInt().xor(b.asInt()));
        throw new IllegalArgumentException("unsupported operands for boolean operation: "+a.getClass().getName()+"   "+a.getClass().getName());
    }

    static Value applyFunction(Value arg,String name){
        if(arg.isReal()){
            NumberValue x=(NumberValue)arg;
            switch (name) {
                case "floor" -> { return new Real(BigMath.floor(x.asReal(),mathContext)); }
                case "round" -> { return new Real(BigMath.round(x.asReal(),mathContext)); }
                case "ceil" -> { return new Real(BigMath.ceil(x.asReal(),mathContext)); }
                case "sqrt" -> {
                    BigDecimal v=x.asReal();
                    if(v.signum()<0)
                        return CMath.sqrt(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(v.sqrt(mathContext));
                }
                case "cbrt" -> { return new Real(BigMath.cbrt(x.asReal(),mathContext)); }
                case "exp" -> { return new Real(BigMath.exp(x.asReal(),mathContext)); }
                case "log" -> {
                    BigDecimal v=x.asReal();
                    if(v.signum()<0)
                        return CMath.ln(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(BigMath.ln(v,mathContext));
                }
                case "sin" -> { return new Real(BigMath.sin(x.asReal(),mathContext)); }
                case "cos" -> { return new Real(BigMath.cos(x.asReal(),mathContext)); }
                case "tan" -> { return new Real(BigMath.tan(x.asReal(),mathContext)); }
                case "asin" -> {
                    BigDecimal v=x.asReal();
                    if(v.abs().compareTo(BigDecimal.ONE)>0)
                        return CMath.asin(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(BigMath.asin(v,mathContext));
                }
                case "acos" -> {
                    BigDecimal v=x.asReal();
                    if(v.abs().compareTo(BigDecimal.ONE)>0)
                        return CMath.acos(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(BigMath.acos(v,mathContext));
                }
                case "atan" -> { return new Real(BigMath.atan(x.asReal(),mathContext)); }
                case "sinh" -> { return new Real(BigMath.sinh(x.asReal(),mathContext)); }
                case "cosh" -> { return new Real(BigMath.cosh(x.asReal(),mathContext)); }
                case "tanh" -> { return new Real(BigMath.tanh(x.asReal(),mathContext)); }
                case "asinh" -> { return new Real(BigMath.asinh(x.asReal(),mathContext)); }
                case "acosh" -> {
                    BigDecimal v=x.asReal();
                    if(v.abs().compareTo(BigDecimal.ONE)>0)
                        return CMath.asin(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(BigMath.acosh(v,mathContext));
                }
                case "atanh" -> {
                    BigDecimal v=x.asReal();
                    if(v.abs().compareTo(BigDecimal.ONE)>0)
                        return CMath.atanh(new Complex(v,BigDecimal.ZERO),mathContext);
                    return new Real(BigMath.atanh(v,mathContext));
                }
            }
            throw new UnsupportedOperationException("unsupported function: \""+name+"\"");
        }else if(arg.isNumber()){
            NumberValue x=(NumberValue)arg;
            switch (name) {
                case "floor" -> { return CMath.floor(x.asComplex(),mathContext); }
                case "round" -> { return CMath.round(x.asComplex(),mathContext); }
                case "ceil" -> { return CMath.ceil(x.asComplex(),mathContext); }
                case "sqrt" -> { return CMath.sqrt(x.asComplex(),mathContext); }
                case "cbrt" -> { return CMath.cbrt(x.asComplex(),mathContext); }
                case "exp" -> { return CMath.exp(x.asComplex(),mathContext); }
                case "log" -> { return CMath.ln(x.asComplex(),mathContext); }
                case "sin" -> { return CMath.sin(x.asComplex(),mathContext); }
                case "cos" -> { return CMath.cos(x.asComplex(),mathContext); }
                case "tan" -> { return CMath.tan(x.asComplex(),mathContext); }
                case "asin" -> { return CMath.asin(x.asComplex(),mathContext); }
                case "acos" -> { return CMath.acos(x.asComplex(),mathContext); }
                case "atan" -> { return CMath.atan(x.asComplex(),mathContext); }
                case "sinh" -> { return CMath.sinh(x.asComplex(),mathContext); }
                case "cosh" -> { return CMath.cosh(x.asComplex(),mathContext); }
                case "tanh" -> { return CMath.tanh(x.asComplex(),mathContext); }
                case "asinh" -> { return CMath.asinh(x.asComplex(),mathContext); }
                case "acosh" -> { return CMath.acosh(x.asComplex(),mathContext); }
                case "atanh" -> { return CMath.atanh(x.asComplex(),mathContext); }
            }
            throw new UnsupportedOperationException("unsupported function: \""+name+"\"");
        }
        if(arg instanceof Tuple){
            Tuple clone= (Tuple) ((Tuple)arg).clone();
            clone.replaceAll(value -> applyFunction(value, name));
        }
        throw new UnsupportedOperationException("unsupported value type: "+arg.getClass().getName());
    }

    static Tuple repeat(Tuple a, @SuppressWarnings("SameParameterValue") int n){
        final int aLen=a.size();
        boolean reverse=false;
        if(n==0)
            return new Tuple();
        if(n<0){
            reverse=true;
            n=-n;
        }
        Value[] data=new Value[aLen*n];
        if(reverse){
            for(int i=0;i<aLen;i++){
                data[aLen-1-i]=a.get(i);
            }
        }else{
            System.arraycopy(a.toArray(new Value[0]),0,data,0,aLen);
        }
        for(int i=1;i<n;i++){
            System.arraycopy(data,0,data,i*aLen,aLen);
        }
        return new Tuple(data);
    }
    static Tuple concatenate(Tuple a, Tuple b){
        Value[] data=new Value[a.size()+b.size()];
        for(int i=0;i<a.size();i++){
            data[i]=a.get(i);
        }
        for(int i=0;i<b.size();i++){
            data[i+a.size()]=b.get(i);
        }
        return new Tuple(data);
    }


    /**simulate a UTF8 input stream independent of the system encoding*/
    static abstract class UTF8Input extends InputStream{
        public abstract int read() throws IOException;
        int readCodepoint() throws IOException {
            int s=read();
            if(s<0)
                return -1;
            long cp=ItrLang.readCodepoint(s,this);
            return (int)cp;// addLater? allow codepoints outside int range
        }
        @SuppressWarnings("SameParameterValue")
        static UTF8Input fromBytes(InputStream stream){
            return new UTF8Input(){
                @Override
                public int read() throws IOException {
                    return stream.read();
                }
            };
        }
        @SuppressWarnings("SameParameterValue")
        static UTF8Input fromChars(InputStreamReader stream){
            Iterator<byte[]> lines=new BufferedReader(stream).lines()
                    .map(l->(l+'\n').getBytes(StandardCharsets.UTF_8)).iterator();
            return new UTF8Input(){
                byte[] bytes;
                int offset;
                boolean nextLine() throws IOException {
                    try{
                        bytes=lines.next();
                    }catch (UncheckedIOException uio){
                        throw uio.getCause();
                    }
                    offset=0;
                    return false;
                }
                @Override
                public int read() throws IOException {
                    if(offset==0){
                        if(nextLine())
                            return -1;
                    }
                    return bytes[offset++]&0xff;
                }
            };
        }
    }

    static long decodeCompressedUTF8(byte[] bytes, int l){
        if(l<=1)
            return bytes[0]&0xff;
        long cp=0;
        // get difference of high bits from expected value
        for(int i=1;i<l;i++){
            cp<<=2;cp|=((bytes[i]&0xff)>>6)^2;
        }
        // get bits stored in first byte
        cp<<=8-l;
        cp|=bytes[0]&(0xff>>l);
        // get bits stored in later bytes
        for(int i=1;i<l;i++){
            cp<<=6;cp|=(bytes[i]&0x3f);
        }
        return cp;
    }
    static long readCodepoint(int start,InputStream in) throws IOException {
        int l=(start==0xFF)?8:((start&0xFE)==0xFE)?7:((start&0xFC)==0xFC)?6:((start&0xF8)==0xF8)?5:
                ((start&0xF0)==0xF0)?4:((start&0xE0)==0xE0)?3:((start&0xC0)==0xC0)?2:1;
        byte[] bytes=new byte[8];
        bytes[0]=(byte)start;
        for(int i=1;i<l;i++){
            start=in.read();
            if(start==-1)
                start=0x80;
            bytes[i]=(byte)start;
        }
        return decodeCompressedUTF8(bytes,l);
    }
    static void readCodepoint(int start,InputStream in,StringBuilder out) throws IOException {
        // addLater allow codepoints outside unicode range
        out.append(Character.toString((int)readCodepoint(start,in)));
    }
    static boolean isItrSpace(int codepoint){
        return contains(new int[]{' ','\n','\t','\r'},codepoint);
    }

    Tuple stack;
    Stack<Tuple> stackStack;
    UTF8Input in;
    boolean implicitInput=false;
    private ItrLang(){
        Console c=System.console();
        if (c == null||c.charset().equals(StandardCharsets.UTF_8))
            in=UTF8Input.fromBytes(System.in);
        else // translate native encoding to UTF8
            in=UTF8Input.fromChars(new InputStreamReader(System.in,c.charset()));
        stack=new Tuple();
        stackStack=new Stack<>();
    }

    static class StackRow extends Tuple{
        StackRow(Value ... elts){super(elts);}
    }

    static int readInstruction(ArrayList<Integer> sourceCode,int ip){
        return ip>=0&&ip< sourceCode.size()?sourceCode.get(ip):'\0';
    }
    Value popValue() throws IOException {
        if(implicitInput&&stack.isEmpty()){
            readValue();
        }
        return stack.popOrDefault(Int.ZERO);
    }
    Value peekValue() throws IOException {
        if(implicitInput&&stack.isEmpty()){
            readValue();
        }
        return stack.peekOrDefault(Int.ZERO);
    }
    void pushValue(BigInteger i){
        stack.push(new Int(i));
    }
    void pushValue(Value v){
        stack.push(v);
    }

    void openStack(){
        stackStack.push(stack);
        stack = new Tuple();
    }
    void closeStack(){
        Tuple prevStack = stackStack.popOrDefault(new Tuple());
        int i=0;
        while(i<stack.size()&&stack.get(i) instanceof StackRow)
            i++;
        if(i>0){
            StackRow tail=new StackRow(stack.subList(i,stack.size()).toArray(Value[]::new));
            stack.truncate(i);
            pushValue(tail);
            prevStack.push(new Matrix(stack));
        }else {
            prevStack.push(stack);
        }
        stack = prevStack;
    }


    private static int findMatchingBracket(List<Integer> str,int i,int left,int right){
        int k=1;
        while(i++<str.size()&&k>0){
            if(str.get(i)==left)
                k++;
            if(str.get(i)==right)
                k--;
        }
        return i;
    }
    private interface ParserToken{}
    private record ValueToken(NumberValue value) implements ParserToken{}
    private record OperatorToken(int op) implements ParserToken{}
    private static Value tryParseNumber(List<Integer> str) {
        ArrayList<ParserToken> expr=new ArrayList<>();
        StringBuilder current=new StringBuilder();
        int base=10;//supported bases: 2-10 or 16
        for(int c:str){// TODO support floats
            if(c>='0'&&(c<=Math.min('0'+(base-1),'9'))){
                current.append((char)c);
                continue;
            }
            if(base==16&&((c>='A'&&c<='F')||(c>='a'&&c<='f'))){
                current.append((char)c);
                continue;
            }
            if(current.toString().equals("0")&&c=='x'){//hex literal
                base=16;
                continue;
            }
            if(current.toString().equals("0")&&c=='b'){//binary literal
                base=2;
                continue;
            }
            if(isItrSpace(c)){
                if(!current.isEmpty()){
                    expr.add(new ValueToken(new Int(new BigInteger(current.toString(),base))));
                }
                base=10;
                current.setLength(0);
                continue;
            }
            if(contains(new int[]{'+','-','*','/','I','J','K','i','j','k'},c)){
                if(!current.isEmpty()){
                    expr.add(new ValueToken(new Int(new BigInteger(current.toString(),base))));
                }
                base=10;
                expr.add(new OperatorToken(c));
                current.setLength(0);
                continue;
            }
            return null;//invalid char
        }
        if(!current.isEmpty()){
            expr.add(new ValueToken(new Int(new BigInteger(current.toString(),base))));
        }
        for(int i=0;i<expr.size();i++){// '\' and imaginary units
            if(expr.get(i) instanceof OperatorToken&&((OperatorToken) expr.get(i)).op=='/'){
                NumberValue l=Int.ONE,r=Int.ONE;
                if(i>0&&expr.get(i-1) instanceof ValueToken){
                    l=((ValueToken) expr.remove(i - 1)).value;
                    i--;
                }
                if(i+1<expr.size()&&expr.get(i+1) instanceof ValueToken){
                    r=((ValueToken) expr.remove(i + 1)).value;
                }
                expr.set(i,new ValueToken(realDivide(l,r)));
            }else if(expr.get(i) instanceof OperatorToken&&contains(new int[]{'I','J','K','i','j','k'},((OperatorToken) expr.get(i)).op)){
                NumberValue v=Int.ONE;
                if(i>0&&expr.get(i-1) instanceof ValueToken){
                    v=((ValueToken) expr.remove(i - 1)).value;
                    i--;
                }
                expr.set(i,new ValueToken(multiplyNumbers(v,Complex.I)));
            }
        }
        for(int i=0;i<expr.size();i++){// '*' (has to be parsed after imaginary units
            if(expr.get(i) instanceof OperatorToken&&((OperatorToken) expr.get(i)).op=='*'){
                NumberValue l=Int.ONE,r=Int.ONE;
                if(i>0&&expr.get(i-1) instanceof ValueToken){
                    l=((ValueToken) expr.remove(i - 1)).value;
                    i--;
                }
                if(i+1<expr.size()&&expr.get(i+1) instanceof ValueToken){
                    r=((ValueToken) expr.remove(i + 1)).value;
                }
                expr.set(i,new ValueToken(multiplyNumbers(l,r)));
            }
        }
        for(int i=0;i<expr.size();i++){// '+' and '-'
            if(expr.get(i) instanceof OperatorToken&&(((OperatorToken) expr.get(i)).op=='+'||((OperatorToken) expr.get(i)).op=='-')){
                boolean plus=((OperatorToken) expr.get(i)).op=='+';
                NumberValue l=Int.ZERO,r=Int.ZERO;
                if(i>0&&expr.get(i-1) instanceof ValueToken){
                    l=((ValueToken) expr.remove(i - 1)).value;
                    i--;
                }
                if(i+1<expr.size()&&expr.get(i+1) instanceof ValueToken){
                    r=((ValueToken) expr.remove(i + 1)).value;
                }
                expr.set(i,new ValueToken(plus?addNumbers(l,r):subtractNumbers(l,r)));
            }
        }
        return ((ValueToken)expr.get(0)).value;
    }
    static Value parseValue(List<Integer> str){
        if(str.isEmpty())
            return Int.ZERO;
        if(str.get(0)=='"'){
            Tuple buff=new Tuple();
            for(int i=1;i<str.size();i++){//read until next "
                if(str.get(i)=='\\'){//escape sequences
                    if(i+1==str.size()){
                        buff.add(new Int(BigInteger.valueOf('\\')));
                        continue;
                    }
                    //noinspection RedundantCast
                    switch ((int)str.get(++i)) {
                        case 't' -> buff.add(new Int(BigInteger.valueOf('\t')));
                        case 'n' -> buff.push(new Int(BigInteger.valueOf('\n')));
                        case 'r' -> buff.push(new Int(BigInteger.valueOf('\r')));
                        default -> buff.push(new Int(BigInteger.valueOf(str.get(i))));
                    }
                    continue;
                }
                if(str.get(i)=='"'){//unescaped " -> end of string
                    if(i<str.size()-1)
                        throw new IllegalArgumentException("unexpected end of string literal");
                    break;
                }
                buff.push(new Int(BigInteger.valueOf(str.get(i))));
            }
            return buff;
        }
        if(str.get(0)=='['||str.get(0)=='{'){
            Tuple buff=new Tuple();
            int i=0;
            while(i++<str.size()){
                while(isItrSpace(str.get(i)))i++;
                int i0=i;
                if(str.get(i)=='['){
                    i=findMatchingBracket(str,i,'[',']');
                    buff.push(parseValue(str.subList(i0,i)));
                    //addLater? report illegal characters
                    while(i<str.size()&&str.get(i)!=','&&str.get(i)!=']'&&str.get(i)!='}')i++;
                }else if(str.get(i)=='{'){
                    i=findMatchingBracket(str,i,'{','}');
                    buff.push(parseValue(str.subList(i0,i)));
                    while(i<str.size()&&str.get(i)!=','&&str.get(i)!=']'&&str.get(i)!='}')i++;
                }else if(str.get(i)=='('){
                    i=findMatchingBracket(str,i,'(',')');
                    buff.push(parseValue(str.subList(i0,i)));
                    while(i<str.size()&&str.get(i)!=','&&str.get(i)!=']'&&str.get(i)!='}')i++;
                }else if(str.get(i)=='"'){
                    while(i++<str.size()){
                        if(str.get(i)=='"')
                            break;
                        if(str.get(i)=='\\')
                            i++;
                    }
                    buff.push(parseValue(str.subList(i0,i)));
                    while(i<str.size()&&str.get(i)!=','&&str.get(i)!=']'&&str.get(i)!='}')i++;
                }else{
                    while(i<str.size()&&str.get(i)!=','&&str.get(i)!=']'&&str.get(i)!='}')i++;
                    if(i0!=i||(str.get(i)!=']'&&str.get(i)!='}'))
                        buff.push(parseValue(str.subList(i0,i)));
                }
                if(str.get(i)==']'||str.get(i)=='}')
                    break;
            }
            return buff;
        }
        if(str.get(0)=='('){
            Tuple rows=new Tuple();
            Tuple buff=new Tuple();
            int i=0;
            while(i++<str.size()){
                while(isItrSpace(str.get(i)))i++;
                int i0=i;
                if(str.get(i)=='['){
                    i=findMatchingBracket(str,i,'[',']');
                    buff.push(parseValue(str.subList(i0,i)));
                }else if(str.get(i)=='{'){
                    i=findMatchingBracket(str,i,'{','}');
                    buff.push(parseValue(str.subList(i0,i)));
                }else if(str.get(i)=='('){
                    i=findMatchingBracket(str,i,'(',')');
                    buff.push(parseValue(str.subList(i0,i)));
                }else if(str.get(i)=='"'){
                    while(i++<str.size()){
                        if(str.get(i)=='"')
                            break;
                        if(str.get(i)=='\\')
                            i++;
                    }
                    buff.push(parseValue(str.subList(i0,i)));
                }else{
                    while(i<str.size()&&!isItrSpace(str.get(i))&&str.get(i)!=','&&str.get(i)!='('&&str.get(i)!=')')i++;
                    if(i0!=i||(str.get(i)!=')'))
                        buff.push(parseValue(str.subList(i0,i)));
                }
                if(str.get(i)==')')
                    break;
                if(str.get(i)==','){
                    rows.push(buff);
                    buff=new Tuple();
                }
                if(contains(new int[]{'(','[','{','"'},str.get(i)))
                    i--;//ensure opening bracket is not skipped
            }
            if(!rows.isEmpty()){
                if(!buff.isEmpty())
                    rows.push(buff);
                return new Matrix(rows);
            }
            return buff;
        }
        Value v=tryParseNumber(str);
        if(v!=null)
            return v;
        return new Tuple(str.stream().map(i->new Int(BigInteger.valueOf(i))).toArray(Value[]::new));
    }

    void readBracket(int left,int right,ArrayList<Integer> buff) throws IOException {
        buff.add(left);
        int k=1;
        int cp=in.readCodepoint();
        while(cp>=0&&k>0){
            if(cp==left)
                k++;
            if(cp==right)
                k--;
            buff.add(cp);
            if(k>0)
                cp=in.readCodepoint();
        }
        pushValue(parseValue(buff));
    }
    void readValue() throws IOException {
        int cp=in.readCodepoint();
        while(isItrSpace(cp))//skip leading spaces
            cp=in.readCodepoint();
        ArrayList<Integer> buff=new ArrayList<>();
        if(cp=='"'){
            buff.add(cp);
            cp=in.readCodepoint();
            while(cp>=0){
                if(cp=='"') {
                    buff.add(cp);
                    break;
                }
                if(cp=='\\'){
                    buff.add(cp);
                    cp=in.readCodepoint();
                }
                buff.add(cp);
                cp=in.readCodepoint();
            }
            pushValue(parseValue(buff));
            return;
        }else if(cp=='['){
            readBracket('[',']',buff);
            return;
        }else if(cp=='{'){
            readBracket('{','}',buff);
            return;
        }else if(cp=='('){
            readBracket('(',')',buff);
            return;
        }
        while(cp>0&&!isItrSpace(cp)) {//skip leading spaces
            buff.add(cp);
            cp = in.readCodepoint();
        }
        pushValue(parseValue(buff));
    }
    static void writeCodepoint(int cp){
        System.out.print(Character.toString(cp));
    }

    static final int[] overwriteBlacklist=new int[]{';',' ','\n','»','«','"','\'','(',',',')','©','?','!','[',']'};

    //list of all iterator operations
    static final int[] iteratorOps=new int[]{'µ','R','M','×','Y','C','¶'};
    //list of all operators that are allowed as an isolated argument to a iterator operation
    static final int[] singleByteIteratorArgs=new int[]{
            ' ', '£', '¥',
            '+', '-', '·', '÷', ':', '%', 'd', '&', '|', 'x', '>', '=', '<',
            '¬', 's', 'a', '¿', '~', '¯',
            'e', '*', '/', '\\', '^',
            '¡', '°', 'L', 'º', '¹', 'S', 'P', 'Í', 'Ì', '®'
    };
    static boolean contains(int[] arr,int x){
        return Arrays.stream(arr).anyMatch(i -> i == x);
    }

    static HashMap<Integer, OpOverwrite> opOverwrites=new HashMap<>();
    record OpOverwrite(int op, boolean isAutoCall,ArrayList<Integer> asCode, Value asValue, HashMap<Integer, OpOverwrite> prevOverwrites) {
    }

    static Stream<NumberValue> flatten(Value v){
        if(v instanceof NumberValue)
            return Stream.of((NumberValue) v);
        if(v instanceof Matrix)
            v=((Matrix) v).rows;
        return v.asTuple().stream().flatMap(ItrLang::flatten);
    }

    static ArrayList<Integer> toCode(Value v){
        return new ArrayList<>(v.asTuple().stream().flatMap(ItrLang::flatten).map(e->e.asInt().intValueExact()).toList());
    }
    static void overwriteOp(int op,Value v,boolean autoCall){
        OpOverwrite overwrite;
        if(autoCall){
            ArrayList<Integer> code=toCode(v);
            overwrite=new OpOverwrite(op,true,code,null,opOverwrites);
        }else {
            overwrite=new OpOverwrite(op,false,null,v,opOverwrites);
        }
        opOverwrites=new HashMap<>(opOverwrites);
        opOverwrites.put(op,overwrite);
    }

    static int readItrArgs(ArrayList<Integer> sourceCode,int ip,ArrayList<Integer> argString){
        int op=readInstruction(sourceCode,ip++);
        boolean isNested=false;
        while(contains(iteratorOps,op)){
            argString.add(op);
            op=readInstruction(sourceCode,ip++);
            isNested=true;
        }
        if(opOverwrites.containsKey(op)){
            OpOverwrite o=opOverwrites.get(op);
            if(o.isAutoCall){
                argString.add(op);
                return ip;
            }
        }else if(contains(singleByteIteratorArgs,op)){
            argString.add(op);
            if(op=='L'){//pop argument when mapping with length
                argString.add((int)'à');
                argString.add((int)'å');
            }
            return ip;
        }
        if(op=='"'){
            op=readInstruction(sourceCode,ip++);
            while(op!='\0'&&op!='"'){
                argString.add(op);
                if(op=='\\'){
                    op=readInstruction(sourceCode,ip++);
                    argString.add(op);
                }
                op=readInstruction(sourceCode,ip++);
            }
            return ip;
        }
        int nestingLevel=1;
        boolean explicitString=false;
        if(op=='»'){
            op=readInstruction(sourceCode,ip++);
            explicitString=true;
            if(isNested)
                nestingLevel++;
        }
        boolean inString=false;
        while(op!='\0'&&(nestingLevel>1||inString||(op!='«'&&(explicitString||op!=';')))){// « (and ; if argument is implicit string) terminates map argument
            argString.add(op);
            if(inString){
                if(op=='"')
                    inString=false;
                if(op=='\\'){
                    op=readInstruction(sourceCode,ip++);
                    argString.add(op);
                    op=readInstruction(sourceCode,ip++);
                    continue;
                }
            }
            if(op=='\''){
                op=readInstruction(sourceCode,ip++);
                argString.add(op);
                op=readInstruction(sourceCode,ip++);
                continue;
            }
            if(op=='"')
                inString=true;
            if(op=='»')
                nestingLevel++;
            if(op=='«')
                nestingLevel--;
            op=readInstruction(sourceCode,ip++);
        }
        return ip-(op==';'?1:0);
    }

    void iteratorOpMap(List<Value> v, ArrayList<Integer> code) throws IOException {
        for(Value e:v){
            pushValue(e);
            interpret(code);
        }
    }
    void iteratorOpReduce(List<Value> v, ArrayList<Integer> code) throws IOException {
        if(v.isEmpty())
            return;
        pushValue(v.get(0));
        if(v.size()==1)
            return;
        for(Value e:v.subList(1,v.size())){
            pushValue(e);
            interpret(code);
        }
    }
    void iteratorOpSubsets(List<Value> v, ArrayList<Integer> code) throws IOException {
        BigInteger setId=BigInteger.ZERO,mask;
        int i;
        while(setId.bitLength()<=v.size()){
            mask=BigInteger.ONE;
            i=0;
            Tuple set=new Tuple();
            while(mask.bitLength()<=setId.bitLength()){
                if(setId.and(mask).signum()!=0)
                    set.push(v.get(i));
                mask=mask.shiftLeft(1);
                i++;
            }
            pushValue(set);
            interpret(code);
            setId=setId.add(BigInteger.ONE);
        }
    }
    void iteratorOpZip(List<Value> l,List<Value> r, ArrayList<Integer> code) throws IOException {
        int i=0;
        for(;i<l.size()&&i<r.size();i++){
            pushValue(l.get(i));
            pushValue(r.get(i));
            interpret(code);
        }
        for(;i<l.size();i++){
            pushValue(l.get(i));
            pushValue(Int.ZERO);
            interpret(code);
        }
        for(;i<r.size();i++){
            pushValue(Int.ZERO);
            pushValue(r.get(i));
            interpret(code);
        }
    }
    void iteratorOpCauchy(List<Value> l,List<Value> r, ArrayList<Integer> code) throws IOException {
        for(int s=0;s<l.size()+r.size()-1;s++){
            for(int i=Math.max(0,s-r.size()+1);i<l.size();i++){
                pushValue(l.get(i));
                pushValue(r.get(s-i));
                interpret(code);
            }
        }
    }
    void iteratorOpTimes(List<Value> l,List<Value> r, ArrayList<Integer> code) throws IOException {
        for (Value value : l) {
            for (Value item : r) {
                pushValue(value);
                pushValue(item);
                interpret(code);
            }
        }
    }

    void interpret(ArrayList<Integer> sourceCode) throws IOException {
        boolean numberMode=false;

        int command;
        for (int ip = 0; ip < sourceCode.size();) {
            command = readInstruction(sourceCode,ip++);
            if(command == '\0'){
                break;//reached end of program
            }
            if(command=='\''){
                command=readInstruction(sourceCode,ip++);
                //push char as string
                pushValue(new Tuple(new Int(BigInteger.valueOf(command))));
                continue;
            }
            if(command=='"'){
                ArrayList<Integer> str=new ArrayList<>();
                str.add((int)'"');//position of "
                while(ip<sourceCode.size()){
                    command=readInstruction(sourceCode,ip++);
                    str.add(command);
                    if(command=='"'){
                        str.add((int)'"');
                        break;
                    }
                    if(command=='\\'){
                        command=readInstruction(sourceCode,ip++);
                        str.add(command);
                    }
                }
                pushValue(parseValue(str));
                continue;
            }
            if(command=='»'){
                Tuple str=new Tuple();
                int level=1;
                while(ip<sourceCode.size()){
                    command=readInstruction(sourceCode,ip++);
                    if(command=='«'){
                        level--;
                        if(level==0)//TODO don't exit code-string literal within string or char literal
                            break;
                    }else if(command=='»'){
                        level++;
                    }
                    str.add(new Int(BigInteger.valueOf(command)));
                }
                // push code-points of string as tuple
                pushValue(str);
            }
            if(opOverwrites.containsKey(command)){
                numberMode=false;
                OpOverwrite o=opOverwrites.get(command);
                if(o.isAutoCall){
                    HashMap<Integer, OpOverwrite> prev=opOverwrites;
                    opOverwrites=o.prevOverwrites;
                    interpret(o.asCode);
                    opOverwrites=prev;
                    continue;
                }
                pushValue(o.asValue);
                continue;
            }
            if(command>='0'&&command<='9'){
                if(numberMode){
                    BigInteger v=((NumberValue)popValue()).asInt();
                    pushValue(BigInteger.TEN.multiply(v).add(BigInteger.valueOf(command-'0')));
                }else{
                    pushValue(BigInteger.valueOf(command-'0'));
                    numberMode=true;
                }
                continue;
            }
            numberMode=false;
            switch (command) {
                //noinspection DataFlowIssue (redundant switch labels)
                case '0','1','2','3','4','5','6','7','8','9', // digits have already handled
                        '"', '\'', '»', '«', //string and char-literals have already been handled
                        '\t', '\n', '\r' -> {}//ignore spaces
                //comments
                case ';' -> {
                    while (ip++ < sourceCode.size()) {
                        if (readInstruction(sourceCode, ip) == '\n')
                            break;
                    }
                    // continue;
                }
                case '(' ->//start tuple
                        openStack();
                case ',' -> { // create new stack row
                    int i = 0;
                    while (i < stack.size() && stack.get(i) instanceof StackRow)
                        i++;
                    StackRow tail = new StackRow(stack.subList(i, stack.size()).toArray(Value[]::new));
                    stack.truncate(i);
                    pushValue(tail);
                }
                case ')' -> //end tuple
                        closeStack();

                // control flow
                case '©' -> {
                    Value code = popValue();
                    interpret(toCode(code));
                }
                case '?' ->// ? start if/while statement
                        throw new Error("unimplemented");

                //break;
                case '!' ->// ? else ? inverted if
                        throw new Error("unimplemented");

                //break;
                case '[' ->// end-if
                        throw new Error("unimplemented");

                //break;
                case ']' ->// end-while
                        throw new Error("unimplemented");

                //break;
                case '$' -> {// overwrite character
                    Value v = popValue();
                    command = readInstruction(sourceCode, ip);
                    boolean autoCall = false;
                    if (command == '©') {
                        command = readInstruction(sourceCode, ++ip);
                        autoCall = true;
                    }
                    if (contains(overwriteBlacklist, command)) {
                        continue;
                    }
                    ip++;//consume next character
                    overwriteOp(command, v, autoCall);
                }

                // stack operations
                case 'ä' -> {//dup
                    Value a = peekValue();
                    pushValue(a);
                }
                case 'á' -> {//over
                    Value a = popValue();
                    Value b = peekValue();
                    pushValue(a);
                    pushValue(b);
                }
                case 'à' -> {//swap
                    Value a = popValue();
                    Value b = popValue();
                    pushValue(a);
                    pushValue(b);
                }
                case 'â' -> {//"under" (shorthand for swap, over) push top element below second element
                    Value a = popValue();
                    Value b = popValue();
                    pushValue(a);
                    pushValue(b);
                    pushValue(a);
                }
                case 'å' -> //drop
                        popValue();

                // IO
                case '_' -> // read byte
                        pushValue(BigInteger.valueOf(in.read()));
                case '#' -> // parse word
                        readValue();

                // addLater read char, read bytes
                // addLater read single line, read word
                case '§' -> {// read "paragraph" (read all characters until first empty line)
                    Tuple paragraph = new Tuple();
                    int c = in.readCodepoint();
                    while (c >= 0) {
                        if (c == '\n') {
                            c = in.readCodepoint();
                            if (c == '\n')//double-new line
                                break;
                            paragraph.add(new Int(BigInteger.valueOf('\n')));
                            continue;
                        }
                        paragraph.add(new Int(BigInteger.valueOf(c)));
                        c = in.readCodepoint();
                    }
                    pushValue(paragraph);
                }
                case '¥' -> {// write char(s)
                    Tuple t = popValue().asTuple();
                    t.stream().flatMap(ItrLang::flatten).
                            forEach(b -> writeCodepoint(b.asInt().add(BigInteger.valueOf(0xff)).intValueExact()));
                }
                case '£' -> // write value
                        System.out.print(popValue());

                // TODO value from/to  string
                // arithmetic operations
                case '+' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(add(a, b));
                }
                case '-' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::subtractNumbers));
                }
                case '·' -> {//point-wise multiplication
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::multiplyNumbers));
                }
                case '÷' -> {// point-wise (fractional) division
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::realDivide));
                }
                case ':' -> {//integer division
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::intDivide));
                }
                case '%' -> {// remainder
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::remainder));
                }
                case 'd' -> {//division and remainder
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::remainder));
                    pushValue(binaryNumberOp(a, b, ItrLang::intDivide));
                }
                case '&' -> {// bit-wise and
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::andNumbers));
                }
                case '|' -> {//  bit-wise or
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::orNumbers));
                }
                case 'x' -> {//  bit-wise xor
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, ItrLang::xorNumbers));
                }
                case '>' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, (x, y) -> new Int(BigInteger.valueOf(compareNumbers(x, y) > 0 ? 1 : 0))));
                }
                case '=' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, (x, y) -> new Int(BigInteger.valueOf(compareNumbers(x, y) == 0 ? 1 : 0))));
                }
                case '<' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, b, (x, y) -> new Int(BigInteger.valueOf(compareNumbers(x, y) < 0 ? 1 : 0))));
                }
                case '¬' -> {
                    Value a = popValue();
                    pushValue(unaryNumberOp(a, x -> compareNumbers(x, Int.ZERO) == 0 ? Int.ONE : Int.ZERO));
                }
                case '¿' -> {
                    Value a = popValue();
                    pushValue(unaryNumberOp(a, x -> compareNumbers(x, Int.ZERO) == 0 ? Int.ZERO : Int.ONE));
                }
                case 's' -> {//sign
                    Value a = popValue();
                    pushValue(unaryNumberOp(a, x -> {
                        if(x.isReal()){
                            int c = compareNumbers(x, Int.ZERO);
                            return new Int(BigInteger.valueOf(c > 0 ? 1 : c < 0 ? -1 : 0));
                        }
                        if(x instanceof Complex)
                            return realDivide(x,new Real(Complex.abs((Complex) x,mathContext)));
                        throw new Error("unsupported operand for 's': " + x.getClass().getName());
                    }));
                }
                case 'a' -> {//absolute value/determinant
                    Value a = popValue();
                    @SuppressWarnings("unchecked")
                    Function<Value, Value>[] f = new Function[1];
                    f[0] = (x) -> {
                        if (x instanceof Tuple)
                            return new Tuple(((Tuple) x).stream().map(f[0]).toArray(Value[]::new));
                        if (x.isReal())
                            return compareNumbers((NumberValue) x, Int.ZERO) < 0 ? negate(x) : x;
                        if(x instanceof Complex)
                            return new Real(Complex.abs((Complex) x,mathContext));
                        if (x instanceof Matrix)
                            return ((Matrix) x).determinant();
                        throw new Error("unsupported operand for 'a': " + x.getClass().getName());
                    };
                    pushValue(f[0].apply(a));
                }
                case '~' -> {
                    Value a = popValue();
                    pushValue(negate(a));
                }
                case '¯' -> {
                    Value a = popValue();
                    pushValue(invert(a));
                }
                case 'º' -> {
                    Value a = popValue();
                    if (a.isNumber()) {//XXX? 2D range for complex numbers
                        Tuple r = new Tuple();
                        for (BigInteger i = BigInteger.ZERO; compareNumbers(new Int(i), (NumberValue) a) < 0; i = i.add(BigInteger.ONE))
                            r.push(new Int(i));
                        pushValue(r);
                        break;
                    }
                    if (a instanceof Tuple) {
                        Tuple r = new Tuple();
                        for (int i = 0; i <= ((Tuple) a).size(); i++)
                            r.push(((Tuple) a).head(i));
                        pushValue(r);
                        break;
                    }
                    //XXX? what is the range of a matrix
                    throw new Error("unsupported operand for " + command + ": " + a.getClass().getName());
                }//break;
                case '¹' -> {
                    Value a = popValue();
                    if (a.isNumber()) {//XXX? 2D range for complex numbers
                        Tuple r = new Tuple();
                        for (BigInteger i = BigInteger.ONE; compareNumbers(new Int(i), (NumberValue) a) <= 0; i = i.add(BigInteger.ONE))
                            r.push(new Int(i));
                        pushValue(r);
                        break;
                    }
                    if (a instanceof Tuple) {
                        Tuple r = new Tuple();
                        for (int i = 1; i <= ((Tuple) a).size(); i++)
                            r.push(((Tuple) a).head(i));
                        pushValue(r);
                        break;
                    }
                    //XXX? what is the range of a matrix
                    throw new Error("unsupported operand for " + command + ": " + a.getClass().getName());
                }//break;
                case 'L' -> {//length
                    Value a = peekValue();
                    if (a instanceof Tuple)
                        pushValue(new Int(BigInteger.valueOf(((Tuple) a).size())));
                    else if (a instanceof Matrix)
                        pushValue(new Int(BigInteger.valueOf(((Matrix) a).nrows())));
                    else
                        pushValue(Int.ONE);
                }
                case 'e' -> {//exponential
                    Value a = popValue();
                    pushValue(applyFunction(a, "exp"));
                }
                case '½' -> {
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, new Int(BigInteger.valueOf(2)), ItrLang::realDivide));
                }
                case 'i','j','k' -> {
                    Value a = popValue();
                    pushValue(binaryNumberOp(a, Complex.I, ItrLang::multiply));
                }
                case '²' -> {
                    Value a = popValue();
                    pushValue(multiply(a, a));
                }
                case '³' -> {
                    Value a = popValue();
                    pushValue(multiply(a, multiply(a, a)));
                }

                // matrix operations
                case '*' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(multiply(a, b));
                }
                case '/' -> {// right division A/B -> AB⁻¹
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(divide_right(a, b));
                }
                case '\\' -> {// left division A\B -> A⁻¹B
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(divide_left(a, b));
                }
                case '^' -> {
                    Value b = popValue();
                    Value a = popValue();
                    pushValue(pow(a, b));
                }

                // vector operations
                case '¡' -> {
                    Tuple a = popValue().asTuple();
                    pushValue(repeat(a, -1));
                }
                case '°' -> {
                    Tuple b = popValue().asTuple();
                    Tuple a = popValue().asTuple();
                    pushValue(concatenate(a, b));
                }
                case 'µ' -> {//map
                    ArrayList<Integer> l = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, l);
                    List<Value> v = popValue().toList();
                    openStack();
                    iteratorOpMap(v, l);
                    closeStack();
                    // continue;
                }
                case 'R' -> {//reduce
                    ArrayList<Integer> l = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, l);
                    List<Value> v = popValue().toList();
                    openStack();
                    iteratorOpReduce(v, l);
                    closeStack();
                    // continue;
                }
                case 'M' -> {//flat-map
                    ArrayList<Integer> l = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, l);
                    List<Value> v = popValue().toList();
                    iteratorOpMap(v, l);
                    // continue;
                }
                case '×' -> {//Cartesian product
                    ArrayList<Integer> c = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, c);
                    List<Value> r = popValue().toList();
                    List<Value> l = popValue().toList();
                    openStack();
                    iteratorOpTimes(l, r, c);
                    closeStack();
                    // continue;
                }
                case 'Y' -> {//zip
                    ArrayList<Integer> c = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, c);
                    List<Value> r = popValue().toList();
                    List<Value> l = popValue().toList();
                    openStack();
                    iteratorOpZip(l, r, c);
                    closeStack();
                    // continue;
                }
                case 'C' -> {//cauchy-product
                    ArrayList<Integer> c = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, c);
                    List<Value> r = popValue().toList();
                    List<Value> l = popValue().toList();
                    openStack();
                    iteratorOpCauchy(l, r, c);
                    closeStack();
                    // continue;
                }
                case '¶' -> {// power set
                    ArrayList<Integer> l = new ArrayList<>();
                    ip = readItrArgs(sourceCode, ip, l);
                    List<Value> v = popValue().toList();
                    openStack();
                    iteratorOpSubsets(v, l);
                    closeStack();
                    // continue;
                }//break;
                case 'S' -> {// sum
                    Value v = popValue();
                    if (v.isNumber()) {//skip conversion of number to array and calculate result directly
                        //number is treated as if it were the 1-based range starting at that number
                        //XXX? handle complex numbers
                        NumberValue x = (NumberValue) v;
                        if (compareNumbers(x, Int.ZERO) <= 0) {
                            pushValue(Int.ZERO);
                            continue;
                        }
                        BigInteger i = x.asInt();
                        pushValue(new Int(i.multiply(i.add(BigInteger.ONE)).divide(BigInteger.TWO)));
                        continue;
                    }
                    v = v.toTuple();
                    @SuppressWarnings("unchecked")
                    Function<Tuple, Value>[] f = (Function<Tuple, Value>[]) new Function[1];
                    f[0] = (t) -> {
                        final Value[] res = new Value[]{Int.ZERO};
                        t.forEach(e -> res[0] = binaryNumberOp(res[0], e instanceof Tuple ? f[0].apply((Tuple) e) : e, ItrLang::addNumbers));
                        return res[0];
                    };
                    pushValue(f[0].apply((Tuple) v));
                }
                case 'P' -> {// product
                    Value v = popValue();
                    if (v.isNumber()) {//skip conversion of number to array and calculate result directly
                        //number is treated as if it were the 1-based range starting at that number
                        //XXX? handle complex numbers
                        NumberValue x = (NumberValue) v;
                        NumberValue P = Int.ONE;
                        for (BigInteger i = BigInteger.ONE; compareNumbers(new Int(i), x) <= 0; i = i.add(BigInteger.ONE))
                            P = multiplyNumbers(P, new Int(i));
                        pushValue(P);
                        continue;
                    }
                    v = v.toTuple();
                    @SuppressWarnings("unchecked")
                    Function<Tuple, Value>[] f = (Function<Tuple, Value>[]) new Function[1];
                    f[0] = (t) -> {
                        final Value[] res = new Value[]{Int.ONE};
                        t.forEach(e -> res[0] = multiply(res[0], e instanceof Tuple ? f[0].apply((Tuple) e) : e));
                        return res[0];
                    };
                    pushValue(f[0].apply((Tuple) v));
                }
                case 'Ì' -> {//indices of nonzero elements
                    Tuple v = popValue().asTuple();
                    Tuple res = new Tuple();
                    for (int i = 0; i < v.size(); i++) if (v.get(i).asBool()) res.push(new Int(BigInteger.valueOf(i)));
                    pushValue(res);
                }
                case 'Í' -> {//put nonzero element at indices given by vector
                    Tuple v = popValue().asTuple();
                    List<BigInteger> ints = v.stream().flatMap(ItrLang::flatten).map(NumberValue::asInt).toList();
                    BigInteger M = ints.stream().reduce(BigInteger.ZERO, (m, e) -> e.compareTo(m) > 0 ? e : m);
                    Tuple res = new Tuple();
                    res.addAll(Collections.nCopies(M.intValueExact() + 1, Int.ZERO));
                    ints.forEach(e -> {
                        if (e.signum() >= 0) res.set(e.intValueExact(), Int.ONE);
                    });
                    pushValue(res);
                }
                case '@' -> {//replace number with corresponding element of vector
                    Value I = popValue();
                    final Value v = popValue();
                    if (v.isNumber()) {//calculate result directly if v already is a number
                        NumberValue x = (NumberValue) v;
                        pushValue(unaryNumberOp(I, (e) -> {//number is treated as if it were the 1-based range starting at that number
                            BigInteger i = e.asInt();
                            return i.signum() >= 0 && compareNumbers(new Int(i), x) < 0 ? new Int(i.add(BigInteger.ONE)) : Int.ZERO;
                        }));
                        continue;
                    }
                    final Tuple t = v.toTuple();
                    pushValue(unaryNumberOp(I, (e) -> {
                        int i = e.asInt().intValueExact();
                        return i >= 0 && i < t.size() ? t.get(i) : Int.ZERO;
                    }));
                }
                case '®' -> {// vector to matrix
                    Value v = popValue();
                    if (v instanceof Tuple) {
                        Tuple elts = new Tuple();
                        ((Tuple) v).forEach(e -> elts.push(e.asTuple()));
                        pushValue(new Matrix(elts));
                        break;
                    }
                    if (v instanceof Matrix) {
                        pushValue(((Matrix) v).rows);
                        break;
                    }
                    pushValue(v);
                }
                default -> {
                }
            }
            // if there is ever code after the switch statement make sure to check all branches marked with continue
        }
    }

    public static String loadCode(File src,boolean utf8Mode) throws IOException {
        // TODO read to list of integers instead of Sting
        StringBuilder code = new StringBuilder();
        if(utf8Mode) {
            try(BufferedReader reader=new BufferedReader(new FileReader(src))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!code.isEmpty())
                        code.append('\n');
                    code.append(line);
                }
                return code.toString();
            }
        }
        boolean stringMode=false;
        int b;
        try(InputStream in=new BufferedInputStream(new FileInputStream(src))){
            while((b=in.read())!=-1){
                code.append((char)b);
                if(stringMode){
                    if(b=='"'){
                        code.append((char)b);//closing "
                        stringMode=false;
                        continue;
                    }else if(b=='\\'){
                        code.append((char)b);
                        b=in.read();
                        if(b==-1)
                            break;
                    }
                    readCodepoint(b,in,code);
                    continue;
                }
                if(b==';'){//skip comment
                    do{
                        b = in.read();
                    }while(b!=-1&&b!='\n');
                    continue;
                }
                if(b=='\''){
                    code.append((char)b);
                    b=in.read();
                    if(b==-1)
                        break;
                    readCodepoint(b,in,code);
                    continue;
                }else if(b=='"'){
                    stringMode=true;
                }
                code.append((char)b);
            }
            return code.toString();
        }
    }
    // TODO code -> byte conversion

    void printDebugInfo(){
        System.out.println("\n---------------");
        System.out.println("stack:"+stack);
        System.out.println("stackStack:"+stackStack);
    }
    public static void run(String code, List<Value> args, boolean debugMode) throws IOException {
        boolean explicitIn=false,explicitOut=false,stringMode=false;
        for(int i=0;i<code.length();i++){
            if(stringMode){
                if(code.charAt(i)=='\\'){
                    i++;
                    continue;
                }
                if(code.charAt(i)=='"'){
                    stringMode=false;
                }
                continue;
            }
            if(contains(new int[]{'_','#','§'},code.charAt(i))){
                explicitIn=true;
                continue;
            }
            if(contains(new int[]{'£','¥'},code.charAt(i))){
                explicitOut=true;
                continue;
            }
            if(code.charAt(i)=='"'){
                stringMode=true;
                continue;
            }
            if(code.charAt(i)==';'){
                while(i<code.length()&&code.charAt(i)!='\n')
                    i++;
                continue;
            }
        }
        ItrLang program=new ItrLang();
        program.implicitInput=!explicitIn;
        program.stack.addAll(args);
        program.interpret(new ArrayList<>(code.codePoints().boxed().toList()));
        if(!explicitOut){
            //TODO implicitly print top stack value (if no explicit output)
            System.out.println(program.stack.peekOrDefault(Int.ZERO));
        }
        if(debugMode){
            program.printDebugInfo();
        }
    }

    public static void main(String[] args) throws IOException {
        String code=null;
        boolean binaryMode=true,debugMode=false,hasSourceFile=false,ignoreFlags=false;
        File out=null;
        ArrayList<String> progArgs=new ArrayList<>();
        for(int i=0;i<args.length;i++){// addLater flag to run code directly
            if(args[i].startsWith("--")){
                ignoreFlags=true;
                continue;
            }
            //TODO help command
            if(!ignoreFlags&&args[i].equals("-f")){
                binaryMode=false;
                hasSourceFile=true;
                code=ItrLang.loadCode(new File(args[++i]),true);
                continue;
            }
            if(!ignoreFlags&&args[i].equals("-b")){
                binaryMode=true;
                hasSourceFile=true;
                code=ItrLang.loadCode(new File(args[++i]),false);
                continue;
            }
            if(!ignoreFlags&&args[i].equals("-o")){
                out=new File(args[++i]);
                continue;
            }
            if(!ignoreFlags&&args[i].equals("-d")){
                debugMode=true;
                continue;
            }
            progArgs.add(args[i]);
        }
        if(code==null){
            System.out.println("Missing source file, please specify a source file with one of the following arguments:");
            System.out.println(" -f <src-file>");
            System.out.println(" -b <src-file>");
            return;
        }
        if(out!=null){
            System.out.print(binaryMode);
            // TODO translate source-code from/to binary and store in out
        }
        ItrLang.run(code,progArgs.stream().map(s->parseValue(s.codePoints().boxed().toList())).toList(),debugMode||!hasSourceFile);
    }
}