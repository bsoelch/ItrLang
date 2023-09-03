package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Tuple;
import bsoelch.itrlang.Value;

import java.util.function.Function;

public class GroupedSequence implements Sequence{
    public static Sequence from(Sequence a, Function<Value,Value> map) {
        return new GroupedSequence(a,map);
    }

    private final Sequence base;
    protected final Function<Value,Value> key;

    private GroupedSequence(Sequence base, Function<Value,Value> key) {
        this.base = base;
        this.key=key;
    }


    private class GroupItr implements SequenceIterator{
        final SequenceIterator baseItr;
        final Tuple group;// addLater? handle case of infinitely large groups
        Value prevKey;
        GroupItr(SequenceIterator baseItr,Tuple group,Value prevKey){
            this.baseItr=baseItr;
            this.group=group;
            this.prevKey=prevKey;
        }
        @Override
        public boolean hasNext() {
            return group.size()>0||baseItr.hasNext();
        }
        @Override
        public Value next() {
            while(baseItr.hasNext()){
                Value e=baseItr.next();
                Value k=key.apply(e);
                if(prevKey==null){
                    group.push(e);
                    prevKey=k;
                    continue;
                }
                if(prevKey.isEqual(k)) {
                    group.push(e);
                    continue;
                }
                Value next=(Tuple)group.clone();
                prevKey=k;
                group.clear();
                group.push(e);
                return next;
            }
            Value next=(Tuple)group.clone();
            group.clear();
            return next;
        }

        @Override
        public SequenceIterator split() {
            return new GroupItr(baseItr.split(),(Tuple)group.clone(),prevKey);
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new GroupItr(base.iterator(),new Tuple(),null);
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
