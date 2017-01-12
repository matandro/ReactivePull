package bgu.cbs.reactivePull.Impl.misc;

/**
 * Created by matan on 12/01/17.
 */
public class Pair<L,R> {
    private L left;
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public Pair() {
        this.left = null;
        this.right = null;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }
}
