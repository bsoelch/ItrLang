package bsoelch.itrlang;

import java.util.Collections;

public class Matrix implements Value{
    public final Tuple rows;
    private int nrows;
    private int ncolumns;
    public Matrix(Tuple rows) {
        this.rows = new Tuple(rows.stream().map(Value::asTuple).toArray(Value[]::new));
        this.nrows=rows.size();
        this.ncolumns = rows.stream().mapToInt(v->((Tuple)v).size()).max().orElse(0);
    }
    public int nrows() {
        return nrows;
    }
    public int ncolumns() {
        return ncolumns;
    }

    Value at(int i,int j){
        if(i<0||i>=nrows)
            return Int.ZERO;
        Tuple row=((Tuple)rows.get(i));
        if(j<0||j>=row.size())
            return Int.ZERO;
        return row.get(j);
    }

    public Matrix copy(){
        return new Matrix((Tuple)rows.clone());
    }
    public Matrix transposed(){
        Tuple rows=new Tuple();
        rows.ensureCapacity(ncolumns);
        for(int c=0;c<ncolumns;c++){
            Tuple row=new Tuple();
            row.ensureCapacity(nrows);
            for(int r=0;r<nrows;r++) {
                row.add(at(r,c));
            }
            rows.add(row);
        }
        return new Matrix(rows);
    }

    public Value determinant() {
        Matrix A =copy();
        gaussElimination(A,null,false);
        Value det=Int.ONE;
        for(int r=0;r<Math.max(A.nrows,A.ncolumns);r++){
            det=ItrLang.multiply(det,A.at(r,r));
        }
        return det;
    }

    @Override
    public Tuple toTuple() {
        return rows;
    }
    @Override
    public boolean asBool() {
        return rows.asBool();
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
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isEqual(Value v) {
        if(!(v instanceof Matrix m))
            return false;
        int nrows=Math.max(this.nrows,m.nrows);
        int ncolumns=Math.max(this.ncolumns,m.ncolumns);
        for(int r=0;r<nrows;r++){
            for(int c=0;c<ncolumns;c++){
                if(!at(r,c).isEqual(m.at(r,c)))
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder s=new StringBuilder("(");
        this.rows.forEach(r->{
            r.asTuple().forEach(e->s.append(e).append(" "));
            s.replace(s.length()-(r.asTuple().size()>0?1:0),s.length(),",");
        });
        return s.replace(s.length()-1,s.length(),")").toString();
    }

    // Matrix-math

    public static Matrix multiply(Matrix A,Matrix B) {
        Tuple res=new Tuple();
        res.ensureCapacity(A.nrows);
        for(int r=0;r<A.nrows;r++){
            Tuple row=new Tuple(Collections.nCopies(B.ncolumns,Int.ZERO).toArray(Value[]::new));
            for(int k=0;k<A.ncolumns||k<B.nrows;k++){
                for(int c=0;c<B.ncolumns;c++){
                    row.set(c,ItrLang.add(row.get(c),ItrLang.multiply(A.at(r,k),B.at(k,c))));
                }
            }
            res.add(row);
        }
        return new Matrix(res);
    }

    // adds l*A[r] onto A[k], applies the same operation to B (if existent)
    static void addRow(int r,int k,Value l,Matrix A,Matrix B){
        while(k>=A.nrows){
            A.rows.push(new Tuple());
            A.nrows++;
        }
        if(B!=null){
            while(k>=B.nrows){
                B.rows.push(new Tuple());
                B.nrows++;
            }
        }
        for(int c=0;c<A.ncolumns;c++){
            Tuple row_k=(Tuple) A.rows.get(k);
            if(row_k.size()<=c){
                row_k.push(Int.ZERO);
                A.ncolumns=Math.max(A.ncolumns,row_k.size());
            }
            row_k.set(c,ItrLang.add(row_k.get(c),ItrLang.multiply(l,A.at(r,c))));
            if(B!=null){
                row_k=(Tuple) B.rows.get(k);
                if(row_k.size()<=c){
                    row_k.push(Int.ZERO);
                    B.ncolumns=Math.max(B.ncolumns,((Tuple) B.rows.get(k)).size());
                }
                row_k.set(c,ItrLang.add(row_k.get(c),ItrLang.multiply(l,B.at(r,c))));
            }
        }
    }
    // bring A in triangle form, apply same operations to B
    public static void gaussElimination(Matrix A, Matrix B, boolean complete) {
        for (int r = 0; r < A.nrows; r++) {
            if (!A.at(r, r).asBool()) {
                //find nonzero row, add it onto row r
                for (int k = r + 1; k < A.nrows; k++) {
                    if (A.at(k, r).asBool()) {
                        addRow(k, r, Int.ONE, A, B);
                        break;
                    }
                }
            }
            for (int k = r + 1; k < A.nrows; k++) {
                if (A.at(k, r).asBool()) {
                    addRow(r, k, ItrLang.negate(ItrLang.divide_right(A.at(k, r), A.at(r, r))), A, B);//subtract scaled row from row
                }
            }
        }
        if (complete) {
            for (int r = A.nrows - 1; r >= 0; r--) {
                if (!A.at(r, r).asBool())
                    continue;
                for (int k = 0; k < r; k++) {
                    if (A.at(k, r).asBool()) {
                        addRow(r, k, ItrLang.negate(ItrLang.divide_right(A.at(k, r), A.at(r, r))), A, B);//subtract scaled row from row
                    }
                }
            }
        }
    }
    public static Matrix idMatrix(int s){
        Tuple rows=new Tuple();
        rows.ensureCapacity(s);
        for(int i=0;i<s;i++){
            Tuple row=new Tuple(Collections.nCopies(s,Int.ZERO).toArray(Value[]::new));
            row.set(i,Int.ONE);
            rows.add(row);
        }
        return new Matrix(rows);
    }
    public static Matrix invert(Matrix A){
        A=A.copy();
        Matrix B=idMatrix(Math.max(A.nrows,A.ncolumns));
        gaussElimination(A,B,true);
        for(int r=0;r<B.nrows;r++){
            Value f=ItrLang.invert(A.at(r,r));
            ((Tuple)B.rows.get(r)).replaceAll(e->ItrLang.multiply(f,e));
        }
        return B;
    }
    public static Matrix divide_left(Matrix A, Matrix B){
        A=A.copy();
        B=B.copy();
        gaussElimination(A,B,true);
        for(int r=0;r<B.nrows;r++){
            Value f=ItrLang.invert(A.at(r,r));
            ((Tuple)B.rows.get(r)).replaceAll(e->ItrLang.multiply(f,e));
        }
        return B;
    }
    public static Matrix divide_right(Matrix A, Matrix B){// A=X*B -> A^T=B^T*X^T
        A=A.transposed();
        B=B.transposed();
        return divide_left(B,A).transposed();
    }
}
