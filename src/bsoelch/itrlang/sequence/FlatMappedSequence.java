package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.util.Iterator;
import java.util.function.Function;

public class FlatMappedSequence implements Sequence{
    public static Sequence from(Sequence a, Function<Value,Iterable<Value>> map) {
        return new FlatMappedSequence(a,map);
    }

    private final Sequence base;
    protected final Function<Value,Iterable<Value>> map;

    private FlatMappedSequence(Sequence base, Function<Value,Iterable<Value>> map) {
        this.base = base;
        this.map=map;
    }
    @Override
    public Iterator<Value> iterator() {
        return new Iterator<>() {
            final Iterator<Value> baseItr=base.iterator();
            Iterator<Value> currentItr=baseItr.hasNext()?map.apply(baseItr.next()).iterator():null;
            @Override
            public boolean hasNext() {
                return currentItr!=null&&currentItr.hasNext();
            }
            @Override
            public Value next() {
                Value next=currentItr.next();
                while(!(currentItr.hasNext())){
                    if(baseItr.hasNext())
                        currentItr=map.apply(baseItr.next()).iterator();
                    else{
                        currentItr=null;
                        break;
                    }
                }
                return next;
            }
        };
    }

    @Override
    public boolean isFinite() {
        return base.isFinite();
    }

    @Override
    public boolean isEqual(Value v) {
        return v==this;//addLater? equal base&maps -> equal sequence
    }
}
