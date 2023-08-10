package bsoelch.itrlang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

class Stack<T> extends ArrayList<T> {
    public Stack() {
        super();
    }

    public Stack(T[] elts) {
        super(Arrays.asList(elts));
    }

    void push(T val) {
        add(val);
    }

    T pop() {
        if (size() > 0)
            return remove(size() - 1);
        throw new NoSuchElementException();
    }

    T popOrDefault(T defVal) {
        if (size() > 0)
            return remove(size() - 1);
        return defVal;
    }

    T peek() {
        if (size() > 0)
            return get(size() - 1);
        throw new NoSuchElementException();
    }
    T peekOrDefault(T defVal) {
        if (size() > 0)
            return get(size() - 1);
        return defVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("( ");
        for (T t : this) sb.append(t).append(' ');
        return sb.append(")").toString();
    }
}
