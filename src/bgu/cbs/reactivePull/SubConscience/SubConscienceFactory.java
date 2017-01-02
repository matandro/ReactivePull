package bgu.cbs.reactivePull.SubConscience;

/**
 * Created by matan on 1/2/2017.
 */
public interface SubConscienceFactory<T> {
    public SubConscience makeSubConscience(T selectionMethod);
}
