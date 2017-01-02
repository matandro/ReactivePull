package bgu.cbs.reactivePull.SubConscience;

/**
 * Created by matan on 1/2/2017.
 * extends Runnable for random through trail and update function for called thgouht trails
 */
public interface SubConscience<T> extends Runnable {
    public void updateSubconscience(T t);
}
