package bgu.cbs.reactivePull.memory;

/**
 * Created by matan on 1/2/2017.
 */
public interface MemoryPull<R,T> {
    public R getByConnection(T input, double alpha);
    public R getCache();
}
