package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Value;

import java.util.Iterator;

public interface SequenceIterator extends Iterator<Value> {
    /**skip the next k elements*/
    default void skip(int k) {
        while(hasNext()&&k>0){
            next();
        }
    }
    /**create an interdependent copy of this iterator */
    SequenceIterator split();
}
