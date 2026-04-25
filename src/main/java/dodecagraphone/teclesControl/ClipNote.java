package dodecagraphone.teclesControl;

/** One note entry in the copy/paste clipboard. Offsets are relative to selection top-left. */
public class ClipNote {
    public final int rowOffset;
    public final int colOffset;
    public final int velocity;
    public final boolean visible;
    public final boolean muted;
    public final boolean linked;
    public final boolean dotted;

    public ClipNote(int rowOffset, int colOffset, int velocity,
                    boolean visible, boolean muted, boolean linked, boolean dotted) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
        this.velocity  = velocity;
        this.visible   = visible;
        this.muted     = muted;
        this.linked    = linked;
        this.dotted    = dotted;
    }
}
