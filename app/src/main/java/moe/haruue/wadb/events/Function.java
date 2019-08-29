package moe.haruue.wadb.events;

public interface Function<E> {

    void invoke(E event);
}
