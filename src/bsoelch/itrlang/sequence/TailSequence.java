package bsoelch.itrlang.sequence;

import bsoelch.itrlang.Sequence;
import bsoelch.itrlang.Value;

public class TailSequence implements Sequence {
    private final Sequence base;
    private final int k;
    public TailSequence(Sequence base, int k) {
        this.base=base;
        this.k=k;
    }

    @Override
    public boolean isFinite() {
        return base.isFinite();
    }

    @Override
    public SequenceIterator iterator() {
        SequenceIterator itr= base.iterator();
        itr.skip(k);
        return itr;
    }

    @Override
    public Sequence tail(int k) {
        return new TailSequence(base,this.k+k);
    }

    @Override
    public boolean isEqual(Value v) {
        return v instanceof TailSequence&&((TailSequence) v).k==k&&((TailSequence) v).base.isEqual(base);
    }
}
