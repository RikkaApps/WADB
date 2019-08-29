package moe.haruue.wadb.events;

public interface WadbStateChangedEvent extends Event {

    default void onWadbStarted(int port) {}

    default void onWadbStopped() {}
}
