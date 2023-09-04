package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

import java.util.function.Function;

public class FlatMappedSequence implements Sequence{
    public static Sequence from(Sequence a, Function<Value,Sequence> map) {
        return new FlatMappedSequence(a,map);
    }

    private final Sequence base;
    protected final Function<Value,Sequence> map;

    private FlatMappedSequence(Sequence base, Function<Value,Sequence> map) {
        this.base = base;
        this.map=map;
    }
    private class FlatMapItr implements SequenceIterator{
        final SequenceIterator baseItr;
        SequenceIterator currentItr;

        FlatMapItr(SequenceIterator baseItr,SequenceIterator current){
            this.baseItr=baseItr;
            this.currentItr=current;
            if(current==null){
                this.currentItr=baseItr.hasNext()?map.apply(baseItr.next()).iterator():null;
            }
            prepareItr();
        }

        void prepareItr(){
            if(currentItr!=null){
                while(!(currentItr.hasNext())){
                    if(baseItr.hasNext())
                        currentItr=map.apply(baseItr.next()).iterator();
                    else{
                        currentItr=null;
                        break;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return currentItr!=null&&currentItr.hasNext();
        }
        @Override
        public Value next() {
            Value next=currentItr.next();
            prepareItr();
            return next;
        }
        @Override
        public SequenceIterator split() {
            return new FlatMapItr(baseItr.split(),currentItr.split());
        }
    }
    @Override
    public SequenceIterator iterator() {
        return new FlatMapItr(base.iterator(),null);
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
