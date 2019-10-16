package moe.haruue.wadb.events;

public interface WadbFailureEvent extends Event {

    default void onRootPermissionFailure() {}

    default void onOperateFailure() {}
}
